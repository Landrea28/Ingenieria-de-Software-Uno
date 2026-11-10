package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.service.CiudadanoApiClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PanelController {

    private final CiudadanoApiClient ciudadanos;

    public PanelController(CiudadanoApiClient ciudadanos) {
        this.ciudadanos = ciudadanos;
    }

    @GetMapping("/")
    public String panel(Model model) {
        model.addAttribute("totalCiudadanos", ciudadanos.listar(null).size());
        return "panel";
    }
}
