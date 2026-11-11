package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.service.AccesoAdminService;
import co.edu.konrad.consultaciudadano.web.service.ApiNoDisponibleException;
import co.edu.konrad.consultaciudadano.web.sesion.SesionAdmin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Login del administrador contra POST /api/auth/login. El token queda en la sesión del servidor. */
@Controller
public class LoginController {

    private final AccesoAdminService acceso;
    private final SesionAdmin sesion;

    public LoginController(AccesoAdminService acceso, SesionAdmin sesion) {
        this.acceso = acceso;
        this.sesion = sesion;
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String salida,
            @RequestParam(required = false) String expirada,
            @RequestParam(required = false) String denegado, Model model) {
        if (sesion.activa()) {
            return "redirect:/";
        }
        if (salida != null) {
            model.addAttribute("mensajeInfo", "Sesión cerrada.");
        } else if (expirada != null) {
            model.addAttribute("mensajeInfo", "Su sesión expiró. Ingrese de nuevo.");
        } else if (denegado != null) {
            model.addAttribute("mensajeError", "Su usuario no tiene permisos de administrador.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String ingresar(@RequestParam(defaultValue = "") String identificacion,
            @RequestParam(defaultValue = "") String contrasena,
            @RequestParam(defaultValue = "false") boolean recordarContrasena,
            HttpServletRequest request, Model model) {
        model.addAttribute("identificacion", identificacion.trim());
        if (identificacion.isBlank() || contrasena.isBlank()) {
            model.addAttribute("mensajeError", "Ingrese identificación y contraseña.");
            return "login";
        }
        AccesoAdminService.Resultado resultado;
        try {
            resultado = acceso.iniciarSesion(identificacion.trim(), contrasena, recordarContrasena);
        } catch (ApiNoDisponibleException e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "login";
        }
        switch (resultado) {
            case EXITO -> {
                // Nuevo id de sesión tras autenticarse (evita la fijación de sesión).
                request.changeSessionId();
                // La sesión web dura lo mismo que el token (15 min, u 8 h con "Recordar contraseña"),
                // en lugar de los 30 min fijos de server.servlet.session.timeout.
                request.getSession().setMaxInactiveInterval((int) Math.max(sesion.segundosRestantes(), 60));
                return "redirect:/";
            }
            case SIN_PERMISO_ADMIN -> model.addAttribute("mensajeError", "Su usuario no tiene permisos de administrador.");
            default -> model.addAttribute("mensajeError", "Identificación o contraseña incorrectas.");
        }
        return "login";
    }

    @PostMapping("/logout")
    public String salir(HttpSession httpSession) {
        acceso.cerrarSesion();
        httpSession.invalidate();
        return "redirect:/login?salida";
    }
}
