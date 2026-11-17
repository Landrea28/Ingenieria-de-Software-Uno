package co.edu.konrad.consultaciudadano.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

/**
 * Usuarios y perfiles (HU08 y HU09, criterios CA31 a CA38) con los datos del perfil "mock", que imita
 * las reglas del backend. Los usuarios simulados son 1000000001 (administrador, el de la sesión),
 * 1000000002 (fuerza pública, activo) y 1000000003 (fuerza pública, inactivo).
 * @DirtiesContext: estas pruebas cambian los datos en memoria; no deben afectar a las demás clases.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UsuariosYPerfilesTest {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private MockMvc mvcSinSesion;

    /** Con sesión iniciada como 1000000001. */
    private MockMvc mvc;

    @BeforeEach
    void iniciarSesion() throws Exception {
        mvc = MvcConSesion.crear(contexto);
    }

    // --- Acceso ---

    @Test
    void sinSesionLasPantallasDeSeguridadRedirigenAlLogin() throws Exception {
        for (String ruta : new String[] {"/usuarios", "/usuarios/nuevo", "/usuarios/1000000001/editar", "/perfiles"}) {
            mvcSinSesion.perform(get(ruta)).andExpect(redirectedUrl("/login"));
        }
        mvcSinSesion.perform(post("/usuarios")).andExpect(redirectedUrl("/login"));
        mvcSinSesion.perform(post("/perfiles/1/permisos")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void elMenuYElPanelLlevanAUsuariosYPerfiles() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(containsString("href=\"/usuarios\"")))
                .andExpect(content().string(containsString("Usuarios y perfiles")));
    }

    // --- CA31: consulta de usuarios ---

    @Test
    void listadoMuestraLosUsuariosConPerfilYEstado() throws Exception {
        mvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/lista"))
                .andExpect(content().string(containsString("admin.mock@example.com")))
                .andExpect(content().string(containsString("Personal de fuerza publica")))
                .andExpect(content().string(containsString("Inactivo")))
                .andExpect(content().string(containsString("Crear usuario")));
    }

    @Test
    void filtroPorEstadoMuestraSoloLosInactivos() throws Exception {
        mvc.perform(get("/usuarios").param("estado", "INACTIVO"))
                .andExpect(content().string(containsString("agente2.mock@example.com")))
                .andExpect(content().string(not(containsString("admin.mock@example.com"))));
    }

    @Test
    void filtroSinCoincidenciasLoIndica() throws Exception {
        mvc.perform(get("/usuarios").param("perfil", "ADMINISTRADOR").param("estado", "INACTIVO"))
                .andExpect(content().string(containsString("No hay usuarios con ese filtro.")));
    }

    @Test
    void filtroConValorInvalidoSeIgnora() throws Exception {
        mvc.perform(get("/usuarios").param("estado", "BORRADO").param("perfil", "x' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("filtroEstado", nullValue()))
                .andExpect(model().attribute("filtroPerfil", nullValue()))
                .andExpect(content().string(containsString("admin.mock@example.com")));
    }

    // --- CA32 y CA34: creación con validación ---

    @Test
    void formularioDeCreacionNoPideContrasena() throws Exception {
        mvc.perform(get("/usuarios/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/formulario"))
                .andExpect(content().string(not(containsString("type=\"password\""))))
                .andExpect(content().string(containsString("contraseña temporal")));
    }

    @Test
    void crearUsuarioValidoConfirmaSinMostrarLaContrasena() throws Exception {
        mvc.perform(usuario("2000000001", "nuevo.uno@example.com").param("perfilId", "2"))
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attribute("mensajeExito", containsString("Usuario creado: Laura Mora")))
                .andExpect(flash().attribute("mensajeExito", containsString("contraseña temporal se envió a su correo")));
        mvc.perform(get("/usuarios")).andExpect(content().string(containsString("nuevo.uno@example.com")));
    }

    @Test
    void formularioVacioMarcaLosCincoCampos() throws Exception {
        mvc.perform(post("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/formulario"))
                .andExpect(model().attributeHasFieldErrors("usuarioForm",
                        "identificacion", "nombres", "apellidos", "correo", "perfilId"));
    }

    @Test
    void correoEIdentificacionConFormatoInvalidoSeRechazan() throws Exception {
        mvc.perform(usuario("12ab", "correo-sin-arroba").param("perfilId", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("usuarioForm", "identificacion", "correo"))
                .andExpect(content().string(containsString("Escriba un correo válido")))
                .andExpect(content().string(containsString("value=\"Laura\"")));
    }

    @Test
    void identificacionDuplicadaSeMuestraEnSuCampo() throws Exception {
        mvc.perform(usuario("1000000002", "otro.correo@example.com").param("perfilId", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("usuarioForm", "identificacion", "duplicado"))
                .andExpect(content().string(containsString(UsuarioController.MSG_ID_DUPLICADA)));
    }

    @Test
    void correoDuplicadoSeMuestraEnSuCampo() throws Exception {
        mvc.perform(usuario("2000000002", "ADMIN.mock@example.com").param("perfilId", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("usuarioForm", "correo", "duplicado"))
                .andExpect(content().string(containsString(UsuarioController.MSG_CORREO_DUPLICADO)));
    }

    // --- CA33: modificación ---

    @Test
    void edicionLlegaPrecargadaConLaIdentificacionBloqueadaYElEstado() throws Exception {
        mvc.perform(get("/usuarios/1000000002/editar"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("modoEdicion", true))
                .andExpect(content().string(containsString("agente1.mock@example.com")))
                .andExpect(content().string(containsString("readonly")))
                .andExpect(content().string(containsString("name=\"estado\"")));
    }

    @Test
    void actualizarCambiaDatosYEstado() throws Exception {
        // Usuario propio de esta prueba, para no depender del orden de ejecución.
        mvc.perform(usuario("2000000005", "por.editar@example.com").param("perfilId", "2"))
                .andExpect(redirectedUrl("/usuarios"));

        mvc.perform(post("/usuarios/2000000005").param("identificacion", "2000000005")
                        .param("nombres", "Laura Editada").param("apellidos", "Mora")
                        .param("correo", "por.editar@example.com").param("perfilId", "2").param("estado", "INACTIVO"))
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attribute("mensajeExito", containsString("Cambios guardados para Laura Editada")));

        mvc.perform(get("/usuarios").param("estado", "INACTIVO").param("perfil", "PERSONAL_FUERZA_PUBLICA"))
                .andExpect(content().string(containsString("Laura Editada")));
    }

    @Test
    void laIdentificacionNoSePuedeCambiarAlEditar() throws Exception {
        mvc.perform(post("/usuarios/1000000003").param("identificacion", "1000000002")
                        .param("nombres", "X").param("apellidos", "Y").param("correo", "x@example.com")
                        .param("perfilId", "2").param("estado", "ACTIVO"))
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attribute("mensajeError", containsString("no se puede modificar")));
    }

    @Test
    void usuarioInexistenteVuelveAlListadoConError() throws Exception {
        mvc.perform(get("/usuarios/9999999999/editar"))
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attribute("mensajeError", containsString("9999999999")));
    }

    @Test
    void elAdministradorNoPuedeDesactivarseASiMismo() throws Exception {
        mvc.perform(admin().param("perfilId", "1").param("estado", "INACTIVO"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("mensajeError", UsuarioController.MSG_AUTODESACTIVACION));
    }

    @Test
    void noSePuedeDejarElSistemaSinAdministradorActivo() throws Exception {
        // Otro usuario en sesión intenta quitarle el perfil al único administrador activo.
        MockHttpSession otra = (MockHttpSession) mvcSinSesion.perform(post("/login")
                        .param("identificacion", "1000000009").param("contrasena", "clave-de-prueba"))
                .andReturn().getRequest().getSession(false);
        mvcSinSesion.perform(admin().param("perfilId", "2").param("estado", "ACTIVO").session(otra))
                .andExpect(status().isOk())
                .andExpect(model().attribute("mensajeError", UsuarioController.MSG_ULTIMO_ADMIN));
    }

    // --- CA36 y CA37: opciones y asignación de permisos ---

    @Test
    void perfilesMuestraLasOpcionesDelSistemaConSusPermisos() throws Exception {
        mvc.perform(get("/perfiles"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfiles"))
                .andExpect(content().string(containsString("Gestión de ciudadanos")))
                .andExpect(content().string(containsString("Consulta de antecedentes")))
                .andExpect(content().string(containsString("Reportes de ciudadanos")))
                .andExpect(content().string(containsString("Gestión de seguridad")))
                .andExpect(content().string(containsString("obligatorio para este perfil")));
    }

    @Test
    void elPerfilDeFuerzaPublicaSoloTieneConsultarAntecedentes() throws Exception {
        mvc.perform(get("/perfiles").param("perfil", "2"))
                .andExpect(model().attribute("asignados", Set.of(4L)))
                .andExpect(content().string(not(containsString("obligatorio para este perfil"))));
    }

    @Test
    void guardarPermisosReemplazaLaAsignacionDelPerfil() throws Exception {
        mvc.perform(post("/perfiles/2/permisos").param("permisosIds", "4", "6"))
                .andExpect(redirectedUrl("/perfiles?perfil=2"))
                .andExpect(flash().attribute("mensajeExito", containsString("2 permisos")));
        mvc.perform(get("/perfiles").param("perfil", "2")).andExpect(model().attribute("asignados", Set.of(4L, 6L)));
        // Se deja como estaba.
        mvc.perform(post("/perfiles/2/permisos").param("permisosIds", "4"))
                .andExpect(flash().attribute("mensajeExito", containsString("1 permiso.")));
    }

    @Test
    void elPerfilAdministradorNoPuedePerderLaGestionDeSeguridad() throws Exception {
        mvc.perform(post("/perfiles/1/permisos").param("permisosIds", "1", "2"))
                .andExpect(redirectedUrl("/perfiles?perfil=1"))
                .andExpect(flash().attribute("mensajeError", containsString("Administrar usuarios y perfiles")));
        mvc.perform(get("/perfiles").param("perfil", "1"))
                .andExpect(model().attribute("asignados", Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L)));
    }

    @Test
    void guardarPermisosDeUnPerfilInexistenteLoInforma() throws Exception {
        mvc.perform(post("/perfiles/99/permisos").param("permisosIds", "4"))
                .andExpect(redirectedUrl("/perfiles?perfil=99"))
                .andExpect(flash().attribute("mensajeError", containsString("ya no existe")));
    }

    private static MockHttpServletRequestBuilder usuario(String identificacion, String correo) {
        return post("/usuarios").param("identificacion", identificacion).param("nombres", "Laura")
                .param("apellidos", "Mora").param("correo", correo);
    }

    /** Edición del administrador simulado (1000000001). */
    private static MockHttpServletRequestBuilder admin() {
        return post("/usuarios/1000000001").param("identificacion", "1000000001").param("nombres", "Admin")
                .param("apellidos", "Simulado Mock").param("correo", "admin.mock@example.com");
    }
}
