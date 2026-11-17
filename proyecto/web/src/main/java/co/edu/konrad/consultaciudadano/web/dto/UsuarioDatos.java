package co.edu.konrad.consultaciudadano.web.dto;

/**
 * Datos que la web envía a la API para crear o actualizar un usuario (esquemas CrearUsuarioRequest y
 * ActualizarUsuarioRequest). Al crear no se envía el estado; al actualizar no se envía la identificación.
 */
public record UsuarioDatos(
        String identificacion,
        String nombres,
        String apellidos,
        String correo,
        Long perfilId,
        String estado) {}
