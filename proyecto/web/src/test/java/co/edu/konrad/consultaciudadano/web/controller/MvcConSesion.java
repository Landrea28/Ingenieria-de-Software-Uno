package co.edu.konrad.consultaciudadano.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Para pruebas con el perfil "mock": inicia sesión y devuelve un MockMvc que envía esa sesión siempre. */
final class MvcConSesion {

    private MvcConSesion() {}

    static MockHttpSession iniciarSesion(MockMvc mvc) throws Exception {
        return (MockHttpSession) mvc.perform(post("/login")
                        .param("identificacion", "1000000001")
                        .param("contrasena", "clave-de-prueba"))
                .andExpect(redirectedUrl("/"))
                .andReturn().getRequest().getSession(false);
    }

    static MockMvc crear(WebApplicationContext contexto) throws Exception {
        MockHttpSession sesion = iniciarSesion(MockMvcBuilders.webAppContextSetup(contexto).build());
        return MockMvcBuilders.webAppContextSetup(contexto).defaultRequest(get("/").session(sesion)).build();
    }
}
