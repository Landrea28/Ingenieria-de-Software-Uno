package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.dto.CiudadanoDto;
import co.edu.konrad.consultaciudadano.web.form.CiudadanoForm;
import co.edu.konrad.consultaciudadano.web.form.ReglasCiudadano;
import co.edu.konrad.consultaciudadano.web.service.ApiNoDisponibleException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoEncontradoException;
import co.edu.konrad.consultaciudadano.web.service.ApiValidacionException;
import co.edu.konrad.consultaciudadano.web.service.CiudadanoApiClient;
import co.edu.konrad.consultaciudadano.web.service.CiudadanoDuplicadoException;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
 * Pantallas de ciudadanos. Las rutas con {identificacion} solo aceptan 6 a 10 dígitos
 * (ID = expresión regular), así no se confunden con /nuevo ni /consulta.
 */
@Controller
@RequestMapping("/ciudadanos")
public class CiudadanoController {

    private static final String ID = "{identificacion:[0-9]{6,10}}";
    private static final String VISTA_FORMULARIO = "ciudadanos/formulario";

    private final CiudadanoApiClient ciudadanos;

    public CiudadanoController(CiudadanoApiClient ciudadanos) {
        this.ciudadanos = ciudadanos;
    }

    // Disponibles en las vistas de este controlador: atributos HTML de cada campo y catálogos.
    @ModelAttribute("reglas")
    public Map<String, Map<String, Object>> reglas() {
        return ReglasCiudadano.paraFormulario(LocalDate.now());
    }

    @ModelAttribute("opcionesRh")
    public List<String> opcionesRh() {
        return ReglasCiudadano.RH;
    }

    @ModelAttribute("gruposSanguineos")
    public List<String> gruposSanguineos() {
        return ReglasCiudadano.GRUPOS_SANGUINEOS;
    }

    // Estados de la vista: 'pagina' con datos o vacía, o 'errorCarga' si la API no respondió.
    // 401/403 no se atrapan aquí: los maneja ManejoErroresApi (vuelta al login).
    @GetMapping
    public String lista(@RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("busqueda", busqueda);
        try {
            model.addAttribute("pagina", ciudadanos.listar(busqueda, Math.max(pagina, 0)));
        } catch (ApiNoDisponibleException e) {
            model.addAttribute("errorCarga", e.getMessage());
        }
        return "ciudadanos/lista";
    }

    /**
     * Búsqueda exacta por identificación: va directo al detalle. Acepta "1.012.345.678" o con
     * espacios, como suele escribirse la cédula.
     */
    @GetMapping("/consulta")
    public String consultar(@RequestParam(defaultValue = "") String identificacion, RedirectAttributes redirect) {
        String limpia = identificacion.replaceAll("[.\\s]", "");
        if (!limpia.matches(ReglasCiudadano.PATRON_IDENTIFICACION)) {
            redirect.addFlashAttribute("mensajeError", "Identificación inválida: " + ReglasCiudadano.MSG_IDENTIFICACION);
            return "redirect:/ciudadanos";
        }
        // Seguro concatenar: 'limpia' ya se validó como solo dígitos.
        return "redirect:/ciudadanos/" + limpia;
    }

    /** Detalle de un ciudadano: GET /api/ciudadanos/{identificacion}. */
    @GetMapping("/" + ID)
    public String detalle(@PathVariable String identificacion, Model model, RedirectAttributes redirect) {
        return ciudadanos.buscar(identificacion)
                .map(c -> {
                    model.addAttribute("ciudadano", c);
                    return "ciudadanos/detalle";
                })
                .orElseGet(() -> noEncontrado(identificacion, redirect));
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("ciudadanoForm", new CiudadanoForm());
        model.addAttribute("modoEdicion", false);
        return VISTA_FORMULARIO;
    }

    // @Valid aplica las reglas de CiudadanoForm. BindingResult debe ir justo después del
    // formulario: recoge los errores (de validación o de conversión, p. ej. estatura "abc").
    @PostMapping
    public String crear(@Valid @ModelAttribute CiudadanoForm ciudadanoForm, BindingResult resultado,
            Model model, RedirectAttributes redirect) {
        validarFechas(ciudadanoForm, resultado);
        if (resultado.hasErrors()) {
            return mostrarErrores(model, false, "Revisa los campos marcados.");
        }
        CiudadanoDto creado;
        try {
            creado = ciudadanos.registrar(ciudadanoForm.aDto());
        } catch (CiudadanoDuplicadoException e) {
            resultado.rejectValue("identificacion", "duplicado", "Ya existe un ciudadano con esta identificación.");
            return mostrarErrores(model, false, "Revisa los campos marcados.");
        } catch (ApiValidacionException | ApiNoDisponibleException e) {
            return errorAlGuardar(e, resultado, model, false);
        }
        redirect.addFlashAttribute("mensajeExito", "Ciudadano registrado: " + creado.nombres() + " "
                + creado.apellidos() + ", identificación " + creado.identificacion() + sufijoSimulado());
        redirect.addFlashAttribute("resaltado", creado.identificacion());
        // El listado se ordena por apellidos: se filtra por la identificación para que la fila nueva se vea.
        redirect.addAttribute("busqueda", creado.identificacion());
        return "redirect:/ciudadanos";
    }

    /** Formulario de edición precargado con los datos actuales de la API. */
    @GetMapping("/" + ID + "/editar")
    public String editar(@PathVariable String identificacion, Model model, RedirectAttributes redirect) {
        return ciudadanos.buscar(identificacion)
                .map(c -> {
                    model.addAttribute("ciudadanoForm", CiudadanoForm.desde(c));
                    model.addAttribute("modoEdicion", true);
                    return VISTA_FORMULARIO;
                })
                .orElseGet(() -> noEncontrado(identificacion, redirect));
    }

    /**
     * Guarda la edición. El navegador solo puede enviar GET/POST, así que el formulario hace POST
     * a la web y la web hace PUT /api/ciudadanos/{identificacion}.
     */
    @PostMapping("/" + ID)
    public String actualizar(@PathVariable String identificacion, @Valid @ModelAttribute CiudadanoForm ciudadanoForm,
            BindingResult resultado, Model model, RedirectAttributes redirect) {
        // El campo es de solo lectura: si llega distinto a la URL, alguien lo alteró.
        if (!identificacion.equals(ciudadanoForm.getIdentificacion())) {
            redirect.addFlashAttribute("mensajeError", "La identificación de un ciudadano no se puede modificar.");
            return "redirect:/ciudadanos";
        }
        validarFechas(ciudadanoForm, resultado);
        if (resultado.hasErrors()) {
            return mostrarErrores(model, true, "Revisa los campos marcados.");
        }
        CiudadanoDto actualizado;
        try {
            actualizado = ciudadanos.actualizar(identificacion, ciudadanoForm.aDto());
        } catch (ApiNoEncontradoException e) {
            // Pudo eliminarse mientras se editaba.
            return noEncontrado(identificacion, redirect);
        } catch (ApiValidacionException | ApiNoDisponibleException e) {
            return errorAlGuardar(e, resultado, model, true);
        }
        redirect.addFlashAttribute("mensajeExito", "Cambios guardados para " + actualizado.nombres() + " "
                + actualizado.apellidos() + sufijoSimulado());
        return "redirect:/ciudadanos/{identificacion}";
    }

    // Regla entre dos campos: no cabe en una anotación de un solo campo.
    private static void validarFechas(CiudadanoForm form, BindingResult resultado) {
        if (!form.fechasCoherentes() && !resultado.hasFieldErrors("fechaExpedicion")) {
            resultado.rejectValue("fechaExpedicion", "fechas", ReglasCiudadano.MSG_FECHAS);
        }
    }

    /**
     * Error de la API al registrar o actualizar: se vuelve al formulario con lo escrito.
     * 400: los detalles "campo: mensaje" se muestran debajo de cada campo.
     * Red o 5xx: mensaje general para reintentar.
     */
    private String errorAlGuardar(RuntimeException e, BindingResult resultado, Model model, boolean modoEdicion) {
        if (e instanceof ApiValidacionException validacion) {
            aplicarErroresApi(validacion, resultado);
            return mostrarErrores(model, modoEdicion, "El servidor rechazó los datos: " + e.getMessage());
        }
        return mostrarErrores(model, modoEdicion, e.getMessage() + " Los datos no se guardaron.");
    }

    // Los detalles de la API llegan como "campo: mensaje"; si el campo existe en el formulario
    // el mensaje se muestra debajo de él.
    private static void aplicarErroresApi(ApiValidacionException e, BindingResult resultado) {
        for (String detalle : e.getDetalles()) {
            int separador = detalle.indexOf(": ");
            if (separador > 0) {
                String campo = detalle.substring(0, separador);
                if (resultado.getFieldType(campo) != null && !resultado.hasFieldErrors(campo)) {
                    resultado.rejectValue(campo, "api", detalle.substring(separador + 2));
                }
            }
        }
    }

    private static String mostrarErrores(Model model, boolean modoEdicion, String mensaje) {
        model.addAttribute("modoEdicion", modoEdicion);
        model.addAttribute("mensajeError", mensaje);
        return VISTA_FORMULARIO;
    }

    private static String noEncontrado(String identificacion, RedirectAttributes redirect) {
        redirect.addFlashAttribute("mensajeError",
                "No se encontró el ciudadano con identificación " + identificacion + ".");
        return "redirect:/ciudadanos";
    }

    private String sufijoSimulado() {
        return ciudadanos.esSimulado() ? " (SIMULADO: guardado solo en memoria, no en la API)." : ".";
    }
}
