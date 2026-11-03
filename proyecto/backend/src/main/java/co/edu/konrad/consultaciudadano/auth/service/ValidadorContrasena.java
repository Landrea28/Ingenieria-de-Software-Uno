package co.edu.konrad.consultaciudadano.auth.service;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ValidadorContrasena {
    private static final Pattern POLITICA = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{6}$");

    public boolean esValida(String contrasena) {
        return contrasena != null && POLITICA.matcher(contrasena).matches();
    }
}
