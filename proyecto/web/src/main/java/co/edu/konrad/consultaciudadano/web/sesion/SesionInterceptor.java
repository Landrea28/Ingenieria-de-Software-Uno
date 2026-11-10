package co.edu.konrad.consultaciudadano.web.sesion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Se ejecuta antes de cada pantalla protegida (ver WebConfig): sin sesión activa redirige al login.
 * Es solo para la experiencia de uso; quien realmente autoriza es la API con el token.
 */
@Component
public class SesionInterceptor implements HandlerInterceptor {

    private final SesionAdmin sesion;

    public SesionInterceptor(SesionAdmin sesion) {
        this.sesion = sesion;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (sesion.activa()) {
            return true;
        }
        String destino = sesion.vencida() ? "/login?expirada" : "/login";
        sesion.cerrar();
        response.sendRedirect(request.getContextPath() + destino);
        return false;
    }
}
