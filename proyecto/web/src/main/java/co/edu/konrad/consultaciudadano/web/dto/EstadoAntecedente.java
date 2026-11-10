package co.edu.konrad.consultaciudadano.web.dto;

import java.util.Arrays;
import java.util.Optional;

/** Estado del antecedente de un ciudadano (mismos valores que el backend), con su texto para la pantalla. */
public enum EstadoAntecedente {

    REQUERIDO("Requerido"),
    NO_REQUERIDO("No requerido");

    private final String etiqueta;

    EstadoAntecedente(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Convierte el texto recibido en la URL; vacío si no es un estado válido. */
    public static Optional<EstadoAntecedente> desde(String valor) {
        return Arrays.stream(values()).filter(e -> e.name().equals(valor)).findFirst();
    }
}
