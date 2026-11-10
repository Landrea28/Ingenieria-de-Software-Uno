package co.edu.konrad.consultaciudadano.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Pantalla para solicitar reportes; el PDF lo genera el backend (David). */
@Controller
public class ReporteController {

    @GetMapping("/reportes")
    public String reportes() {
        return "reportes";
    }
}
