package co.edu.konrad.consultaciudadano.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Cabeceras de seguridad para todas las respuestas de la web.
 * - Cache-Control no-store en las pantallas: tras cerrar sesión, "Atrás" no muestra datos de ciudadanos
 *   guardados en la caché del navegador. Los CSS/JS sí pueden guardarse.
 * - X-Frame-Options / frame-ancestors: ningún otro sitio puede incrustar el panel en un iframe (clickjacking).
 * - Content-Security-Policy: solo se cargan scripts, estilos e imágenes de la propia web.
 * - Referrer-Policy: las URL con identificaciones (?busqueda=...) no se envían a otros sitios.
 */
@Component
public class CabecerasSeguridadFilter extends OncePerRequestFilter {

    static final String CSP = "default-src 'self'; frame-ancestors 'none'; form-action 'self'; "
            + "base-uri 'self'; object-src 'none'";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "same-origin");
        // El PDF se muestra con el visor del navegador; una CSP estricta puede bloquearlo en algunos navegadores.
        if (!ruta.startsWith("/reportes/pdf")) {
            response.setHeader("Content-Security-Policy", CSP);
        }
        if (!ruta.startsWith("/css/") && !ruta.startsWith("/js/")) {
            response.setHeader("Cache-Control", "no-store");
        }
        chain.doFilter(request, response);
    }
}
