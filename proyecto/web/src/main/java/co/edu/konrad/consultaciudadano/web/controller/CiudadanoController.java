package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.service.CiudadanoApiClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/ciudadanos")
public class CiudadanoController {

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
        model.addAttribute("modoEdicion", false);
        return "ciudadanos/formulario";
    }

    @GetMapping("/{identificacion}/editar")
    public String editar(@PathVariable String identificacion, Model model) {
        model.addAttribute("modoEdicion", true);
        model.addAttribute("ciudadano", ciudadanos.buscar(identificacion).orElse(null));
        return "ciudadanos/formulario";
    }
}
