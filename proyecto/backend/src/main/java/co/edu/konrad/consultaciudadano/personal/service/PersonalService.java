package co.edu.konrad.consultaciudadano.personal.service;

import co.edu.konrad.consultaciudadano.catalogo.repository.FuerzaPublicaRepository;
import co.edu.konrad.consultaciudadano.catalogo.repository.RangoRepository;
import co.edu.konrad.consultaciudadano.catalogo.service.CatalogoService;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import co.edu.konrad.consultaciudadano.common.security.AuditoriaSeguridad;
import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;
import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalResponse;
import co.edu.konrad.consultaciudadano.usuario.entity.Usuario;
import co.edu.konrad.consultaciudadano.usuario.repository.PerfilRepository;
import co.edu.konrad.consultaciudadano.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonalService {
    private final CatalogoService catalogo;
    private final FuerzaPublicaRepository fuerzas;
    private final RangoRepository rangos;
    private final UsuarioRepository usuarios;
    private final PerfilRepository perfiles;
    private final RegistroPersonalIntegracion integracion;
    private final AuditoriaSeguridad auditoria;

    @Transactional
    public RegistroPersonalResponse registrar(RegistroPersonalRequest request) {
        catalogo.validar(request.fuerzaCodigo(), request.rangoId());
        integracion.validarContraDefensa(request);
        if (usuarios.existsByIdentificacion(request.identificacion()) ||
                usuarios.existsByCorreoIgnoreCase(request.correo())) {
            throw new NegocioException(HttpStatus.CONFLICT, CodigoError.DUPLICADO,
                    "El personal ya se encuentra registrado");
        }
        var contrasena = integracion.generarContrasena();
        var hash = integracion.hashearContrasena(contrasena);
        var usuario = new Usuario(request.identificacion(), request.nombres(), request.apellidos(),
                request.correo(), fuerzas.getReferenceById(request.fuerzaCodigo()),
                rangos.getReferenceById(request.rangoId()), request.idFuerza(), hash);
        var perfilPersonal = perfiles.findByCodigo("PERSONAL_FUERZA_PUBLICA")
                .orElseThrow(() -> new NegocioException(HttpStatus.INTERNAL_SERVER_ERROR, CodigoError.ERROR_INTERNO,
                        "No está configurado el perfil de personal"));
        usuario.getPerfiles().add(perfilPersonal);
        usuarios.saveAndFlush(usuario);
        integracion.enviarCorreo(request.correo(), contrasena);
        auditoria.registroPersonal(request.identificacion());
        return new RegistroPersonalResponse("Registro aceptado. Revise el correo de confirmacion");
    }
}
