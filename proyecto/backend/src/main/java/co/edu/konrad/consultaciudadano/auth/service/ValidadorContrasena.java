package co.edu.konrad.consultaciudadano.auth.service;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Comprueba que una contraseña cumpla la política del sistema (seis caracteres con mayúscula, minúscula y número).
 */
@Component
public class ValidadorContrasena {
    private static final Pattern POLITICA = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{6}$");

    public boolean esValida(String contrasena) {
        return contrasena != null && POLITICA.matcher(contrasena).matches();
    }
}
