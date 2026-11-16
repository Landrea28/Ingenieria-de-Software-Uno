package co.edu.konrad.consultaciudadano.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

/** Pruebas de regresión de los defectos encontrados en la revisión de cierre del módulo. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class DefectosCorregidosTest {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private MockMvc mvcSinSesion;

    private MockMvc mvc;

    @BeforeEach
    void iniciarSesion() throws Exception {
        mvc = MvcConSesion.crear(contexto);
    }

    /** Defecto 1: ?pagina=99 decía "Aún no hay ciudadanos registrados" aunque sí había. */
    @Test
    void paginaFueraDeRangoNoDiceQueNoHayCiudadanos() throws Exception {
        mvc.perform(get("/ciudadanos").param("pagina", "99"))
                .andExpect(content().string(containsString("La página solicitada no existe.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Aún no hay ciudadanos"))));
    }

    /** Defecto 2: tras cerrar sesión, "Atrás" mostraba datos de ciudadanos guardados en caché. */
    @Test
    void pantallasConDatosNoSeGuardanEnCache() throws Exception {
        for (String ruta : new String[] {"/", "/ciudadanos", "/ciudadanos/1000000001", "/ciudadanos/1000000001/editar"}) {
            mvc.perform(get(ruta)).andExpect(header().string("Cache-Control", "no-store"));
        }
        mvc.perform(get("/reportes/pdf").param("estado", "REQUERIDO"))
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void recursosEstaticosSiPuedenGuardarseEnCache() throws Exception {
        mvcSinSesion.perform(get("/css/admin.css"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Cache-Control"));
    }

    /** Defecto 3: otra página podía incrustar el panel en un iframe (clickjacking). */
    @Test
    void cabecerasDeSeguridadEnTodasLasPantallas() throws Exception {
        for (MockMvc cliente : new MockMvc[] {mvcSinSesion, mvc}) {
            String ruta = cliente == mvc ? "/ciudadanos" : "/login";
            cliente.perform(get(ruta))
                    .andExpect(header().string("X-Frame-Options", "DENY"))
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                    .andExpect(header().string("Referrer-Policy", "same-origin"))
                    .andExpect(header().string("Content-Security-Policy", containsString("frame-ancestors 'none'")))
                    .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")));
        }
    }

    @Test
    void elPdfNoLlevaCspParaQueElVisorDelNavegadorFuncione() throws Exception {
        mvc.perform(get("/reportes/pdf").param("estado", "REQUERIDO").param("modo", "ver"))
                .andExpect(header().doesNotExist("Content-Security-Policy"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    /** Defecto 4: la sesión web expiraba a los 30 min aunque el token ("Recordar") durara 8 h. */
    @Test
    void laSesionWebDuraLoMismoQueElToken() throws Exception {
        MockHttpSession sesion = MvcConSesion.iniciarSesion(mvcSinSesion);
        // El login simulado entrega un token de 8 horas.
        assertThat(sesion.getMaxInactiveInterval()).isBetween(8 * 3600 - 60, 8 * 3600);
    }
}
