package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.service.CiudadanoApiClient;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Atributos disponibles en todas las vistas (p. ej. el aviso de datos simulados del layout). */
@ControllerAdvice
public class ModeloGlobal {

    private final CiudadanoApiClient ciudadanos;

    public ModeloGlobal(CiudadanoApiClient ciudadanos) {
        this.ciudadanos = ciudadanos;
    }

    @ModelAttribute("modoMock")
    public boolean modoMock() {
        return ciudadanos.esSimulado();
    }
}
