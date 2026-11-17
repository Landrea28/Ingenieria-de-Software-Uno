package co.edu.konrad.consultaciudadano.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.konrad.consultaciudadano.web.dto.PerfilDto;
import co.edu.konrad.consultaciudadano.web.service.ApiAccesoDenegadoException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoAutorizadoException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoDisponibleException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoEncontradoException;
import co.edu.konrad.consultaciudadano.web.service.ApiValidacionException;
import co.edu.konrad.consultaciudadano.web.service.PerfilApiClient;
import co.edu.konrad.consultaciudadano.web.service.UsuarioApiClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Respuestas de error de la API en usuarios y perfiles (CA38: la API permite o restringe el acceso).
 * Los clientes se reemplazan por dobles de Mockito para simular 401, 403, 400, 404 y la API caída.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class SeguridadEstadosTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UsuarioApiClient usuarios;

    @MockitoBean
    private PerfilApiClient perfiles;

    private MockHttpSession sesion;

    @BeforeEach
    void iniciarSesion() throws Exception {
        sesion = MvcConSesion.iniciarSesion(mvc);
        when(perfiles.perfiles()).thenReturn(List.of(new PerfilDto(1L, "ADMINISTRADOR", "Administrador")));
    }

    @Test
    void api403EnUsuariosCierraLaSesionYVuelveAlLogin() throws Exception {
        when(usuarios.listar(any(), any(), anyInt())).thenThrow(new ApiAccesoDenegadoException("Acceso denegado"));
        mvc.perform(get("/usuarios").session(sesion)).andExpect(redirectedUrl("/login?denegado"));
        // La sesión quedó invalidada: ya no da acceso.
        mvc.perform(get("/usuarios").session(sesion)).andExpect(redirectedUrl("/login"));
    }

    @Test
    void api403AlGuardarPermisosVuelveAlLogin() throws Exception {
        when(perfiles.reemplazarPermisos(anyLong(), any())).thenThrow(new ApiAccesoDenegadoException("Acceso denegado"));
        mvc.perform(post("/perfiles/1/permisos").param("permisosIds", "7").session(sesion))
                .andExpect(redirectedUrl("/login?denegado"));
    }

    @Test
    void api401EnPerfilesVuelveAlLoginComoSesionExpirada() throws Exception {
        when(perfiles.perfiles()).thenThrow(new ApiNoAutorizadoException("No autenticado"));
        mvc.perform(get("/perfiles").session(sesion)).andExpect(redirectedUrl("/login?expirada"));
    }

    @Test
    void apiCaidaEnElListadoMuestraReintentar() throws Exception {
        when(usuarios.listar(any(), any(), anyInt())).thenThrow(new ApiNoDisponibleException());
        mvc.perform(get("/usuarios").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No fue posible conectar con el servidor")))
                .andExpect(content().string(containsString("Reintentar")));
    }

    @Test
    void apiCaidaEnPerfilesMuestraReintentar() throws Exception {
        when(perfiles.perfiles()).thenThrow(new ApiNoDisponibleException());
        mvc.perform(get("/perfiles").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reintentar")));
    }

    @Test
    void siElCorreoOLaApiFallanAlCrearSeConservaLoEscrito() throws Exception {
        // El backend responde 503 cuando no puede enviar la contraseña temporal.
        when(usuarios.crear(any())).thenThrow(new ApiNoDisponibleException());
        mvc.perform(usuarioValido().session(sesion))
                .andExpect(status().isOk())
                .andExpect(model().attribute("mensajeError", containsString("El usuario no se creó.")))
                .andExpect(content().string(containsString("value=\"laura@example.com\"")));
    }

    @Test
    void validacionDelBackendSeMuestraEnElCampo() throws Exception {
        when(usuarios.crear(any())).thenThrow(new ApiValidacionException("Datos invalidos",
                List.of("correo: debe ser una direccion valida", "campoDesconocido: x")));
        mvc.perform(usuarioValido().session(sesion))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("usuarioForm", "correo", "api"))
                .andExpect(content().string(containsString("El servidor rechazó los datos")));
    }

    @Test
    void perfilInexistenteAlCrearSeMuestraEnElCampoPerfil() throws Exception {
        when(usuarios.crear(any())).thenThrow(new ApiNoEncontradoException("Perfil no encontrado"));
        mvc.perform(usuarioValido().session(sesion))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("usuarioForm", "perfilId", "api"));
    }

    @Test
    void apiCaidaAlGuardarPermisosLoInforma() throws Exception {
        when(perfiles.reemplazarPermisos(anyLong(), any())).thenThrow(new ApiNoDisponibleException());
        mvc.perform(post("/perfiles/1/permisos").param("permisosIds", "7").session(sesion))
                .andExpect(redirectedUrl("/perfiles?perfil=1"))
                .andExpect(flash().attribute("mensajeError", containsString("Los permisos no se guardaron.")));
    }

    private static MockHttpServletRequestBuilder usuarioValido() {
        return post("/usuarios").param("identificacion", "2000000009").param("nombres", "Laura")
                .param("apellidos", "Mora").param("correo", "laura@example.com").param("perfilId", "1");
    }
}
