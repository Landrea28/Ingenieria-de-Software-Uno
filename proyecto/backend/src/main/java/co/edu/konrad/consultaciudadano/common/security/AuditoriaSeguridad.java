package co.edu.konrad.consultaciudadano.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuditoriaSeguridad {
    private static final Logger LOG = LoggerFactory.getLogger(AuditoriaSeguridad.class);

    public void loginExitoso(String identificacion) {
        LOG.info("Evento seguridad=login_ok identificacion={} ip={}", identificacion, ipActual());
    }

    public void loginFallido(String identificacion) {
        LOG.warn("Evento seguridad=login_fallido identificacion={} ip={}", identificacion, ipActual());
    }

    public void bloqueoLogin(String identificacion) {
        LOG.warn("Evento seguridad=bloqueo_login identificacion={} ip={}", identificacion, ipActual());
    }

    public void registroPersonal(String identificacion) {
        LOG.info("Evento seguridad=registro_personal identificacion={} ip={}", identificacion, ipActual());
    }

    public void registroPersonalFallido(String identificacion) {
        LOG.warn("Evento seguridad=registro_personal_fallido identificacion={} ip={}", identificacion, ipActual());
    }

    public void usuarioCreado(String identificacion, String perfil) {
        LOG.info("Evento seguridad=usuario_creado identificacion={} perfil={} ip={}",
                identificacion, perfil, ipActual());
    }

    public void cambioPerfilEstado(String identificacion, String perfil, String estado) {
        LOG.info("Evento seguridad=cambio_perfil_estado identificacion={} perfil={} estado={} ip={}",
                identificacion, perfil, estado, ipActual());
    }

    public void cambioPermisos(String perfil) {
        LOG.info("Evento seguridad=cambio_permisos perfil={} ip={}", perfil, ipActual());
    }

    public void logout(String identificacion) {
        LOG.info("Evento seguridad=logout identificacion={} ip={}", identificacion, ipActual());
    }

    public void noAutorizado(HttpServletRequest request) {
        LOG.warn("Evento seguridad=401 identificacion={} ip={} ruta={}",
                identidad(request), request.getRemoteAddr(), request.getRequestURI());
    }

    public void accesoDenegado(HttpServletRequest request) {
        LOG.warn("Evento seguridad=403 identificacion={} ip={} ruta={}",
                identidad(request), request.getRemoteAddr(), request.getRequestURI());
    }

    private String ipActual() {
        var attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servlet
                ? servlet.getRequest().getRemoteAddr() : "desconocida";
    }

    private String identidad(HttpServletRequest request) {
        return request.getUserPrincipal() == null ? "anonimo" : request.getUserPrincipal().getName();
    }
}
