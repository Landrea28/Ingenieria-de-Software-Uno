package co.edu.konrad.consultaciudadano.common.security;

/**
 * Contrato para generar una contraseña temporal. Lo implementa el generador del módulo de autenticación.
 */
public interface GeneradorCredenciales {
    String generar();
}
