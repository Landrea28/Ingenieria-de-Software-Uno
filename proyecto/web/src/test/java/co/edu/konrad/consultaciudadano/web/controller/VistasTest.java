package co.edu.konrad.consultaciudadano.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

/** Comprueba que cada pantalla responde y renderiza su vista con datos mock (con sesión iniciada). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class VistasTest {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private MockMvc mvcSinSesion;

    /** Con sesión de administrador iniciada (login simulado). */
    private MockMvc mvc;

    @BeforeEach
    void iniciarSesion() throws Exception {
        mvc = MvcConSesion.crear(contexto);
    }

    @Test
    void loginMuestraFormulario() throws Exception {
        mvcSinSesion.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void panelMuestraAvisoDeDatosSimulados() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("panel"))
                .andExpect(content().string(containsString("DATOS SIMULADOS")));
    }

    @Test
    void listadoMuestraCiudadanosMock() throws Exception {
        mvc.perform(get("/ciudadanos"))
                .andExpect(status().isOk())
                .andExpect(view().name("ciudadanos/lista"))
                .andExpect(content().string(containsString("Mostrando 1–3 de 3")))
                .andExpect(content().string(containsString("Prueba Dos")));
    }

    @Test
    void busquedaFiltraPorIdentificacion() throws Exception {
        mvc.perform(get("/ciudadanos").param("busqueda", "1000000002"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mostrando 1–1 de 1")));
    }

    @Test
    void formularioDeRegistro() throws Exception {
        mvc.perform(get("/ciudadanos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("ciudadanos/formulario"))
                .andExpect(model().attribute("modoEdicion", false));
    }

    @Test
    void formularioDeEdicionCargaElCiudadano() throws Exception {
        mvc.perform(get("/ciudadanos/1000000001/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("ciudadanos/formulario"))
                .andExpect(content().string(containsString("Prueba Uno")));
    }

    @Test
    void edicionDeIdentificacionInexistenteVuelveAlListadoConError() throws Exception {
        mvc.perform(get("/ciudadanos/9999999999/editar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ciudadanos"))
                .andExpect(flash().attribute("mensajeError", containsString("9999999999")));
    }

    @Test
    void valorInvalidoMuestraMensajeDeValidacionEnElCampo() throws Exception {
        mvc.perform(post("/ciudadanos").param("estatura", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("ciudadanos/formulario"))
                .andExpect(model().attributeHasFieldErrors("ciudadanoForm", "estatura"))
                .andExpect(content().string(containsString("Debe ser un número")))
                .andExpect(content().string(containsString("Revisa los campos marcados")));
    }

    @Test
    void actualizarRechazaIdentificacionAlterada() throws Exception {
        mvc.perform(post("/ciudadanos/1000000001").param("identificacion", "1000000002"))
                .andExpect(redirectedUrl("/ciudadanos"))
                .andExpect(flash().attribute("mensajeError", containsString("no se puede modificar")));
    }

    @Test
    void actualizarConErroresVuelveAlFormularioDeEdicion() throws Exception {
        mvc.perform(post("/ciudadanos/1000000001").param("identificacion", "1000000001").param("estatura", "x"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("modoEdicion", true))
                .andExpect(content().string(containsString("value=\"1000000001\"")));
    }

    @Test
    void cerrarSesionMuestraMensajeEnLogin() throws Exception {
        mvc.perform(post("/logout"))
                .andExpect(redirectedUrl("/login?salida"));
        mvcSinSesion.perform(get("/login").param("salida", ""))
                .andExpect(content().string(containsString("Sesión cerrada")));
    }

    @Test
    void reportesMuestraFiltroDeEstado() throws Exception {
        mvc.perform(get("/reportes"))
                .andExpect(status().isOk())
                .andExpect(view().name("reportes"))
                .andExpect(content().string(containsString("NO_REQUERIDO")));
    }
}
