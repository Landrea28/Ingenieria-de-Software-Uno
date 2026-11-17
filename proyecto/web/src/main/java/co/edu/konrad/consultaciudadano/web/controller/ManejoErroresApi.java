package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.service.ApiAccesoDenegadoException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoAutorizadoException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoDisponibleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Errores de la API que pueden ocurrir en cualquier pantalla y no maneja el controlador.
 * 401/403: la API ya no acepta el token (vencido, revocado o sin rol) → se cierra la sesión
 * local y se vuelve al login con un mensaje.
 */
@ControllerAdvice
public class ManejoErroresApi {

    @ExceptionHandler(ApiNoAutorizadoException.class)
    public String noAutorizado(HttpServletRequest request) {
        invalidarSesion(request);
        return "redirect:/login?expirada";
    }

    @ExceptionHandler(ApiAccesoDenegadoException.class)
    public String accesoDenegado(HttpServletRequest request) {
        invalidarSesion(request);
        return "redirect:/login?denegado";
    }

    @ExceptionHandler(ApiNoDisponibleException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String noDisponible(ApiNoDisponibleException e, HttpServletRequest request, Model model) {
        model.addAttribute("mensajeError", e.getMessage());
        model.addAttribute("reintentar", request.getRequestURI());
        return "error-api";
    }

    private static void invalidarSesion(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
    }
}
