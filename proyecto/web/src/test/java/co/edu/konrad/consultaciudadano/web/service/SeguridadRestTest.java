package co.edu.konrad.consultaciudadano.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.edu.konrad.consultaciudadano.web.config.ApiRestConfig;
import co.edu.konrad.consultaciudadano.web.dto.PaginaUsuarios;
import co.edu.konrad.consultaciudadano.web.dto.PermisoDto;
import co.edu.konrad.consultaciudadano.web.dto.UsuarioDatos;
import co.edu.konrad.consultaciudadano.web.dto.UsuarioDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
 * Clientes REST de usuarios y perfiles contra un servidor HTTP simulado (MockRestServiceServer):
 * URL, token, JSON del contrato OpenAPI y traducción de los códigos 409.
 */
@SpringBootTest
@ActiveProfiles("api")
class SeguridadRestTest {

    private static final String API = "http://api.prueba";
    private static final String ERROR_JSON = "{\"codigo\":\"%s\",\"mensaje\":\"%s\",\"detalles\":[]}";
    private static final String USUARIO_JSON = """
            {"identificacion":"8000000001","nombres":"Laura","apellidos":"Mora",
             "correo":"laura@example.com","perfil":"PERSONAL_FUERZA_PUBLICA","estado":"ACTIVO"}""";
    private static final UsuarioDatos DATOS =
            new UsuarioDatos("8000000001", "Laura", "Mora", "laura@example.com", 2L, "INACTIVO");

    @Autowired
    private ObjectMapper objectMapper;

    private MockRestServiceServer servidor;
    private UsuarioApiClientRest usuarios;
    private PerfilApiClientRest perfiles;

    @BeforeEach
    void configurar() {
        RestClient.Builder builder = ApiRestConfig.configurar(RestClient.builder(), API, () -> "token-de-prueba",
                objectMapper);
        servidor = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        usuarios = new UsuarioApiClientRest(restClient);
        perfiles = new PerfilApiClientRest(restClient);
    }

    @Test
    void listarUsuariosEnviaTokenPaginaYFiltros() {
        servidor.expect(requestTo(API + "/api/usuarios?page=1&size=20&estado=ACTIVO&perfil=ADMINISTRADOR"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token-de-prueba"))
                .andRespond(withSuccess("{\"content\":[" + USUARIO_JSON + "],\"totalElements\":21,\"totalPages\":2,"
                        + "\"size\":20,\"number\":1,\"first\":false,\"last\":true}", MediaType.APPLICATION_JSON));

        PaginaUsuarios pagina = usuarios.listar("ACTIVO", "ADMINISTRADOR", 1);

        assertThat(pagina.desde()).isEqualTo(21);
        assertThat(pagina.content().get(0).perfil()).isEqualTo("PERSONAL_FUERZA_PUBLICA");
        assertThat(pagina.content().get(0).activo()).isTrue();
        servidor.verify();
    }

    @Test
    void listarSinFiltrosNoLosEnvia() {
        servidor.expect(requestTo(API + "/api/usuarios?page=0&size=20"))
                .andRespond(withSuccess("{\"content\":[],\"totalElements\":0,\"totalPages\":0,\"size\":20,"
                        + "\"number\":0,\"first\":true,\"last\":true}", MediaType.APPLICATION_JSON));

        assertThat(usuarios.listar(null, null, 0).vacia()).isTrue();
    }

    @Test
    void crearEnviaCincoCamposSinEstadoNiContrasena() {
        servidor.expect(requestTo(API + "/api/usuarios"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.identificacion").value("8000000001"))
                .andExpect(jsonPath("$.correo").value("laura@example.com"))
                .andExpect(jsonPath("$.perfilId").value(2))
                .andExpect(jsonPath("$.estado").doesNotExist())
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(USUARIO_JSON));

        UsuarioDto creado = usuarios.crear(DATOS);

        assertThat(creado.identificacion()).isEqualTo("8000000001");
        servidor.verify();
    }

    @Test
    void actualizarHacePutConEstadoYSinIdentificacion() {
        servidor.expect(requestTo(API + "/api/usuarios/8000000001"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.estado").value("INACTIVO"))
                .andExpect(jsonPath("$.perfilId").value(2))
                .andExpect(jsonPath("$.identificacion").doesNotExist())
                .andRespond(withSuccess(USUARIO_JSON, MediaType.APPLICATION_JSON));

        usuarios.actualizar("8000000001", DATOS);

        servidor.verify();
    }

    @Test
    void buscarUsuarioInexistenteDevuelveVacio() {
        responderError(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", "Usuario no encontrado");

        assertThat(usuarios.buscar("9999999999")).isEmpty();
    }

    @Test
    void cadaConflicto409ConservaSuCodigo() {
        for (String codigo : List.of(ApiConflictoException.DUPLICADO, ApiConflictoException.AUTODESACTIVACION,
                ApiConflictoException.ULTIMO_ADMIN)) {
            responderError(HttpStatus.CONFLICT, codigo, "Conflicto");
            assertThatThrownBy(() -> usuarios.actualizar("8000000001", DATOS))
                    .isInstanceOfSatisfying(ApiConflictoException.class, e -> assertThat(e.getCodigo()).isEqualTo(codigo));
            servidor.reset();
        }
    }

    @Test
    void correoNoDisponibleAlCrearEsApiNoDisponible() {
        responderError(HttpStatus.SERVICE_UNAVAILABLE, "INTEGRACION_PENDIENTE", "El correo SIMULADO no esta disponible");

        assertThatThrownBy(() -> usuarios.crear(DATOS)).isInstanceOf(ApiNoDisponibleException.class);
    }

    @Test
    void perfilesYPermisosSeLeenComoListas() {
        servidor.expect(requestTo(API + "/api/perfiles"))
                .andRespond(withSuccess("[{\"id\":1,\"codigo\":\"ADMINISTRADOR\",\"nombre\":\"Administrador\"}]",
                        MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(API + "/api/perfiles/1/permisos"))
                .andRespond(withSuccess("[{\"id\":7,\"codigo\":\"SEGURIDAD_ADMINISTRAR\","
                        + "\"nombre\":\"Administrar usuarios y perfiles\"}]", MediaType.APPLICATION_JSON));

        assertThat(perfiles.perfiles().get(0).administrador()).isTrue();
        PermisoDto permiso = perfiles.permisosDe(1).get(0);
        assertThat(permiso.opcion()).isEqualTo("SEGURIDAD");
        servidor.verify();
    }

    @Test
    void reemplazarPermisosHacePutConLaListaCompleta() {
        servidor.expect(requestTo(API + "/api/perfiles/2/permisos"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("Authorization", "Bearer token-de-prueba"))
                .andExpect(jsonPath("$.permisosIds.length()").value(2))
                .andExpect(jsonPath("$.permisosIds[1]").value(6))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        perfiles.reemplazarPermisos(2, List.of(4L, 6L));

        servidor.verify();
    }

    @Test
    void quitarLaGestionDeSeguridadAlAdministradorEsConflicto() {
        responderError(HttpStatus.CONFLICT, ApiConflictoException.PERMISOS_ADMIN, "Debe conservar el permiso");

        assertThatThrownBy(() -> perfiles.reemplazarPermisos(1, List.of(1L)))
                .isInstanceOfSatisfying(ApiConflictoException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo(ApiConflictoException.PERMISOS_ADMIN));
    }

    private void responderError(HttpStatus estado, String codigo, String mensaje) {
        servidor.expect(request -> { })
                .andRespond(withStatus(estado).contentType(MediaType.APPLICATION_JSON)
                        .body(ERROR_JSON.formatted(codigo, mensaje)));
    }
}
