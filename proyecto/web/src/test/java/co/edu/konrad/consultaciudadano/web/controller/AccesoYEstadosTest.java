package co.edu.konrad.consultaciudadano.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.konrad.consultaciudadano.web.dto.PaginaCiudadanos;
import co.edu.konrad.consultaciudadano.web.service.ApiAccesoDenegadoException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoAutorizadoException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoDisponibleException;
import co.edu.konrad.consultaciudadano.web.service.ApiValidacionException;
import co.edu.konrad.consultaciudadano.web.service.CiudadanoApiClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Control de acceso y estados de la interfaz. El cliente de ciudadanos se reemplaza por un doble
 * de Mockito para simular respuestas de la API (401, 403, red caída, lista vacía...).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class AccesoYEstadosTest {

    private static final PaginaCiudadanos VACIA = new PaginaCiudadanos(List.of(), 0, 0, 20, 0, true, true);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CiudadanoApiClient ciudadanos;

    @Test
    void sinSesionCualquierPantallaRedirigeAlLogin() throws Exception {
        for (String ruta : new String[] {"/", "/ciudadanos", "/ciudadanos/nuevo", "/reportes"}) {
            mvc.perform(get(ruta)).andExpect(redirectedUrl("/login"));
        }
    }

    @Test
    void recursosEstaticosNoExigenSesion() throws Exception {
        mvc.perform(get("/css/admin.css")).andExpect(status().isOk());
        mvc.perform(get("/js/estados.js")).andExpect(status().isOk());
    }

    @Test
    void loginSinDatosMuestraMensaje() throws Exception {
        mvc.perform(post("/login").param("identificacion", "").param("contrasena", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ingrese identificación y contraseña.")));
    }

    @Test
    void loginExitosoEntraAlPanelYMuestraElUsuario() throws Exception {
        when(ciudadanos.listar(isNull(), anyInt())).thenReturn(VACIA);
        MockHttpSession sesion = MvcConSesion.iniciarSesion(mvc);
        mvc.perform(get("/").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sesión: 1000000001")));
    }

    @Test
    void usuarioSinRolAdministradorNoPuedeEntrar() throws Exception {
        // La API responde 403 a la consulta de verificación: no es administrador.
        when(ciudadanos.listar(isNull(), anyInt())).thenThrow(new ApiAccesoDenegadoException("Acceso denegado"));
        mvc.perform(post("/login").param("identificacion", "8000000001").param("contrasena", "x"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no tiene permisos de administrador")));
    }

    @Test
    void cerrarSesionImpideVolverAEntrar() throws Exception {
        when(ciudadanos.listar(isNull(), anyInt())).thenReturn(VACIA);
        MockHttpSession sesion = MvcConSesion.iniciarSesion(mvc);
        mvc.perform(post("/logout").session(sesion)).andExpect(redirectedUrl("/login?salida"));
        mvc.perform(get("/ciudadanos")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void api401EnUnaPantallaCierraLaSesionYVuelveAlLogin() throws Exception {
        MockHttpSession sesion = MvcConSesion.iniciarSesion(mvc);
        when(ciudadanos.listar(any(), anyInt())).thenThrow(new ApiNoAutorizadoException("No autenticado"));
        mvc.perform(get("/ciudadanos").session(sesion)).andExpect(redirectedUrl("/login?expirada"));
        mvc.perform(get("/login").param("expirada", ""))
                .andExpect(content().string(containsString("Su sesión expiró")));
    }

    @Test
    void api403EnUnaPantallaVuelveAlLoginConMensaje() throws Exception {
        MockHttpSession sesion = MvcConSesion.iniciarSesion(mvc);
        when(ciudadanos.listar(any(), anyInt())).thenThrow(new ApiAccesoDenegadoException("Acceso denegado"));
        mvc.perform(get("/ciudadanos").session(sesion)).andExpect(redirectedUrl("/login?denegado"));
    }

    @Test
    void apiCaidaMuestraEstadoDeErrorConReintentar() throws Exception {
        MockHttpSession sesion = MvcConSesion.iniciarSesion(mvc);
        when(ciudadanos.listar(any(), anyInt())).thenThrow(new ApiNoDisponibleException());
        mvc.perform(get("/ciudadanos").session(sesion))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorCarga"))
                .andExpect(content().string(containsString("No fue posible conectar con el servidor")))
                .andExpect(content().string(containsString("Reintentar")));
    }

    @Test
    void apiCaidaEnElPanelMuestraElPanelSinTotal() throws Exception {
        MockHttpSession sesion = MvcConSesion.iniciarSesion(mvc);
        when(ciudadanos.listar(any(), anyInt())).thenThrow(new ApiNoDisponibleException());
        mvc.perform(get("/").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("—")))
                .andExpect(content().string(containsString("No fue posible consultar el total")));
    }

    @Test
    void apiCaidaEnEdicionMuestraPaginaDeErrorConReintentar() throws Exception {
        MockHttpSession sesion = MvcConSesion.iniciarSesion(mvc);
        when(ciudadanos.buscar(any())).thenThrow(new ApiNoDisponibleException());
        mvc.perform(get("/ciudadanos/1000000001/editar").session(sesion))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("href=\"/ciudadanos/1000000001/editar\"")));
    }

    @Test
    void listaVaciaSinBusquedaInvitaARegistrar() throws Exception {
        MockHttpSession sesion = MvcConSesion.iniciarSesion(mvc);
        when(ciudadanos.listar(any(), anyInt())).thenReturn(VACIA);
        mvc.perform(get("/ciudadanos").session(sesion))
                .andExpect(content().string(containsString("Aún no hay ciudadanos registrados.")))
                .andExpect(content().string(not(containsString("<table>"))));
    }

    @Test
    void busquedaSinResultadosLoIndica() throws Exception {
        MockHttpSession sesion = MvcConSesion.iniciarSesion(mvc);
        when(ciudadanos.listar(any(), anyInt())).thenReturn(VACIA);
        mvc.perform(get("/ciudadanos").param("busqueda", "zzz").session(sesion))
                .andExpect(content().string(containsString("No hay resultados para «zzz».")));
    }

    @Test
    void validacionDelBackendSeMuestraEnElCampo() throws Exception {
        MockHttpSession sesion = MvcConSesion.iniciarSesion(mvc);
        when(ciudadanos.registrar(any())).thenThrow(new ApiValidacionException("Datos invalidos",
                List.of("lugarNacimiento: no debe estar vacío", "campoDesconocido: x")));
        mvc.perform(registroValido().session(sesion))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("ciudadanoForm", "lugarNacimiento", "api"))
                .andExpect(content().string(containsString("El servidor rechazó los datos: Datos invalidos")))
                .andExpect(content().string(containsString("no debe estar vacío")));
    }

    @Test
    void apiCaidaAlRegistrarConservaLosDatosEscritos() throws Exception {
        MockHttpSession sesion = MvcConSesion.iniciarSesion(mvc);
        when(ciudadanos.registrar(any())).thenThrow(new ApiNoDisponibleException());
        mvc.perform(registroValido().session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Los datos no se guardaron.")))
                .andExpect(content().string(containsString("value=\"Tunja\"")));
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder registroValido() {
        return post("/ciudadanos")
                .param("identificacion", "1000000099")
                .param("nombres", "Prueba Nueva")
                .param("apellidos", "Simulado Mock")
                .param("fechaNacimiento", "1995-04-10")
                .param("lugarNacimiento", "Tunja")
                .param("fechaExpedicion", "2013-05-02")
                .param("lugarExpedicion", "Bogota")
                .param("rh", "+")
                .param("grupoSanguineo", "B")
                .param("estatura", "1.68");
    }
}
