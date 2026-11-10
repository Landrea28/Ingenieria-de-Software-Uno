package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.form.CiudadanoForm;
import co.edu.konrad.consultaciudadano.web.service.CiudadanoApiClient;
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

    // BindingResult debe ir justo después del formulario: recoge los errores de conversión
    // (p. ej. estatura "abc") en lugar de lanzar una excepción.
    @PostMapping
    public String crear(@ModelAttribute CiudadanoForm ciudadanoForm, BindingResult resultado,
            Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return mostrarErrores(model, false);
        }
        redirect.addFlashAttribute("mensajeInfo", MENSAJE_SIMULADO);
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
    public String actualizar(@PathVariable String identificacion, @ModelAttribute CiudadanoForm ciudadanoForm,
            BindingResult resultado, Model model, RedirectAttributes redirect) {
        // La identificación de la URL manda; el campo del formulario es de solo lectura.
        ciudadanoForm.setIdentificacion(identificacion);
        if (resultado.hasErrors()) {
            return mostrarErrores(model, true);
        }
        redirect.addFlashAttribute("mensajeInfo", MENSAJE_SIMULADO);
        return "redirect:/ciudadanos";
    }

    private String mostrarErrores(Model model, boolean modoEdicion) {
        model.addAttribute("modoEdicion", modoEdicion);
        model.addAttribute("mensajeError", "Revisa los campos marcados.");
        return VISTA_FORMULARIO;
    }
}
