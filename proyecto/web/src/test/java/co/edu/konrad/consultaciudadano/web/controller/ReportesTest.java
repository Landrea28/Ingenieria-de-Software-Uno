package co.edu.konrad.consultaciudadano.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

/** Pantalla de reportes con el perfil "mock": REQUERIDO tiene datos y NO_REQUERIDO no. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class ReportesTest {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private MockMvc mvcSinSesion;

    private MockMvc mvc;

    @BeforeEach
    void iniciarSesion() throws Exception {
        mvc = MvcConSesion.crear(contexto);
    }

    @Test
    void pantallaTieneElFiltroYLosBotonesActivos() throws Exception {
        mvc.perform(get("/reportes"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("value=\"REQUERIDO\" required checked"),
                        containsString("value=\"NO_REQUERIDO\""),
                        containsString("No requerido"),
                        containsString("value=\"ver\""),
                        containsString("value=\"descargar\""),
                        not(containsString("disabled")))));
    }

    @Test
    void descargarDevuelveElPdfComoAdjuntoSinCache() throws Exception {
        byte[] pdf = mvc.perform(get("/reportes/pdf").param("estado", "REQUERIDO").param("modo", "descargar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"ciudadanos-requerido-" + LocalDate.now() + ".pdf\""))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andReturn().getResponse().getContentAsByteArray();

        String texto = new String(pdf, StandardCharsets.ISO_8859_1);
        assertThat(texto).startsWith("%PDF-1.4").endsWith("%%EOF\n").contains("Prueba Dos", "DATOS SIMULADOS");
        verificarTablaXref(texto);
    }

    @Test
    void verAbreElPdfEnElNavegador() throws Exception {
        mvc.perform(get("/reportes/pdf").param("estado", "REQUERIDO").param("modo", "ver"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("inline;")));
    }

    @Test
    void sinResultadosVuelveConMensajeClaro() throws Exception {
        mvc.perform(get("/reportes/pdf").param("estado", "NO_REQUERIDO"))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("http://localhost/reportes?estado=NO_REQUERIDO&resultado=vacio"));
        mvc.perform(get("/reportes").param("estado", "NO_REQUERIDO").param("resultado", "vacio"))
                .andExpect(content().string(allOf(
                        containsString("No hay ciudadanos con estado «No requerido». No se generó el reporte."),
                        containsString("value=\"NO_REQUERIDO\" required checked"))));
    }

    @Test
    void estadoInvalidoOAusentePideElegirUnEstado() throws Exception {
        for (String estado : new String[] {"", "TODOS"}) {
            mvc.perform(get("/reportes/pdf").param("estado", estado))
                    .andExpect(redirectedUrl("http://localhost/reportes?resultado=estado"));
        }
        mvc.perform(get("/reportes").param("resultado", "estado"))
                .andExpect(content().string(containsString("Seleccione un estado")));
    }

    @Test
    void sinSesionNoSePuedePedirElReporte() throws Exception {
        mvcSinSesion.perform(get("/reportes/pdf").param("estado", "REQUERIDO"))
                .andExpect(redirectedUrl("/login"));
    }

    /** Cada entrada de la tabla xref debe apuntar exactamente al inicio de "N 0 obj". */
    private static void verificarTablaXref(String pdf) {
        int inicioXref = Integer.parseInt(pdf.substring(pdf.lastIndexOf("startxref\n") + 10, pdf.lastIndexOf("\n%%EOF")));
        assertThat(pdf.substring(inicioXref)).startsWith("xref\n");
        Matcher entradas = Pattern.compile("(\\d{10}) 00000 n \n").matcher(pdf.substring(inicioXref));
        int objeto = 1;
        while (entradas.find()) {
            int posicion = Integer.parseInt(entradas.group(1));
            assertThat(pdf.substring(posicion)).startsWith(objeto + " 0 obj\n");
            objeto++;
        }
        assertThat(objeto - 1).isEqualTo(5);
    }
}
