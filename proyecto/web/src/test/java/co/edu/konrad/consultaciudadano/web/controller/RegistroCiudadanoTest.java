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

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Registro de ciudadano con el mock. @DirtiesContext reinicia el contexto después de cada
 * prueba porque registrar agrega ciudadanos a la lista en memoria.
 */
@SpringBootTest
@ActiveProfiles("mock")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RegistroCiudadanoTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    @BeforeEach
    void iniciarSesion() throws Exception {
        mvc = MvcConSesion.crear(contexto);
    }

    /** Los 10 campos con valores válidos y ficticios. */
    private static MockHttpServletRequestBuilder registroValido() {
        return registroCon(Map.of());
    }

    /** Registro válido con algunos campos reemplazados (.param() agregaría un segundo valor). */
    private static MockHttpServletRequestBuilder registroCon(String campo, String valor) {
        return registroCon(Map.of(campo, valor));
    }

    private static MockHttpServletRequestBuilder registroCon(Map<String, String> reemplazos) {
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("identificacion", "1000000099");
        datos.put("nombres", "Prueba Nueva");
        datos.put("apellidos", "Simulado Mock");
        datos.put("fechaNacimiento", "1995-04-10");
        datos.put("lugarNacimiento", "Tunja");
        datos.put("fechaExpedicion", "2013-05-02");
        datos.put("lugarExpedicion", "Bogota");
        datos.put("rh", "+");
        datos.put("grupoSanguineo", "B");
        datos.put("estatura", "1.68");
        datos.putAll(reemplazos);
        MockHttpServletRequestBuilder peticion = post("/ciudadanos");
        datos.forEach(peticion::param);
        return peticion;
    }

    @Test
    void registroValidoConfirmaYApareceEnElListado() throws Exception {
        mvc.perform(registroValido())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ciudadanos?busqueda=1000000099"))
                .andExpect(flash().attribute("mensajeExito",
                        allOf(containsString("Ciudadano registrado"), containsString("1000000099"),
                                containsString("SIMULADO"))))
                .andExpect(flash().attribute("resaltado", "1000000099"));

        mvc.perform(get("/ciudadanos"))
                .andExpect(content().string(containsString("Mostrando 1–4 de 4")))
                .andExpect(content().string(containsString("Prueba Nueva")));
    }

    @Test
    void identificacionDuplicadaMuestraErrorEnElCampo() throws Exception {
        mvc.perform(registroCon("identificacion", "1000000001"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("ciudadanoForm", "identificacion", "duplicado"))
                .andExpect(content().string(containsString("Ya existe un ciudadano con esta identificación.")));
    }

    @Test
    void formularioVacioMarcaLosDiezCamposObligatorios() throws Exception {
        mvc.perform(post("/ciudadanos"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("ciudadanoForm",
                        "identificacion", "nombres", "apellidos", "fechaNacimiento", "lugarNacimiento",
                        "fechaExpedicion", "lugarExpedicion", "rh", "grupoSanguineo", "estatura"))
                .andExpect(content().string(containsString("La identificación es obligatoria.")));
    }

    @Test
    void identificacionConLetrasOPuntosEsRechazada() throws Exception {
        for (String invalida : new String[] {"12ab56", "1.012.345", "12345", "12345678901"}) {
            mvc.perform(registroCon("identificacion", invalida))
                    .andExpect(model().attributeHasFieldErrors("ciudadanoForm", "identificacion"));
        }
    }

    @Test
    void nombresConNumerosSonRechazados() throws Exception {
        mvc.perform(registroCon("nombres", "Prueba 2"))
                .andExpect(model().attributeHasFieldErrors("ciudadanoForm", "nombres"));
    }

    @Test
    void fechaFuturaEsRechazada() throws Exception {
        String manana = LocalDate.now().plusDays(1).toString();
        mvc.perform(registroCon("fechaNacimiento", manana))
                .andExpect(model().attributeHasFieldErrors("ciudadanoForm", "fechaNacimiento"))
                .andExpect(content().string(containsString("No puede ser una fecha futura.")));
    }

    @Test
    void expedicionAnteriorAlNacimientoEsRechazada() throws Exception {
        mvc.perform(registroCon("fechaExpedicion", "1990-01-01"))
                .andExpect(model().attributeHasFieldErrorCode("ciudadanoForm", "fechaExpedicion", "fechas"))
                .andExpect(content().string(containsString("Debe ser posterior a la fecha de nacimiento.")));
    }

    @Test
    void estaturaFueraDeRangoOConTresDecimalesEsRechazada() throws Exception {
        for (String invalida : new String[] {"0.30", "2.80", "1.705"}) {
            mvc.perform(registroCon("estatura", invalida))
                    .andExpect(model().attributeHasFieldErrors("ciudadanoForm", "estatura"));
        }
    }

    @Test
    void catalogoFueraDeLaListaEsRechazado() throws Exception {
        mvc.perform(registroCon(Map.of("rh", "x", "grupoSanguineo", "C")))
                .andExpect(model().attributeHasFieldErrors("ciudadanoForm", "rh", "grupoSanguineo"));
    }

    @Test
    void formularioIncluyeReglasParaElNavegador() throws Exception {
        String hoy = LocalDate.now().toString();
        mvc.perform(get("/ciudadanos/nuevo"))
                .andExpect(content().string(allOf(
                        containsString("data-validar"),
                        containsString("pattern=\"[0-9]{6,10}\""),
                        containsString("inputmode=\"numeric\""),
                        containsString("type=\"date\""),
                        containsString("max=\"" + hoy + "\""),
                        containsString("data-despues-de=\"fechaNacimiento\""),
                        containsString("min=\"0.50\""),
                        containsString("step=\"0.01\""),
                        containsString("<option value=\"AB\">AB</option>"),
                        containsString("<option value=\"-\">-</option>"),
                        containsString("/js/validacion.js"))));
    }
}
