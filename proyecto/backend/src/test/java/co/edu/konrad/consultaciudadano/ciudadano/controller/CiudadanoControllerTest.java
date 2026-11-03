package co.edu.konrad.consultaciudadano.ciudadano.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import co.edu.konrad.consultaciudadano.ciudadano.service.CiudadanoService;
import co.edu.konrad.consultaciudadano.auth.service.FiltroJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CiudadanoController.class)
@AutoConfigureMockMvc(addFilters = false)
class CiudadanoControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean CiudadanoService service;
    @MockitoBean FiltroJwt filtroJwt;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void validaCamposObligatoriosAntesDeService() throws Exception {
        mvc.perform(post("/api/ciudadanos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));
    }
}
