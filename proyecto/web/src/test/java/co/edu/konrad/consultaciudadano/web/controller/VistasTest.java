package co.edu.konrad.consultaciudadano.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Comprueba que cada pantalla del esqueleto responde y renderiza su vista con datos mock. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class VistasTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void loginMuestraFormulario() throws Exception {
        mvc.perform(get("/login"))
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
                .andExpect(model().attribute("ciudadanos", hasSize(3)));
    }

    @Test
    void busquedaFiltraPorIdentificacion() throws Exception {
        mvc.perform(get("/ciudadanos").param("busqueda", "1000000002"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ciudadanos", hasSize(1)));
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
    void reportesMuestraFiltroDeEstado() throws Exception {
        mvc.perform(get("/reportes"))
                .andExpect(status().isOk())
                .andExpect(view().name("reportes"))
                .andExpect(content().string(containsString("NO_REQUERIDO")));
    }
}
