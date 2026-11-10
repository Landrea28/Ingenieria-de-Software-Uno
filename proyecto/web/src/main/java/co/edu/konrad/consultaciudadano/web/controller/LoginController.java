package co.edu.konrad.consultaciudadano.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Placeholder del login del administrador. Más adelante: POST /api/auth/login al backend
 * y el token se guarda en la HttpSession (nunca en el navegador).
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String salida, Model model) {
        if (salida != null) {
            model.addAttribute("mensajeInfo", "Sesión cerrada.");
        }
        return "login";
    }

    // Por ahora no valida credenciales: solo entra al panel para poder navegar el esqueleto.
    @PostMapping("/login")
    public String ingresar() {
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String salir() {
        return "redirect:/login?salida";
    }
}
