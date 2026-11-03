package co.edu.konrad.consultaciudadano.auth.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import co.edu.konrad.consultaciudadano.catalogo.repository.RangoRepository;
import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;
import co.edu.konrad.consultaciudadano.usuario.entity.Usuario;
import co.edu.konrad.consultaciudadano.usuario.repository.PerfilRepository;
import co.edu.konrad.consultaciudadano.usuario.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIntegracionTest {
    private static final String SECRETO_TEST = "test-only-secret-with-at-least-32-characters";
    private static final String IDENTIFICACION_PERSONAL = "9000000091";
    private static final String CONTRASENA = "Abc123";
    private static final int PUERTO_SMTP_TEST = 25251;
    private static final GreenMail SMTP = new GreenMail(
            new ServerSetup(PUERTO_SMTP_TEST, "127.0.0.1", ServerSetup.PROTOCOL_SMTP));

    static {
        SMTP.start();
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired PerfilRepository perfiles;
    @Autowired RangoRepository rangos;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired JavaMailSenderImpl mailSender;

    @AfterAll
    static void detenerSmtp() {
        SMTP.stop();
    }

    @BeforeEach
    void crearPersonalDePrueba() {
        if (usuarios.existsByIdentificacion(IDENTIFICACION_PERSONAL)) {
            return;
        }
        var perfil = perfiles.findByCodigo("PERSONAL_FUERZA_PUBLICA").orElseThrow();
        var personal = new Usuario(IDENTIFICACION_PERSONAL, "Personal", "Prueba",
                "personal-seguridad@example.test", null, null, null, passwordEncoder.encode(CONTRASENA));
        personal.getPerfiles().add(perfil);
        usuarios.saveAndFlush(personal);
    }

    @Test
    void loginValidoDevuelveTokenYCredencialesInvalidasSonGenericas() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(IDENTIFICACION_PERSONAL, CONTRASENA, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"));

        var existente = respuestaLogin(IDENTIFICACION_PERSONAL, "Wrong1");
        var inexistente = respuestaLogin("9000000094", "Wrong1");
        assertTrue(existente.getResponse().getContentAsString().contains("Credenciales inválidas"));
        assertTrue(inexistente.getResponse().getContentAsString().contains("Credenciales inválidas"));
        assertTrue(objectMapper.readTree(existente.getResponse().getContentAsString()).get("mensaje").asText()
                .equals(objectMapper.readTree(inexistente.getResponse().getContentAsString())
                        .get("mensaje").asText()));
    }

    @Test
    void recordarExtiendeLaVidaDelTokenYLasRutasPublicasSiguenAbiertas() throws Exception {
        var corto = objectMapper.readTree(respuestaLogin(IDENTIFICACION_PERSONAL, CONTRASENA)
                .getResponse().getContentAsString()).get("expiraEnSegundos").asLong();
        var largo = objectMapper.readTree(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(IDENTIFICACION_PERSONAL, CONTRASENA, true)))
                .andReturn().getResponse().getContentAsString()).get("expiraEnSegundos").asLong();
        assertTrue(largo > corto);
        mvc.perform(get("/api/health")).andExpect(status().isOk());
        mvc.perform(get("/api/catalogos/fuerzas-rangos")).andExpect(status().isOk());
    }

    @Test
    void bloqueaTrasCincoIntentosYElSextoSigueSiendoGenerico() throws Exception {
        for (int intento = 0; intento < 5; intento++) {
            assertEquals(401, respuestaLogin("9000000093", "Wrong1").getResponse().getStatus());
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("9000000093", "Wrong1", false)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));
    }

    @Test
    void protegeRutasConJwtYAplicaRoles() throws Exception {
        mvc.perform(get("/api/ciudadanos")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.detalles").isArray());

        mvc.perform(get("/api/ciudadanos").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/ciudadanos").header("Authorization", "Bearer " + tokenExpirado()))
                .andExpect(status().isUnauthorized());

        var token = tokenPersonal();
        mvc.perform(get("/api/ciudadanos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").exists());
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/ciudadanos").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registroValidaDefensaGuardaHashYNoExponeLaContrasena() throws Exception {
        var raiz = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        var eventos = new ListAppender<ILoggingEvent>();
        eventos.start();
        raiz.addAppender(eventos);
        try {
            SMTP.reset();
            assertEquals("127.0.0.1", mailSender.getHost());
            assertEquals(PUERTO_SMTP_TEST, mailSender.getPort());
            var request = registroValido();
            var resultado = mvc.perform(post("/api/personal/registro").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();
            var respuesta = resultado.getResponse().getContentAsString();
            assertTrue(SMTP.waitForIncomingEmail(5_000, 1));
            var correo = SMTP.getReceivedMessages()[0];
            assertEquals("registro-seguridad@example.test", correo.getAllRecipients()[0].toString());
            var contenido = (String) correo.getContent();
            var matcher = Pattern.compile("temporal es: ([A-Za-z\\d]{6})").matcher(contenido);
            assertTrue(matcher.find());
            var contrasenaGenerada = matcher.group(1);
            assertTrue(contrasenaGenerada.matches(".*[A-Z].*"));
            assertTrue(contrasenaGenerada.matches(".*[a-z].*"));
            assertTrue(contrasenaGenerada.matches(".*\\d.*"));
            assertFalse(respuesta.contains(contrasenaGenerada));
            var hash = jdbc.queryForObject("SELECT password_hash FROM usuario WHERE identificacion = ?",
                    String.class, "8000000005");
            assertTrue(hash.startsWith("$2"));
            assertFalse(eventos.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .anyMatch(mensaje -> mensaje.contains(contrasenaGenerada)));

            mvc.perform(post("/api/personal/registro").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        } finally {
            raiz.detachAppender(eventos);
            eventos.stop();
        }
    }

    @Test
    void registroRechazaDatosQueNoCoincidenConDefensa() throws Exception {
        var request = """
                {"identificacion":"8000000011","nombres":"Desconocido","apellidos":"Prueba",
                 "fuerzaCodigo":"POLICIA","rangoId":1,"idFuerza":"DEMO-011","correo":"rechazado@example.test"}
                """;
        mvc.perform(post("/api/personal/registro").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").exists());
    }

    private MvcResult respuestaLogin(String identificacion, String contrasena) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(loginJson(identificacion, contrasena, false))).andReturn();
    }

    private String tokenPersonal() throws Exception {
        var resultado = respuestaLogin(IDENTIFICACION_PERSONAL, CONTRASENA);
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
    }

    private String tokenExpirado() {
        var ahora = Instant.now();
        return Jwts.builder().subject(IDENTIFICACION_PERSONAL).id("00000000-0000-0000-0000-000000000001")
                .claim("roles", java.util.List.of("PERSONAL_FUERZA_PUBLICA"))
                .issuedAt(Date.from(ahora.minusSeconds(120))).expiration(Date.from(ahora.minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRETO_TEST.getBytes(StandardCharsets.UTF_8))).compact();
    }

    private String loginJson(String identificacion, String contrasena, boolean recordar) {
        return "{\"identificacion\":\"" + identificacion + "\",\"contrasena\":\"" + contrasena
                + "\",\"recordarContrasena\":" + recordar + "}";
    }

    private RegistroPersonalRequest registroValido() {
        var rango = rangos.findByFuerzaCodigoOrderByNombre("POLICIA").stream()
                .filter(candidato -> candidato.getNombre().equals("Subintendente"))
                .findFirst().orElseThrow();
        return new RegistroPersonalRequest("8000000005", "Ana", "Prueba5", "POLICIA", rango.getId(),
                "DEMO-005", "registro-seguridad@example.test");
    }
}
