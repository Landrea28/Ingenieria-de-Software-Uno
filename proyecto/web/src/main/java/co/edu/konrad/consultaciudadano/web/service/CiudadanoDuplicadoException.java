package co.edu.konrad.consultaciudadano.web.service;

/** La identificación ya está registrada (código DUPLICADO de la API). */
public class CiudadanoDuplicadoException extends ApiConflictoException {

    public CiudadanoDuplicadoException(String identificacion) {
        super("Ya existe un ciudadano con la identificación " + identificacion);
    }
}
