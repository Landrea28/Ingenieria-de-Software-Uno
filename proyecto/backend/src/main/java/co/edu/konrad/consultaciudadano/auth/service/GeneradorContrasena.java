package co.edu.konrad.consultaciudadano.auth.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GeneradorContrasena {
    private static final String MAYUSCULAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITOS = "0123456789";
    private static final String ALFANUMERICOS = MAYUSCULAS + MINUSCULAS + DIGITOS;
    private final SecureRandom random = new SecureRandom();

    public String generar() {
        List<Character> caracteres = new ArrayList<>(6);
        caracteres.add(caracterAleatorio(MAYUSCULAS));
        caracteres.add(caracterAleatorio(MINUSCULAS));
        caracteres.add(caracterAleatorio(DIGITOS));
        for (int indice = caracteres.size(); indice < 6; indice++) {
            caracteres.add(caracterAleatorio(ALFANUMERICOS));
        }
        Collections.shuffle(caracteres, random);
        var resultado = new StringBuilder(6);
        caracteres.forEach(resultado::append);
        return resultado.toString();
    }

    private char caracterAleatorio(String caracteres) {
        return caracteres.charAt(random.nextInt(caracteres.length()));
    }
}
