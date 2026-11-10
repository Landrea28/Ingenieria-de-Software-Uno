package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.dto.CiudadanoDto;
import co.edu.konrad.consultaciudadano.web.form.CiudadanoForm;
import co.edu.konrad.consultaciudadano.web.form.ReglasCiudadano;
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

@Controller
@RequestMapping("/ciudadanos")
public class CiudadanoController {

    private static final String VISTA_FORMULARIO = "ciudadanos/formulario";
    private static final String MENSAJE_SIMULADO =
            "SIMULACIÓN: los datos no se guardaron porque la API aún no está conectada.";

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

    @GetMapping
    public String lista(@RequestParam(required = false) String busqueda, Model model) {
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("ciudadanos", ciudadanos.listar(busqueda));
        return "ciudadanos/lista";
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
            return mostrarErrores(model, false);
        }
        CiudadanoDto creado;
        try {
            creado = ciudadanos.registrar(ciudadanoForm.aDto());
        } catch (CiudadanoDuplicadoException e) {
            resultado.rejectValue("identificacion", "duplicado", "Ya existe un ciudadano con esta identificación.");
            return mostrarErrores(model, false);
        }
        String sufijo = ciudadanos.esSimulado() ? " (SIMULADO: guardado solo en memoria, no en la API)." : ".";
        redirect.addFlashAttribute("mensajeExito", "Ciudadano registrado: " + creado.nombres() + " "
                + creado.apellidos() + ", identificación " + creado.identificacion() + sufijo);
        redirect.addFlashAttribute("resaltado", creado.identificacion());
        return "redirect:/ciudadanos";
    }

    @GetMapping("/{identificacion}/editar")
    public String editar(@PathVariable String identificacion, Model model, RedirectAttributes redirect) {
        return ciudadanos.buscar(identificacion)
                .map(c -> {
                    model.addAttribute("ciudadanoForm", CiudadanoForm.desde(c));
                    model.addAttribute("modoEdicion", true);
                    return VISTA_FORMULARIO;
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("mensajeError",
                            "No se encontró el ciudadano con identificación " + identificacion + ".");
                    return "redirect:/ciudadanos";
                });
    }

    @PostMapping("/{identificacion}")
    public String actualizar(@PathVariable String identificacion, @Valid @ModelAttribute CiudadanoForm ciudadanoForm,
            BindingResult resultado, Model model, RedirectAttributes redirect) {
        // El campo es de solo lectura: si llega distinto a la URL, alguien lo alteró.
        if (!identificacion.equals(ciudadanoForm.getIdentificacion())) {
            redirect.addFlashAttribute("mensajeError", "La identificación de un ciudadano no se puede modificar.");
            return "redirect:/ciudadanos";
        }
        validarFechas(ciudadanoForm, resultado);
        if (resultado.hasErrors()) {
            return mostrarErrores(model, true);
        }
        redirect.addFlashAttribute("mensajeInfo", MENSAJE_SIMULADO);
        return "redirect:/ciudadanos";
    }

    // Regla entre dos campos: no cabe en una anotación de un solo campo.
    private static void validarFechas(CiudadanoForm form, BindingResult resultado) {
        if (!form.fechasCoherentes() && !resultado.hasFieldErrors("fechaExpedicion")) {
            resultado.rejectValue("fechaExpedicion", "fechas",
                    "Debe ser posterior a la fecha de nacimiento.");
        }
    }

    private String mostrarErrores(Model model, boolean modoEdicion) {
        model.addAttribute("modoEdicion", modoEdicion);
        model.addAttribute("mensajeError", "Revisa los campos marcados.");
        return VISTA_FORMULARIO;
    }
}
