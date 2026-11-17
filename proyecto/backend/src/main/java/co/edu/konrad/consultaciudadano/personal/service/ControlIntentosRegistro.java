package co.edu.konrad.consultaciudadano.personal.service;

/**
 * Contrato para decidir si un origen puede intentar otro registro (límite de intentos).
 */
public interface ControlIntentosRegistro {
    boolean permitido(String ip);
}
