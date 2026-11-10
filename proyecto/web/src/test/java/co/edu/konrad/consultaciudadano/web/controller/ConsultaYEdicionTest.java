package co.edu.konrad.consultaciudadano.web.controller;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

/** Consulta por identificación, detalle y edición con el mock (la edición modifica la lista en memoria). */
@SpringBootTest
@ActiveProfiles("mock")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ConsultaYEdicionTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    @BeforeEach
    void iniciarSesion() throws Exception {
        mvc = MvcConSesion.crear(contexto);
    }

    @Test
    void consultaPorIdentificacionVaAlDetalleAunqueTengaPuntosOEspacios() throws Exception {
        mvc.perform(get("/ciudadanos/consulta").param("identificacion", " 1.000.000.002 "))
                .andExpect(redirectedUrl("/ciudadanos/1000000002"));
    }

    @Test
    void consultaConIdentificacionInvalidaVuelveAlListadoConError() throws Exception {
        mvc.perform(get("/ciudadanos/consulta").param("identificacion", "12ab"))
                .andExpect(redirectedUrl("/ciudadanos"))
                .andExpect(flash().attribute("mensajeError", containsString("Identificación inválida")));
    }

    @Test
    void detalleMuestraLosDiezCampos() throws Exception {
        mvc.perform(get("/ciudadanos/1000000002"))
                .andExpect(status().isOk())
                .andExpect(view().name("ciudadanos/detalle"))
                .andExpect(content().string(allOf(
                        containsString("1000000002"), containsString("Prueba Dos"), containsString("Simulado Mock"),
                        containsString("03/06/1985"), containsString("Medellin"), containsString("10/07/2003"),
                        containsString("<dd>A</dd>"), containsString("<dd>-</dd>"), containsString("1.62 m"),
                        containsString("href=\"/ciudadanos/1000000002/editar\""))));
    }

    @Test
    void detalleDeIdentificacionInexistenteVuelveAlListado() throws Exception {
        mvc.perform(get("/ciudadanos/9999999999"))
                .andExpect(redirectedUrl("/ciudadanos"))
                .andExpect(flash().attribute("mensajeError", containsString("No se encontró")));
    }

    @Test
    void rutaConIdentificacionNoNumericaEs404ConPaginaPropia() throws Exception {
        mvc.perform(get("/ciudadanos/abc"))
                .andExpect(status().isNotFound());
    }

    @Test
    void edicionValidaGuardaYConfirmaEnElDetalle() throws Exception {
        mvc.perform(edicion(Map.of("nombres", "Nombre Editado", "estatura", "1.75")))
                .andExpect(redirectedUrl("/ciudadanos/1000000001"))
                .andExpect(flash().attribute("mensajeExito", allOf(
                        containsString("Cambios guardados para Nombre Editado"), containsString("SIMULADO"))));

        mvc.perform(get("/ciudadanos/1000000001"))
                .andExpect(content().string(allOf(containsString("Nombre Editado"), containsString("1.75 m"))));
    }

    @Test
    void edicionInvalidaNoGuardaYMuestraLosErrores() throws Exception {
        mvc.perform(edicion(Map.of("nombres", "", "fechaExpedicion", "1980-01-01")))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("ciudadanoForm", "nombres", "fechaExpedicion"))
                .andExpect(model().attribute("modoEdicion", true));

        mvc.perform(get("/ciudadanos/1000000001"))
                .andExpect(content().string(containsString("Prueba Uno")));
    }

    @Test
    void cancelarEnEdicionVuelveAlDetalle() throws Exception {
        mvc.perform(get("/ciudadanos/1000000001/editar"))
                .andExpect(content().string(containsString("href=\"/ciudadanos/1000000001\" class=\"boton\">Cancelar")));
    }

    @Test
    void panelYListadoTienenLaConsultaPorIdentificacion() throws Exception {
        for (String ruta : new String[] {"/", "/ciudadanos"}) {
            mvc.perform(get(ruta))
                    .andExpect(content().string(containsString("action=\"/ciudadanos/consulta\"")));
        }
    }

    /** Edición del ciudadano 1000000001 (datos del mock) con algunos campos reemplazados. */
    private static MockHttpServletRequestBuilder edicion(Map<String, String> reemplazos) {
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("identificacion", "1000000001");
        datos.put("nombres", "Prueba Uno");
        datos.put("apellidos", "Simulado Mock");
        datos.put("fechaNacimiento", "1990-01-15");
        datos.put("lugarNacimiento", "Bogota");
        datos.put("fechaExpedicion", "2008-02-20");
        datos.put("lugarExpedicion", "Bogota");
        datos.put("rh", "+");
        datos.put("grupoSanguineo", "O");
        datos.put("estatura", "1.70");
        datos.putAll(reemplazos);
        MockHttpServletRequestBuilder peticion = post("/ciudadanos/1000000001");
        datos.forEach(peticion::param);
        return peticion;
    }
}
