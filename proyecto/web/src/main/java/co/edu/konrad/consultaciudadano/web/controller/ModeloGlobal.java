package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.service.CiudadanoApiClient;
import co.edu.konrad.consultaciudadano.web.sesion.SesionAdmin;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Atributos disponibles en todas las vistas (aviso de datos simulados y usuario de la sesión). */
@ControllerAdvice
public class ModeloGlobal {

    private final CiudadanoApiClient ciudadanos;
    private final SesionAdmin sesion;

    public ModeloGlobal(CiudadanoApiClient ciudadanos, SesionAdmin sesion) {
        this.ciudadanos = ciudadanos;
        this.sesion = sesion;
    }

    @ModelAttribute("modoMock")
    public boolean modoMock() {
        return ciudadanos.esSimulado();
    }

    @ModelAttribute("usuarioSesion")
    public String usuarioSesion() {
        return sesion.getIdentificacion();
    }
}
