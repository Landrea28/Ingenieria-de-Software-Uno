package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.service.ApiNoDisponibleException;
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
        // Si la API no responde, el panel se muestra igual, sin el total.
        try {
            model.addAttribute("totalCiudadanos", ciudadanos.listar(null, 0).totalElements());
        } catch (ApiNoDisponibleException e) {
            model.addAttribute("mensajeError", "No fue posible consultar el total de ciudadanos. "
                    + ApiNoDisponibleException.MENSAJE);
        }
        return "panel";
    }
}
