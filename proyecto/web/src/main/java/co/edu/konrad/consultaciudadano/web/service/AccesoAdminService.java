package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.LoginResponse;
import co.edu.konrad.consultaciudadano.web.sesion.SesionAdmin;
import org.springframework.stereotype.Service;

/** Inicio y cierre de sesión del administrador contra la API. */
@Service
public class AccesoAdminService {

    public enum Resultado { EXITO, CREDENCIALES_INVALIDAS, SIN_PERMISO_ADMIN }

    private static final long DURACION_POR_DEFECTO = 15 * 60;

    private final AuthApiClient auth;
    private final CiudadanoApiClient ciudadanos;
    private final SesionAdmin sesion;

    public AccesoAdminService(AuthApiClient auth, CiudadanoApiClient ciudadanos, SesionAdmin sesion) {
        this.auth = auth;
        this.ciudadanos = ciudadanos;
        this.sesion = sesion;
    }

    /**
     * 1. POST /api/auth/login. 2. Guarda el token en la sesión. 3. Verifica el rol: la respuesta del
     * login no dice el rol, así que hace una consulta que exige ADMINISTRADOR; si la API responde 403,
     * el usuario (p. ej. personal de la fuerza pública) no puede usar la web administrativa.
     * @throws ApiNoDisponibleException si la API no responde
     */
    public Resultado iniciarSesion(String identificacion, String contrasena, boolean recordarContrasena) {
        // Sin token previo: la API rechaza con 401 cualquier petición que traiga un token vencido.
        sesion.cerrar();
        LoginResponse respuesta;
        try {
            respuesta = auth.login(identificacion, contrasena, recordarContrasena);
        } catch (ApiNoAutorizadoException | ApiValidacionException e) {
            // 400 (formato) y 401 dan el mismo mensaje: no revelar si la identificación existe.
            return Resultado.CREDENCIALES_INVALIDAS;
        }
        long duracion = respuesta.expiraEnSegundos() != null ? respuesta.expiraEnSegundos() : DURACION_POR_DEFECTO;
        sesion.iniciar(identificacion, respuesta.token(), duracion);
        try {
            ciudadanos.listar(null, 0);
        } catch (ApiAccesoDenegadoException e) {
            cerrarSesion();
            return Resultado.SIN_PERMISO_ADMIN;
        } catch (RuntimeException e) {
            sesion.cerrar();
            throw e;
        }
        return Resultado.EXITO;
    }

    /** Revoca el token en la API (si se puede) y limpia la sesión. */
    public void cerrarSesion() {
        if (sesion.getToken() != null) {
            try {
                auth.logout();
            } catch (ApiException e) {
                // Token ya vencido o API caída: igual se cierra la sesión local.
            }
        }
        sesion.cerrar();
    }
}
