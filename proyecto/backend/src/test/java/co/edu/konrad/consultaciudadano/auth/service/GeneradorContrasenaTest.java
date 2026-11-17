package co.edu.konrad.consultaciudadano.auth.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class GeneradorContrasenaTest {
    private final GeneradorContrasena generador = new GeneradorContrasena();
    private final ValidadorContrasena validador = new ValidadorContrasena();

    @Test
    void generaMilContrasenasDeSeisCaracteresConLasTresClases() {
        var formato = Pattern.compile("^[A-Za-z\\d]{6}$");
        for (int intento = 0; intento < 1_000; intento++) {
            var contrasena = generador.generar();
            assertTrue(formato.matcher(contrasena).matches());
            assertTrue(contrasena.matches(".*[A-Z].*"));
            assertTrue(contrasena.matches(".*[a-z].*"));
            assertTrue(contrasena.matches(".*\\d.*"));
            assertTrue(validador.esValida(contrasena));
        }
    }

    @Test
    void validaCasosLimiteDeLaPolitica() {
        assertTrue(validador.esValida("Abc123"));
        assertFalse(validador.esValida("Abc12"));
        assertFalse(validador.esValida("Abc1234"));
        assertFalse(validador.esValida("abc123"));
        assertFalse(validador.esValida("Abc12!"));
        assertFalse(validador.esValida(null));
    }
}
