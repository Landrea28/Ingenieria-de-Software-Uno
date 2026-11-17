package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.dto.PerfilDto;
import co.edu.konrad.consultaciudadano.web.dto.UsuarioDto;
import co.edu.konrad.consultaciudadano.web.form.ReglasUsuario;
import co.edu.konrad.consultaciudadano.web.form.UsuarioForm;
import co.edu.konrad.consultaciudadano.web.service.ApiConflictoException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoDisponibleException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoEncontradoException;
import co.edu.konrad.consultaciudadano.web.service.ApiValidacionException;
import co.edu.konrad.consultaciudadano.web.service.PerfilApiClient;
import co.edu.konrad.consultaciudadano.web.service.UsuarioApiClient;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pantallas de usuarios (HU08): listar, crear y actualizar. No hay eliminar: un usuario se desactiva.
 * La web no decide quién puede entrar aquí: la API exige el perfil ADMINISTRADOR en /api/usuarios y,
 * si responde 401/403, ManejoErroresApi cierra la sesión y vuelve al login.
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final String ID = "{identificacion:[0-9]{6,10}}";
    private static final String VISTA_FORMULARIO = "usuarios/formulario";
    static final String MSG_ID_DUPLICADA = "Ya existe un usuario con esta identificación.";
    static final String MSG_CORREO_DUPLICADO = "Este correo ya está registrado por otro usuario.";
    static final String MSG_AUTODESACTIVACION =
            "No puede desactivar su propia cuenta ni quitarse el perfil de administrador.";
    static final String MSG_ULTIMO_ADMIN = "Debe quedar al menos un administrador activo en el sistema.";

    private final UsuarioApiClient usuarios;
    private final PerfilApiClient perfiles;

    public UsuarioController(UsuarioApiClient usuarios, PerfilApiClient perfiles) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
    }

    @ModelAttribute("reglas")
    public Map<String, Map<String, Object>> reglas() {
        return ReglasUsuario.paraFormulario();
    }

    @ModelAttribute("estados")
    public Map<String, String> estados() {
        Map<String, String> estados = new LinkedHashMap<>();
        estados.put(UsuarioDto.ACTIVO, "Activo");
        estados.put(UsuarioDto.INACTIVO, "Inactivo");
        return estados;
    }

    // Los filtros se validan aquí: a la API solo llegan valores con el formato esperado.
    @GetMapping
    public String lista(@RequestParam(required = false) String estado, @RequestParam(required = false) String perfil,
            @RequestParam(defaultValue = "0") int pagina, Model model) {
        String filtroEstado = estado != null && estado.matches("ACTIVO|INACTIVO") ? estado : null;
        String filtroPerfil = perfil != null && perfil.matches("[A-Z_]{1,50}") ? perfil : null;
        model.addAttribute("filtroEstado", filtroEstado);
        model.addAttribute("filtroPerfil", filtroPerfil);
        try {
            model.addAttribute("perfiles", perfiles.perfiles());
            model.addAttribute("pagina", usuarios.listar(filtroEstado, filtroPerfil, Math.max(pagina, 0)));
        } catch (ApiNoDisponibleException e) {
            model.addAttribute("errorCarga", e.getMessage());
        }
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuarioForm", new UsuarioForm());
        return formulario(model, false);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute UsuarioForm usuarioForm, BindingResult resultado, Model model,
            RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return mostrarErrores(model, false, "Revisa los campos marcados.");
        }
        UsuarioDto creado;
        try {
            creado = usuarios.crear(usuarioForm.aDatos());
        } catch (ApiConflictoException | ApiValidacionException | ApiNoEncontradoException
                | ApiNoDisponibleException e) {
            return errorAlGuardar(e, resultado, model, false);
        }
        redirect.addFlashAttribute("mensajeExito", "Usuario creado: " + creado.nombres() + " " + creado.apellidos()
                + ", identificación " + creado.identificacion()
                + ". La contraseña temporal se envió a su correo; no se muestra en pantalla.");
        redirect.addFlashAttribute("resaltado", creado.identificacion());
        return "redirect:/usuarios";
    }

    /** Formulario de edición precargado con los datos actuales de la API. */
    @GetMapping("/" + ID + "/editar")
    public String editar(@PathVariable String identificacion, Model model, RedirectAttributes redirect) {
        return usuarios.buscar(identificacion)
                .map(u -> {
                    Long perfilId = perfiles.perfiles().stream().filter(p -> p.codigo().equals(u.perfil()))
                            .map(PerfilDto::id).findFirst().orElse(null);
                    model.addAttribute("usuarioForm", UsuarioForm.desde(u, perfilId));
                    return formulario(model, true);
                })
                .orElseGet(() -> noEncontrado(identificacion, redirect));
    }

    /** Guarda la edición: el formulario hace POST a la web y la web hace PUT /api/usuarios/{identificacion}. */
    @PostMapping("/" + ID)
    public String actualizar(@PathVariable String identificacion, @Valid @ModelAttribute UsuarioForm usuarioForm,
            BindingResult resultado, Model model, RedirectAttributes redirect) {
        // El campo es de solo lectura: si llega distinto a la URL, alguien lo alteró.
        if (!identificacion.equals(usuarioForm.getIdentificacion())) {
            redirect.addFlashAttribute("mensajeError", "La identificación de un usuario no se puede modificar.");
            return "redirect:/usuarios";
        }
        if (resultado.hasErrors()) {
            return mostrarErrores(model, true, "Revisa los campos marcados.");
        }
        UsuarioDto actualizado;
        try {
            actualizado = usuarios.actualizar(identificacion, usuarioForm.aDatos());
        } catch (ApiNoEncontradoException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Usuario")) {
                return noEncontrado(identificacion, redirect);
            }
            return errorAlGuardar(e, resultado, model, true);
        } catch (ApiConflictoException | ApiValidacionException | ApiNoDisponibleException e) {
            return errorAlGuardar(e, resultado, model, true);
        }
        redirect.addFlashAttribute("mensajeExito", "Cambios guardados para " + actualizado.nombres() + " "
                + actualizado.apellidos() + ".");
        redirect.addFlashAttribute("resaltado", actualizado.identificacion());
        return "redirect:/usuarios";
    }

    /**
     * Error de la API al crear o actualizar: se vuelve al formulario con lo escrito.
     * 409: cada código tiene su mensaje (duplicados junto al campo). 400: detalles "campo: mensaje"
     * junto a su campo. 404: el perfil ya no existe. Red o 5xx: mensaje general para reintentar.
     */
    private String errorAlGuardar(RuntimeException e, BindingResult resultado, Model model, boolean modoEdicion) {
        if (e instanceof ApiConflictoException conflicto) {
            String codigo = conflicto.getCodigo() == null ? "" : conflicto.getCodigo();
            switch (codigo) {
                case ApiConflictoException.AUTODESACTIVACION:
                    return mostrarErrores(model, modoEdicion, MSG_AUTODESACTIVACION);
                case ApiConflictoException.ULTIMO_ADMIN:
                    return mostrarErrores(model, modoEdicion, MSG_ULTIMO_ADMIN);
                default:
                    // DUPLICADO: el mensaje de la API dice si fue el correo o la identificación.
                    boolean porCorreo = String.valueOf(e.getMessage()).toLowerCase().contains("correo");
                    resultado.rejectValue(porCorreo ? "correo" : "identificacion", "duplicado",
                            porCorreo ? MSG_CORREO_DUPLICADO : MSG_ID_DUPLICADA);
                    return mostrarErrores(model, modoEdicion, "Revisa los campos marcados.");
            }
        }
        if (e instanceof ApiValidacionException validacion) {
            for (String detalle : validacion.getDetalles()) {
                int separador = detalle.indexOf(": ");
                if (separador > 0 && resultado.getFieldType(detalle.substring(0, separador)) != null
                        && !resultado.hasFieldErrors(detalle.substring(0, separador))) {
                    resultado.rejectValue(detalle.substring(0, separador), "api", detalle.substring(separador + 2));
                }
            }
            return mostrarErrores(model, modoEdicion, "El servidor rechazó los datos: " + e.getMessage());
        }
        if (e instanceof ApiNoEncontradoException) {
            resultado.rejectValue("perfilId", "api", "El perfil seleccionado ya no existe.");
            return mostrarErrores(model, modoEdicion, "Revisa los campos marcados.");
        }
        return mostrarErrores(model, modoEdicion, e.getMessage()
                + (modoEdicion ? " Los cambios no se guardaron." : " El usuario no se creó."));
    }

    private String mostrarErrores(Model model, boolean modoEdicion, String mensaje) {
        model.addAttribute("mensajeError", mensaje);
        return formulario(model, modoEdicion);
    }

    // El formulario necesita el catálogo de perfiles (valor = id, texto = nombre).
    private String formulario(Model model, boolean modoEdicion) {
        Map<Long, String> opciones = new LinkedHashMap<>();
        List<PerfilDto> catalogo = perfiles.perfiles();
        catalogo.forEach(p -> opciones.put(p.id(), p.nombre()));
        model.addAttribute("opcionesPerfil", opciones);
        model.addAttribute("modoEdicion", modoEdicion);
        return VISTA_FORMULARIO;
    }

    private static String noEncontrado(String identificacion, RedirectAttributes redirect) {
        redirect.addFlashAttribute("mensajeError",
                "No se encontró el usuario con identificación " + identificacion + ".");
        return "redirect:/usuarios";
    }
}
