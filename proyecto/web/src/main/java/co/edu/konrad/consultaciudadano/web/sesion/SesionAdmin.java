package co.edu.konrad.consultaciudadano.web.sesion;

import java.io.Serializable;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Datos de la sesión del administrador, guardados en la HttpSession del servidor web.
 * El token JWT nunca se envía al navegador: el navegador solo tiene la cookie de sesión.
 * @SessionScope: Spring crea una instancia por cada sesión de usuario.
 */
@Component
@SessionScope
public class SesionAdmin implements Serializable {

    private String identificacion;
    private String token;
    private Instant expiraEn;

    public void iniciar(String identificacion, String token, long expiraEnSegundos) {
        this.identificacion = identificacion;
        this.token = token;
        this.expiraEn = Instant.now().plusSeconds(expiraEnSegundos);
    }

    public void cerrar() {
        identificacion = null;
        token = null;
        expiraEn = null;
    }

    /** true si hay token y aún no venció. */
    public boolean activa() {
        return token != null && expiraEn != null && Instant.now().isBefore(expiraEn);
    }

    /** true si hubo sesión pero el token ya venció. */
    public boolean vencida() {
        return token != null && !activa();
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getToken() {
        return token;
    }
}
