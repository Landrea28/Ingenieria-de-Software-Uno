package co.edu.konrad.consultaciudadano.ciudadano.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import co.edu.konrad.consultaciudadano.ciudadano.service.CiudadanoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CiudadanoController.class)
class CiudadanoControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean CiudadanoService service;

    @Test
    void validaCamposObligatoriosAntesDeService() throws Exception {
        mvc.perform(post("/api/ciudadanos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));
    }
}
