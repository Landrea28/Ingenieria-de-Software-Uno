package co.edu.konrad.consultaciudadano.auth.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import co.edu.konrad.consultaciudadano.catalogo.repository.RangoRepository;
import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;
import co.edu.konrad.consultaciudadano.usuario.dto.ActualizarUsuarioRequest;
import co.edu.konrad.consultaciudadano.usuario.dto.CrearUsuarioRequest;
import co.edu.konrad.consultaciudadano.usuario.dto.EstadoUsuario;
import co.edu.konrad.consultaciudadano.usuario.service.UsuarioService;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import co.edu.konrad.consultaciudadano.auth.service.ServicioJwt;
import co.edu.konrad.consultaciudadano.auth.service.ServicioRevocacionToken;
import co.edu.konrad.consultaciudadano.usuario.entity.Usuario;
import co.edu.konrad.consultaciudadano.usuario.repository.PerfilRepository;
import co.edu.konrad.consultaciudadano.usuario.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Duration;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SeguridadIntegracionTest.RelojControlado.class)
class SeguridadIntegracionTest {
    private static final String SECRETO_TEST = "test-only-secret-with-at-least-32-characters";
    private static final String IDENTIFICACION_PERSONAL = "9000000091";
    private static final String IDENTIFICACION_ADMIN = "9000000092";
    private static final String CONTRASENA_ADMIN = "Admin123";
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
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired DataSource dataSource;
    @Autowired MutableClock reloj;
    @Autowired ServicioJwt jwt;
    @Autowired UsuarioService usuarioService;
    @Autowired @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping;
    @Autowired ServicioRevocacionToken revocacion;

    @AfterAll
    static void detenerSmtp() {
        SMTP.stop();
    }

    @BeforeEach
    void crearPersonalDePrueba() {
        reloj.reset();
        jdbc.update("DELETE FROM auth_login_intento");
        jdbc.update("DELETE FROM auth_registro_intento");
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
    void bloqueaEnElSextoIntentoYSeDesbloqueaTrasQuinceMinutos() throws Exception {
        for (int intento = 0; intento < 5; intento++) {
            assertEquals(401, respuestaLogin("9000000093", "Wrong1").getResponse().getStatus());
        }
        assertFalse(bloqueos().estaBloqueada("9000000093"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("9000000093", "Wrong1", false)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));
        assertTrue(bloqueos().estaBloqueada("9000000093"));
        reloj.advance(Duration.ofMinutes(14).plusSeconds(59));
        assertTrue(bloqueos().estaBloqueada("9000000093"));
        reloj.advance(Duration.ofSeconds(1));
        assertFalse(bloqueos().estaBloqueada("9000000093"));
    }

    private co.edu.konrad.consultaciudadano.auth.service.ServicioBloqueoLogin bloqueos() {
        return new co.edu.konrad.consultaciudadano.auth.service.ServicioBloqueoLogin(
                jdbc, transactionManager, dataSource, reloj);
    }

    @Test
    void protegeRutasConJwtYAplicaRoles() throws Exception {
        mvc.perform(get("/api/ciudadanos")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.detalles").isArray())
                .andExpect(jsonPath("$.fecha").doesNotExist());

        mvc.perform(get("/api/ciudadanos").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/ciudadanos").header("Authorization", "Bearer " + tokenExpirado()))
                .andExpect(status().isUnauthorized());

        var token = tokenPersonal();
        mvc.perform(get("/api/ciudadanos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.detalles").isArray())
                .andExpect(jsonPath("$.fecha").doesNotExist());
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
    void identificacionInexistenteYDatosNoCoincidentesDevuelvenElMismoError() throws Exception {
        var identificacionInexistente = """
                {"identificacion":"8000000011","nombres":"Desconocido","apellidos":"Prueba",
                 "fuerzaCodigo":"POLICIA","rangoId":1,"idFuerza":"DEMO-011","correo":"rechazado@example.test"}
                """;
        var datosNoCoincidentes = """
                {"identificacion":"8000000005","nombres":"Desconocido","apellidos":"Prueba",
                 "fuerzaCodigo":"POLICIA","rangoId":1,"idFuerza":"DEMO-011","correo":"rechazado@example.test"}
                """;

        var respuestaInexistente = mvc.perform(post("/api/personal/registro")
                        .contentType(MediaType.APPLICATION_JSON).content(identificacionInexistente))
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
        var respuestaNoCoincidente = mvc.perform(post("/api/personal/registro")
                        .contentType(MediaType.APPLICATION_JSON).content(datosNoCoincidentes))
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

        assertEquals(respuestaInexistente, respuestaNoCoincidente);
    }

    @Test
    void registroConCamposExcesivosOInyeccionSqlDevuelve400() throws Exception {
        var nombreExcesivo = "A".repeat(101);
        var requestLongitudExcesiva = """
                {"identificacion":"8000000011","nombres":"%s","apellidos":"Prueba",
                 "fuerzaCodigo":"POLICIA","rangoId":1,"idFuerza":"DEMO-011","correo":"rechazado@example.test"}
                """.formatted(nombreExcesivo);
        var requestInyeccionSql = """
                {"identificacion":"1' OR '1'='1","nombres":"Desconocido","apellidos":"Prueba",
                 "fuerzaCodigo":"POLICIA","rangoId":1,"idFuerza":"DEMO-011","correo":"rechazado@example.test"}
                """;

        mvc.perform(post("/api/personal/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(requestLongitudExcesiva))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/personal/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(requestInyeccionSql))
                .andExpect(status().isBadRequest());
    }

    @Test
    void limitaIntentosRepetidosDeRegistroPorIp() throws Exception {
        var registroRechazado = """
                {"identificacion":"8000000011","nombres":"No","apellidos":"Coincide",
                 "fuerzaCodigo":"POLICIA","rangoId":1,"idFuerza":"DEMO-011","correo":"limitado@example.test"}
                """;
        for (int intento = 0; intento < 5; intento++) {
            mvc.perform(post("/api/personal/registro").contentType(MediaType.APPLICATION_JSON)
                            .content(registroRechazado))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/personal/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(registroRechazado))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("LIMITE_REGISTRO"));
    }

    @Test
    void administraUsuariosConHashEstadoYRevocacionDeTokens() throws Exception {
        var administrador = asegurarAdministrador();
        SMTP.reset();
        var perfilPersonal = perfiles.findByCodigo("PERSONAL_FUERZA_PUBLICA").orElseThrow();
        var solicitud = new CrearUsuarioRequest("8012345671", "Usuario", "Gestion",
                "usuario-gestion@example.test", perfilPersonal.getId());
        var rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        var eventos = new ListAppender<ILoggingEvent>();
        eventos.start();
        rootLogger.addAppender(eventos);
        try {
            var tokenAdmin = token(administrador);
            var creado = mvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(solicitud)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.contrasena").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist())
                    .andReturn();
            assertTrue(SMTP.waitForIncomingEmail(5_000, 1));
            var cuerpoCorreo = (String) SMTP.getReceivedMessages()[0].getContent();
            assertTrue(cuerpoCorreo.contains("[SIMULADO]"));
            var coincidencia = Pattern.compile("temporal es: ([A-Za-z\\d]{6})").matcher(cuerpoCorreo);
            assertTrue(coincidencia.find());
            var contrasenaTemporal = coincidencia.group(1);
            assertTrue(contrasenaTemporal.matches("(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)[A-Za-z\\d]{6}"));
            var hash = jdbc.queryForObject("SELECT password_hash FROM usuario WHERE identificacion = ?",
                    String.class, solicitud.identificacion());
            assertTrue(hash.startsWith("$2"));
            assertFalse(eventos.list.stream().map(ILoggingEvent::getFormattedMessage)
                    .anyMatch(mensaje -> mensaje.contains(contrasenaTemporal)));

            var tokenPersonal = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson(solicitud.identificacion(), contrasenaTemporal, false)))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            var jwtPersonal = objectMapper.readTree(tokenPersonal).get("token").asText();
            var perfilAdmin = perfiles.findByCodigo("ADMINISTRADOR").orElseThrow();
            var cambioPerfil = new ActualizarUsuarioRequest("Usuario", "Gestion", solicitud.correo(),
                    perfilAdmin.getId(), EstadoUsuario.ACTIVO);
            mvc.perform(put("/api/usuarios/" + solicitud.identificacion())
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cambioPerfil)))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/perfiles").header("Authorization", "Bearer " + jwtPersonal))
                    .andExpect(status().isUnauthorized());

            mvc.perform(get("/api/usuarios?estado=ACTIVO&perfil=PERSONAL_FUERZA_PUBLICA&page=0&size=10")
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist());
            mvc.perform(get("/api/usuarios/" + solicitud.identificacion())
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + jwtPersonal))
                    .andExpect(status().isUnauthorized());

            var desactivar = new ActualizarUsuarioRequest("Usuario", "Gestion", solicitud.correo(),
                    perfilPersonal.getId(), EstadoUsuario.INACTIVO);
            mvc.perform(put("/api/usuarios/" + solicitud.identificacion())
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(desactivar)))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/perfiles").header("Authorization", "Bearer " + jwtPersonal))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson(solicitud.identificacion(), contrasenaTemporal, false)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));

            var activar = new ActualizarUsuarioRequest("Usuario", "Gestion", solicitud.correo(),
                    perfilPersonal.getId(), EstadoUsuario.ACTIVO);
            mvc.perform(put("/api/usuarios/" + solicitud.identificacion())
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(activar)))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/perfiles").header("Authorization", "Bearer " + jwtPersonal))
                    .andExpect(status().isUnauthorized());

            mvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(solicitud)))
                    .andExpect(status().isConflict());
        } finally {
            rootLogger.detachAppender(eventos);
            eventos.stop();
        }
    }

    @Test
    void protegePerfilesYNoPermiteRetirarGestionAlAdministradorNiAutodesactivarse() throws Exception {
        var administrador = asegurarAdministrador();
        var tokenAdmin = token(administrador);
        var tokenPersonalJwt = tokenPersonal();
        var perfilAdmin = perfiles.findByCodigo("ADMINISTRADOR").orElseThrow();
        var perfilPersonal = perfiles.findByCodigo("PERSONAL_FUERZA_PUBLICA").orElseThrow();
        var permisoSeguridad = jdbc.queryForObject(
                "SELECT id FROM permiso WHERE codigo = 'SEGURIDAD_ADMINISTRAR'", Long.class);

        mvc.perform(get("/api/perfiles").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
        mvc.perform(get("/api/permisos").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
        mvc.perform(get("/api/perfiles").header("Authorization", "Bearer " + tokenPersonalJwt))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/perfiles/" + perfilAdmin.getId() + "/permisos")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
        mvc.perform(put("/api/perfiles/" + perfilAdmin.getId() + "/permisos")
                        .header("Authorization", "Bearer " + tokenAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permisosIds\":[]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PERMISOS_ADMIN_REQUERIDOS"));
        mvc.perform(put("/api/perfiles/" + perfilPersonal.getId() + "/permisos")
                        .header("Authorization", "Bearer " + tokenAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permisosIds\":[" + permisoSeguridad + "]}"))
                .andExpect(status().isOk());

        var solicitud = new ActualizarUsuarioRequest("Administrador", "Inicial",
                "admin-seguridad@example.test", perfilPersonal.getId(), EstadoUsuario.INACTIVO);
        mvc.perform(put("/api/usuarios/" + IDENTIFICACION_ADMIN)
                        .header("Authorization", "Bearer " + tokenAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("AUTODESACTIVACION_NO_PERMITIDA"));

        var segundoAdmin = new Usuario("9000000095", "Segundo", "Administrador",
                "segundo-admin@example.test", null, null, null, passwordEncoder.encode("Admin123"));
        segundoAdmin.getPerfiles().add(perfilAdmin);
        usuarios.saveAndFlush(segundoAdmin);
        var desactivarSegundoAdmin = new ActualizarUsuarioRequest("Segundo", "Administrador",
                "segundo-admin@example.test", perfilAdmin.getId(), EstadoUsuario.INACTIVO);
        mvc.perform(put("/api/usuarios/9000000095").header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(desactivarSegundoAdmin)))
                .andExpect(status().isOk());

        var contextoAnterior = org.springframework.security.core.context.SecurityContextHolder.getContext();
        try {
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("otro-administrador", null,
                            java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))));
            var ultimaActualizacion = new ActualizarUsuarioRequest("Administrador", "Inicial",
                    "admin-seguridad@example.test", perfilAdmin.getId(), EstadoUsuario.INACTIVO);
            var error = org.junit.jupiter.api.Assertions.assertThrows(NegocioException.class,
                    () -> usuarioService.actualizar(IDENTIFICACION_ADMIN, ultimaActualizacion));
            assertEquals(CodigoError.ULTIMO_ADMIN_ACTIVO, error.getCodigo());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            contextoAnterior.setAuthentication(null);
        }
    }

    @Test
    void cadaEndpointApiTieneUnaPoliticaDeAccesoDeclarada() {
        for (RequestMappingInfo ruta : mapping.getHandlerMethods().keySet()) {
            var metodos = ruta.getMethodsCondition().getMethods();
            for (String patron : ruta.getPatternValues()) {
                if (!patron.startsWith("/api/")) {
                    continue;
                }
                assertFalse(metodos.isEmpty(), "El endpoint " + patron + " debe declarar metodos HTTP");
                for (RequestMethod metodo : metodos) {
                    assertTrue(tienePoliticaExplicita(metodo, patron),
                            () -> "Falta politica de acceso explicita para " + metodo + " " + patron);
                }
            }
        }
    }

    private boolean tienePoliticaExplicita(RequestMethod metodo, String patron) {
        if ((patron.equals("/api/health") || patron.equals("/api/catalogos/fuerzas-rangos"))
                && metodo == RequestMethod.GET) {
            return true;
        }
        if ((patron.equals("/api/auth/login") || patron.equals("/api/personal/registro"))
                && metodo == RequestMethod.POST) {
            return true;
        }
        if (patron.equals("/api/auth/logout") && metodo == RequestMethod.POST) {
            return true;
        }
        if (patron.equals("/api/antecedentes/{identificacion}")) {
            return metodo == RequestMethod.GET;
        }
        if (patron.equals("/api/antecedentes/{identificacion}/estado")) {
            return metodo == RequestMethod.PUT;
        }
        if (patron.equals("/api/ciudadanos")) {
            return metodo == RequestMethod.GET || metodo == RequestMethod.POST;
        }
        if (patron.equals("/api/ciudadanos/{identificacion}")) {
            return metodo == RequestMethod.GET || metodo == RequestMethod.PUT;
        }
        if (patron.equals("/api/reportes/ciudadanos") || patron.equals("/api/perfiles")
                || patron.equals("/api/permisos")) {
            return metodo == RequestMethod.GET;
        }
        if (patron.equals("/api/usuarios")) {
            return metodo == RequestMethod.GET || metodo == RequestMethod.POST;
        }
        if (patron.equals("/api/usuarios/{identificacion}")) {
            return metodo == RequestMethod.GET || metodo == RequestMethod.PUT;
        }
        return patron.equals("/api/perfiles/{id}/permisos")
                && (metodo == RequestMethod.GET || metodo == RequestMethod.PUT);
    }

    @Test
    void matrizDeLecturasProtegidasCubreTodosLosTiposDeTokenYRoles() throws Exception {
        var administrador = asegurarAdministrador();
        var tokenAdmin = token(administrador);
        var tokenPersonalValido = tokenPersonal();
        var usuarioPersonal = usuarios.findByIdentificacion(IDENTIFICACION_PERSONAL).orElseThrow();
        var emitido = jwt.emitir(usuarioPersonal, false);
        revocacion.revocar(emitido.jti(), emitido.expiraEn());
        var perfilAdmin = perfiles.findByCodigo("ADMINISTRADOR").orElseThrow();
        var endpoints = java.util.List.of(
                new EndpointLectura("/api/ciudadanos", false),
                new EndpointLectura("/api/ciudadanos/9000000001", false),
                new EndpointLectura("/api/antecedentes/9000000001", true),
                new EndpointLectura("/api/reportes/ciudadanos?estado=REQUERIDO", false),
                new EndpointLectura("/api/usuarios", false),
                new EndpointLectura("/api/usuarios/" + IDENTIFICACION_ADMIN, false),
                new EndpointLectura("/api/perfiles", false),
                new EndpointLectura("/api/permisos", false),
                new EndpointLectura("/api/perfiles/" + perfilAdmin.getId() + "/permisos", false));
        var tokens = java.util.List.of(
                new TokenMatriz("sin token", null),
                new TokenMatriz("invalido", "Bearer invalido"),
                new TokenMatriz("vencido", "Bearer " + tokenExpirado()),
                new TokenMatriz("revocado", "Bearer " + emitido.valor()),
                new TokenMatriz("personal", "Bearer " + tokenPersonalValido),
                new TokenMatriz("administrador", "Bearer " + tokenAdmin));

        for (var endpoint : endpoints) {
            for (var estadoToken : tokens) {
                var peticion = get(endpoint.ruta());
                if (estadoToken.autorizacion() != null) {
                    peticion.header("Authorization", estadoToken.autorizacion());
                }
                var response = mvc.perform(peticion).andReturn().getResponse();
                var esperado = switch (estadoToken.nombre()) {
                    case "sin token", "invalido", "vencido", "revocado" -> 401;
                    case "personal" -> endpoint.permitePersonal() ? 200 : 403;
                    default -> 200;
                };
                assertEquals(esperado, response.getStatus(),
                        () -> endpoint.ruta() + " con " + estadoToken.nombre());
                if (endpoint.ruta().startsWith("/api/reportes") && "administrador".equals(estadoToken.nombre())) {
                    assertEquals(MediaType.APPLICATION_PDF_VALUE, response.getContentType());
                    assertTrue(new String(response.getContentAsByteArray(), StandardCharsets.US_ASCII)
                            .startsWith("%PDF"));
                }
            }
        }
    }

    @Test
    void aplicaCabecerasDeSeguridadYNoExponeDocumentacionFueraDeDev() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("X-Frame-Options", "DENY"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string(org.springframework.http.HttpHeaders.CACHE_CONTROL,
                                org.hamcrest.Matchers.containsString("no-store")));
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is4xxClientError());
        mvc.perform(get("/actuator/health")).andExpect(status().is4xxClientError());
    }

    @Test
    void antecedentesSinEstadoOInexistentesDevuelven404ParaPersonalAutenticado() throws Exception {
        var token = tokenPersonal();
        mvc.perform(get("/api/antecedentes/9000000020").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("SIN_INFORMACION"));
        mvc.perform(get("/api/antecedentes/9000000999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("SIN_INFORMACION"));
    }

    @Test
    void loginDevuelve400ParaIdentificacionYContrasenaDemasiadoLargas() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("12345678901", "Abc123", false)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("8000000001", "A".repeat(129), false)))
                .andExpect(status().isBadRequest());
    }

    private record EndpointLectura(String ruta, boolean permitePersonal) {}
    private record TokenMatriz(String nombre, String autorizacion) {}

    private MvcResult respuestaLogin(String identificacion, String contrasena) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(loginJson(identificacion, contrasena, false))).andReturn();
    }

    private String tokenPersonal() throws Exception {
        var resultado = respuestaLogin(IDENTIFICACION_PERSONAL, CONTRASENA);
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
    }

    private Usuario asegurarAdministrador() {
        return usuarios.findByIdentificacion(IDENTIFICACION_ADMIN).orElseGet(() -> {
            var perfil = perfiles.findByCodigo("ADMINISTRADOR").orElseThrow();
            var admin = new Usuario(IDENTIFICACION_ADMIN, "Administrador", "Inicial",
                    "admin-seguridad@example.test", null, null, null, passwordEncoder.encode(CONTRASENA_ADMIN));
            admin.getPerfiles().add(perfil);
            return usuarios.saveAndFlush(admin);
        });
    }

    private String token(Usuario usuario) {
        return jwt.emitir(usuario, false).valor();
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

    @TestConfiguration
    static class RelojControlado {
        @Bean
        @Primary
        MutableClock relojControlado() {
            return new MutableClock();
        }
    }
}
