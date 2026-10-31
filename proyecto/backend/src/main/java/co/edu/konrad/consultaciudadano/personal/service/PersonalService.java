package co.edu.konrad.consultaciudadano.personal.service;

import co.edu.konrad.consultaciudadano.catalogo.repository.FuerzaPublicaRepository;
import co.edu.konrad.consultaciudadano.catalogo.repository.RangoRepository;
import co.edu.konrad.consultaciudadano.catalogo.service.CatalogoService;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;
import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalResponse;
import co.edu.konrad.consultaciudadano.usuario.entity.Usuario;
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
    private final RegistroPersonalIntegracion integracion;

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
        usuarios.saveAndFlush(usuario);
        integracion.enviarCorreo(request.correo(), contrasena);
        return new RegistroPersonalResponse("Registro aceptado. Revise el correo de confirmacion");
    }
}
