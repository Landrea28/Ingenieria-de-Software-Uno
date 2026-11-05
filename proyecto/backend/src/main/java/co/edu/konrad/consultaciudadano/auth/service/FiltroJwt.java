package co.edu.konrad.consultaciudadano.auth.service;

import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.ErrorResponse;
import co.edu.konrad.consultaciudadano.common.security.AuditoriaSeguridad;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FiltroJwt extends OncePerRequestFilter {
    private final ServicioJwt jwt;
    private final ServicioRevocacionToken revocacion;
    private final ObjectMapper objectMapper;
    private final AuditoriaSeguridad auditoria;

    public FiltroJwt(ServicioJwt jwt, ServicioRevocacionToken revocacion, ObjectMapper objectMapper,
            AuditoriaSeguridad auditoria) {
        this.jwt = jwt;
        this.revocacion = revocacion;
        this.objectMapper = objectMapper;
        this.auditoria = auditoria;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        Claims claims;
        try {
            var token = header.substring(7);
            if (token.isBlank()) {
                throw new IllegalArgumentException("Token Bearer vacio");
            }
            claims = jwt.validar(token);
            if (claims.getId() == null || claims.getExpiration() == null || claims.getSubject() == null
                    || claims.getSubject().isBlank() || revocacion.estaRevocado(claims.getId())) {
                throw new IllegalArgumentException("Token no valido");
            }
        } catch (JwtException | IllegalArgumentException error) {
            SecurityContextHolder.clearContext();
            auditoria.noAutorizado(request);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(),
                    ErrorResponse.crear(CodigoError.NO_AUTENTICADO, "No autenticado", List.of()));
            return;
        }
        var authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                authorities(claims.get("roles")));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private Collection<SimpleGrantedAuthority> authorities(Object rolesClaim) {
        if (!(rolesClaim instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream().filter(String.class::isInstance).map(String.class::cast)
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol)).toList();
    }
}
