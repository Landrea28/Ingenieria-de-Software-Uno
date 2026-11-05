package co.edu.konrad.consultaciudadano.usuario.service;

import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import co.edu.konrad.consultaciudadano.common.security.GeneradorCredenciales;
import co.edu.konrad.consultaciudadano.common.security.AuditoriaSeguridad;
import co.edu.konrad.consultaciudadano.common.service.ServicioCorreo;
import co.edu.konrad.consultaciudadano.usuario.dto.ActualizarUsuarioRequest;
import co.edu.konrad.consultaciudadano.usuario.dto.CrearUsuarioRequest;
import co.edu.konrad.consultaciudadano.usuario.dto.EstadoUsuario;
import co.edu.konrad.consultaciudadano.usuario.dto.UsuarioResponse;
import co.edu.konrad.consultaciudadano.usuario.entity.Usuario;
import co.edu.konrad.consultaciudadano.usuario.repository.PerfilRepository;
import co.edu.konrad.consultaciudadano.usuario.repository.UsuarioRepository;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private static final String PERFIL_ADMINISTRADOR = "ADMINISTRADOR";

    private final UsuarioRepository usuarios;
    private final PerfilRepository perfiles;
    private final GeneradorCredenciales generadorContrasena;
    private final PasswordEncoder passwordEncoder;
    private final ServicioCorreo correo;
    private final AuditoriaSeguridad auditoria;

    public UsuarioService(UsuarioRepository usuarios, PerfilRepository perfiles,
            GeneradorCredenciales generadorContrasena, PasswordEncoder passwordEncoder,
            ServicioCorreo correo, AuditoriaSeguridad auditoria) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
        this.generadorContrasena = generadorContrasena;
        this.passwordEncoder = passwordEncoder;
        this.correo = correo;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(EstadoUsuario estado, String perfil, int page, int size) {
        var activo = estado == null ? null : estado == EstadoUsuario.ACTIVO;
        var filtroPerfil = perfil == null || perfil.isBlank() ? null : perfil.trim();
        return usuarios.buscarUsuarios(activo, filtroPerfil,
                        PageRequest.of(page, size, Sort.by("identificacion").ascending()))
                .map(UsuarioResponse::desde);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(String identificacion) {
        return usuarios.findByIdentificacion(identificacion).map(UsuarioResponse::desde)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, CodigoError.NO_ENCONTRADO,
                        "Usuario no encontrado"));
    }

    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest request) {
        if (usuarios.existsByIdentificacion(request.identificacion())) {
            throw duplicado("La identificacion ya esta registrada");
        }
        if (usuarios.existsByCorreoIgnoreCase(request.correo())) {
            throw duplicado("El correo ya esta registrado");
        }
        var perfil = perfiles.findById(request.perfilId())
                .orElseThrow(() -> noEncontrado("Perfil no encontrado"));
        var contrasena = generadorContrasena.generar();
        var usuario = new Usuario(request.identificacion(), request.nombres(), request.apellidos(),
                request.correo(), null, null, null, passwordEncoder.encode(contrasena));
        usuario.getPerfiles().add(perfil);
        var guardado = usuarios.saveAndFlush(usuario);
        try {
            correo.enviarCredenciales(request.correo(), contrasena);
        } catch (MailException error) {
            throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE, CodigoError.INTEGRACION_PENDIENTE,
                    "El correo SIMULADO no esta disponible");
        }
        auditoria.usuarioCreado(request.identificacion(), perfil.getCodigo());
        return UsuarioResponse.desde(guardado);
    }

    @Transactional
    public UsuarioResponse actualizar(String identificacion, ActualizarUsuarioRequest request) {
        var usuario = usuarios.findByIdentificacion(identificacion)
                .orElseThrow(() -> noEncontrado("Usuario no encontrado"));
        if (usuarios.existsByCorreoIgnoreCaseAndIdentificacionNot(request.correo(), identificacion)) {
            throw duplicado("El correo ya esta registrado");
        }
        var perfil = perfiles.findById(request.perfilId())
                .orElseThrow(() -> noEncontrado("Perfil no encontrado"));
        var activo = request.estado() == EstadoUsuario.ACTIVO;
        var eraAdministrador = usuario.tienePerfilCodigo(PERFIL_ADMINISTRADOR);
        var seraAdministrador = PERFIL_ADMINISTRADOR.equals(perfil.getCodigo());
        var actor = SecurityContextHolder.getContext().getAuthentication();
        var esAutogestion = actor != null && Objects.equals(actor.getName(), identificacion);

        if (esAutogestion && (!activo || !seraAdministrador)) {
            throw new NegocioException(HttpStatus.CONFLICT, CodigoError.AUTODESACTIVACION_NO_PERMITIDA,
                    "Un administrador no puede desactivarse ni quitarse su perfil");
        }
        if (usuario.isActivo() && eraAdministrador && (!activo || !seraAdministrador)
                && usuarios.bloquearAdministradoresActivos().size() <= 1) {
            throw new NegocioException(HttpStatus.CONFLICT, CodigoError.ULTIMO_ADMIN_ACTIVO,
                    "Debe permanecer al menos un administrador activo");
        }

        usuario.actualizar(request.nombres(), request.apellidos(), request.correo(), perfil, activo);
        auditoria.cambioPerfilEstado(identificacion, perfil.getCodigo(), request.estado().name());
        return UsuarioResponse.desde(usuario);
    }

    private NegocioException duplicado(String mensaje) {
        return new NegocioException(HttpStatus.CONFLICT, CodigoError.DUPLICADO, mensaje);
    }

    private NegocioException noEncontrado(String mensaje) {
        return new NegocioException(HttpStatus.NOT_FOUND, CodigoError.NO_ENCONTRADO, mensaje);
    }
}
