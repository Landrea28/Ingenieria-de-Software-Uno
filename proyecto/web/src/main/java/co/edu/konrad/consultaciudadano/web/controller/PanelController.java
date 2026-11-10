package co.edu.konrad.consultaciudadano.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PanelController {

    @GetMapping("/")
    public String panel() {
        return "panel";
    }
}
