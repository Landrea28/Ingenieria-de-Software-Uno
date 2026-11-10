package co.edu.konrad.consultaciudadano.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.edu.konrad.consultaciudadano.web.config.ApiRestConfig;
import co.edu.konrad.consultaciudadano.web.dto.CiudadanoDto;
import co.edu.konrad.consultaciudadano.web.dto.LoginResponse;
import co.edu.konrad.consultaciudadano.web.dto.PaginaCiudadanos;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Clientes REST contra un servidor HTTP simulado (MockRestServiceServer): verifica la URL, el token,
 * el JSON enviado/recibido según el contrato OpenAPI y la traducción de cada error.
 * Se levanta el contexto con el perfil "api" solo para usar el mismo ObjectMapper que la aplicación.
 */
@SpringBootTest
@ActiveProfiles("api")
class ClientesRestTest {

    @Autowired
    private ObjectMapper objectMapper;


    private static final String API = "http://api.prueba";
    private static final String ERROR_JSON = "{\"codigo\":\"%s\",\"mensaje\":\"%s\",\"detalles\":[%s]}";
    private static final String CIUDADANO_JSON = """
            {"id":7,"identificacion":"9000000099","nombres":"Clara","apellidos":"Rivera",
             "fechaNacimiento":"1995-02-14","lugarNacimiento":"Bogota","fechaExpedicion":"2013-03-22",
             "lugarExpedicion":"Bogota","rh":"+","grupoSanguineo":"B","estatura":1.71}""";

    private final AtomicReference<String> token = new AtomicReference<>("token-de-prueba");
    private MockRestServiceServer servidor;
    private CiudadanoApiClientRest ciudadanos;
    private AuthApiClientRest auth;

    @BeforeEach
    void configurar() {
        RestClient.Builder builder = ApiRestConfig.configurar(RestClient.builder(), API, token::get,
                objectMapper);
        servidor = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        ciudadanos = new CiudadanoApiClientRest(restClient);
        auth = new AuthApiClientRest(restClient);
    }

    @Test
    void listarEnviaTokenParametrosYLeeLaPagina() {
        servidor.expect(requestTo(API + "/api/ciudadanos?pagina=1&tamano=20&busqueda=P%C3%A9rez%20%26%20Co"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token-de-prueba"))
                .andRespond(withSuccess("{\"content\":[" + CIUDADANO_JSON + "],\"totalElements\":21,"
                        + "\"totalPages\":2,\"size\":20,\"number\":1,\"numberOfElements\":1,"
                        + "\"first\":false,\"last\":true,\"empty\":false}", MediaType.APPLICATION_JSON));

        PaginaCiudadanos pagina = ciudadanos.listar("  Pérez & Co ", 1);

        assertThat(pagina.totalElements()).isEqualTo(21);
        assertThat(pagina.number()).isEqualTo(1);
        assertThat(pagina.desde()).isEqualTo(21);
        CiudadanoDto c = pagina.content().get(0);
        assertThat(c.fechaNacimiento()).isEqualTo(LocalDate.of(1995, 2, 14));
        assertThat(c.estatura()).isEqualByComparingTo("1.71");
        assertThat(c.grupoSanguineo()).isEqualTo("B");
        servidor.verify();
    }

    @Test
    void sinSesionNoSeEnviaToken() {
        token.set(null);
        servidor.expect(requestTo(API + "/api/ciudadanos?pagina=0&tamano=20"))
                .andExpect(headerDoesNotExist("Authorization"))
                .andRespond(withSuccess("{\"content\":[],\"totalElements\":0,\"totalPages\":0,\"size\":20,"
                        + "\"number\":0,\"first\":true,\"last\":true}", MediaType.APPLICATION_JSON));
        assertThat(ciudadanos.listar(" ", 0).vacia()).isTrue();
    }

    @Test
    void registrarEnviaLosDiezCamposSinIdYDevuelveElCreado() {
        servidor.expect(requestTo(API + "/api/ciudadanos"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.identificacion").value("9000000099"))
                .andExpect(jsonPath("$.fechaNacimiento").value("1995-02-14"))
                .andExpect(jsonPath("$.rh").value("+"))
                .andExpect(jsonPath("$.estatura").value(1.71))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(CIUDADANO_JSON));

        CiudadanoDto creado = ciudadanos.registrar(nuevo());

        assertThat(creado.id()).isEqualTo(7L);
        servidor.verify();
    }

    @Test
    void registroDuplicadoLanzaCiudadanoDuplicado() {
        responderError(HttpStatus.CONFLICT, "DUPLICADO", "Identificacion ya registrada", "");
        assertThatThrownBy(() -> ciudadanos.registrar(nuevo())).isInstanceOf(CiudadanoDuplicadoException.class);
    }

    @Test
    void validacionDelBackendTraeMensajeYDetalles() {
        responderError(HttpStatus.BAD_REQUEST, "VALIDACION", "Datos invalidos", "\"nombres: solo letras\"");
        assertThatThrownBy(() -> ciudadanos.registrar(nuevo()))
                .isInstanceOfSatisfying(ApiValidacionException.class, e -> {
                    assertThat(e.getMessage()).isEqualTo("Datos invalidos");
                    assertThat(e.getDetalles()).containsExactly("nombres: solo letras");
                });
    }

    @Test
    void error401Y403SeTraducen() {
        responderError(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", "No autenticado", "");
        assertThatThrownBy(() -> ciudadanos.listar(null, 0)).isInstanceOf(ApiNoAutorizadoException.class);
        servidor.reset();
        responderError(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "Acceso denegado", "");
        assertThatThrownBy(() -> ciudadanos.listar(null, 0)).isInstanceOf(ApiAccesoDenegadoException.class);
    }

    @Test
    void errores5xxYCuerposNoJsonSonNoDisponible() {
        responderError(HttpStatus.SERVICE_UNAVAILABLE, "INTEGRACION_PENDIENTE", "Pendiente", "");
        assertThatThrownBy(() -> ciudadanos.listar(null, 0)).isInstanceOf(ApiNoDisponibleException.class);
        servidor.reset();
        servidor.expect(requestTo(API + "/api/ciudadanos?pagina=0&tamano=20"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY).contentType(MediaType.TEXT_HTML).body("<html>proxy</html>"));
        assertThatThrownBy(() -> ciudadanos.listar(null, 0)).isInstanceOf(ApiNoDisponibleException.class);
    }

    @Test
    void erroresDeRedSonNoDisponible() {
        servidor.expect(requestTo(API + "/api/ciudadanos?pagina=0&tamano=20"))
                .andRespond(withException(new ConnectException("Connection refused")));
        assertThatThrownBy(() -> ciudadanos.listar(null, 0)).isInstanceOf(ApiNoDisponibleException.class)
                .hasMessageContaining("No fue posible conectar");
        servidor.reset();
        servidor.expect(requestTo(API + "/api/ciudadanos?pagina=0&tamano=20"))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));
        assertThatThrownBy(() -> ciudadanos.listar(null, 0)).isInstanceOf(ApiNoDisponibleException.class);
    }

    @Test
    void actualizarHacePutConNueveCamposSinIdentificacionNiId() {
        servidor.expect(requestTo(API + "/api/ciudadanos/9000000099"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("Authorization", "Bearer token-de-prueba"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.identificacion").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.nombres").value("Clara"))
                .andExpect(jsonPath("$.fechaExpedicion").value("2013-03-22"))
                .andExpect(jsonPath("$.grupoSanguineo").value("B"))
                .andExpect(jsonPath("$.estatura").value(1.71))
                .andRespond(withSuccess(CIUDADANO_JSON, MediaType.APPLICATION_JSON));

        CiudadanoDto actualizado = ciudadanos.actualizar("9000000099", nuevo());

        assertThat(actualizado.id()).isEqualTo(7L);
        servidor.verify();
    }

    @Test
    void actualizarInexistenteLanzaNoEncontrado() {
        responderError(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", "Ciudadano no encontrado", "");
        assertThatThrownBy(() -> ciudadanos.actualizar("9000000098", nuevo()))
                .isInstanceOf(ApiNoEncontradoException.class);
    }

    @Test
    void actualizarConDatosRechazadosTraeLosDetalles() {
        responderError(HttpStatus.BAD_REQUEST, "VALIDACION", "Datos invalidos", "\"estatura: debe ser menor o igual a 2.50\"");
        assertThatThrownBy(() -> ciudadanos.actualizar("9000000099", nuevo()))
                .isInstanceOfSatisfying(ApiValidacionException.class,
                        e -> assertThat(e.getDetalles()).containsExactly("estatura: debe ser menor o igual a 2.50"));
    }

    @Test
    void buscarInexistenteDevuelveVacio() {
        servidor.expect(requestTo(API + "/api/ciudadanos/9000000098"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                        .body(ERROR_JSON.formatted("NO_ENCONTRADO", "Ciudadano no encontrado", "")));
        assertThat(ciudadanos.buscar("9000000098")).isEmpty();
    }

    @Test
    void loginEnviaCredencialesYLeeElToken() {
        token.set(null);
        servidor.expect(requestTo(API + "/api/auth/login"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(headerDoesNotExist("Authorization"))
                .andExpect(jsonPath("$.identificacion").value("1012345678"))
                .andExpect(jsonPath("$.contrasena").value("Clave123"))
                .andExpect(jsonPath("$.recordarContrasena").value(true))
                .andRespond(withSuccess("{\"token\":\"jwt\",\"tipo\":\"Bearer\",\"expiraEnSegundos\":28800}",
                        MediaType.APPLICATION_JSON));

        LoginResponse respuesta = auth.login("1012345678", "Clave123", true);

        assertThat(respuesta.token()).isEqualTo("jwt");
        assertThat(respuesta.expiraEnSegundos()).isEqualTo(28800L);
    }

    @Test
    void loginConCredencialesInvalidasLanza401() {
        token.set(null);
        servidor.expect(requestTo(API + "/api/auth/login"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
                        .body(ERROR_JSON.formatted("NO_AUTENTICADO", "Credenciales inválidas", "")));
        assertThatThrownBy(() -> auth.login("1012345678", "mala", false))
                .isInstanceOf(ApiNoAutorizadoException.class);
    }

    @Test
    void logoutEnviaElToken() {
        servidor.expect(requestTo(API + "/api/auth/logout"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer token-de-prueba"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));
        auth.logout();
        servidor.verify();
    }

    private void responderError(HttpStatus estado, String codigo, String mensaje, String detalles) {
        servidor.expect(request -> { })
                .andRespond(withStatus(estado).contentType(MediaType.APPLICATION_JSON)
                        .body(ERROR_JSON.formatted(codigo, mensaje, detalles)));
    }

    private static CiudadanoDto nuevo() {
        return new CiudadanoDto(null, "9000000099", "Clara", "Rivera", LocalDate.of(1995, 2, 14), "Bogota",
                LocalDate.of(2013, 3, 22), "Bogota", "+", "B", new BigDecimal("1.71"));
    }
}
