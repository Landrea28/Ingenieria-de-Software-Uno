package co.edu.konrad.consultaciudadano.auth.service;

import co.edu.konrad.consultaciudadano.auth.dto.LoginRequest;
import co.edu.konrad.consultaciudadano.auth.dto.LoginResponse;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import co.edu.konrad.consultaciudadano.common.security.AuditoriaSeguridad;
import co.edu.konrad.consultaciudadano.usuario.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacionService implements AutenticacionIntegracion {
    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Credenciales inválidas";
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final ServicioJwt jwt;
    private final ServicioRevocacionToken revocacion;
    private final ServicioBloqueoLogin bloqueos;
    private final AuditoriaSeguridad auditoria;
    private final String hashInexistente;

    public AutenticacionService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, ServicioJwt jwt,
            ServicioRevocacionToken revocacion, ServicioBloqueoLogin bloqueos, AuditoriaSeguridad auditoria) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.revocacion = revocacion;
        this.bloqueos = bloqueos;
        this.auditoria = auditoria;
        this.hashInexistente = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        if (bloqueos.estaBloqueada(request.identificacion())) {
            auditoria.bloqueoLogin(request.identificacion());
            throw credencialesInvalidas();
        }
        var usuario = usuarios.findByIdentificacion(request.identificacion());
        var hashValido = usuarios.buscarPasswordHash(request.identificacion())
                .map(hash -> passwordEncoder.matches(request.contrasena(), hash))
                .orElseGet(() -> passwordEncoder.matches(request.contrasena(), hashInexistente));
        if (usuario.isEmpty() || !hashValido || !usuario.get().isActivo()) {
            bloqueos.registrarFallo(request.identificacion());
            auditoria.loginFallido(request.identificacion());
            throw credencialesInvalidas();
        }
        bloqueos.limpiar(request.identificacion());
        auditoria.loginExitoso(request.identificacion());
        var token = jwt.emitir(usuario.get(), Boolean.TRUE.equals(request.recordarContrasena()));
        return new LoginResponse(token.valor(), "Bearer", token.expiraEnSegundos());
    }

    @Override
    public void logout(String autorizacion) {
        var token = extraerBearer(autorizacion);
        var claims = validarToken(token);
        if (claims.getId() == null || claims.getExpiration() == null) {
            throw noAutenticado();
        }
        if (!revocacion.estaRevocado(claims.getId())) {
            revocacion.revocar(claims.getId(), claims.getExpiration().toInstant());
        }
        auditoria.logout(claims.getSubject());
    }

    private Claims validarToken(String token) {
        try {
            return jwt.validar(token);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException error) {
            throw noAutenticado();
        }
    }

    private String extraerBearer(String autorizacion) {
        if (autorizacion == null || !autorizacion.startsWith("Bearer ") || autorizacion.length() <= 7) {
            throw noAutenticado();
        }
        return autorizacion.substring(7);
    }

    private NegocioException credencialesInvalidas() {
        return new NegocioException(HttpStatus.UNAUTHORIZED, CodigoError.NO_AUTENTICADO,
                MENSAJE_CREDENCIALES_INVALIDAS);
    }

    private NegocioException noAutenticado() {
        return new NegocioException(HttpStatus.UNAUTHORIZED, CodigoError.NO_AUTENTICADO,
                MENSAJE_CREDENCIALES_INVALIDAS);
    }
}
