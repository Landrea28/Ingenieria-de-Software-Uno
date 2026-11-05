package co.edu.konrad.consultaciudadano.usuario.service;

import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import co.edu.konrad.consultaciudadano.common.security.AuditoriaSeguridad;
import co.edu.konrad.consultaciudadano.usuario.dto.PermisoResponse;
import co.edu.konrad.consultaciudadano.usuario.dto.PerfilResponse;
import co.edu.konrad.consultaciudadano.usuario.entity.Permiso;
import co.edu.konrad.consultaciudadano.usuario.repository.PerfilRepository;
import co.edu.konrad.consultaciudadano.usuario.repository.PermisoRepository;
import java.util.HashSet;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {
    private static final String PERFIL_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String PERMISO_GESTION_SEGURIDAD = "SEGURIDAD_ADMINISTRAR";

    private final PerfilRepository perfiles;
    private final PermisoRepository permisos;
    private final AuditoriaSeguridad auditoria;

    public PerfilService(PerfilRepository perfiles, PermisoRepository permisos, AuditoriaSeguridad auditoria) {
        this.perfiles = perfiles;
        this.permisos = permisos;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<PerfilResponse> listarPerfiles() {
        return perfiles.findAll().stream().map(PerfilResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> listarPermisos() {
        return permisos.findAllByOrderByCodigoAsc().stream().map(PermisoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> permisosDe(Long perfilId) {
        return buscarPerfil(perfilId).getPermisos().stream()
                .sorted(java.util.Comparator.comparing(Permiso::getCodigo))
                .map(PermisoResponse::desde).toList();
    }

    @Transactional
    public List<PermisoResponse> reemplazarPermisos(Long perfilId, List<Long> permisosIds) {
        var perfil = buscarPerfil(perfilId);
        var ids = new HashSet<>(permisosIds);
        if (ids.size() != permisosIds.size()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, CodigoError.VALIDACION,
                    "La lista contiene permisos duplicados");
        }
        var nuevos = permisos.findAllById(ids);
        if (nuevos.size() != ids.size()) {
            throw new NegocioException(HttpStatus.NOT_FOUND, CodigoError.NO_ENCONTRADO,
                    "Uno o mas permisos no existen");
        }
        if (PERFIL_ADMINISTRADOR.equals(perfil.getCodigo())
                && nuevos.stream().noneMatch(permiso -> PERMISO_GESTION_SEGURIDAD.equals(permiso.getCodigo()))) {
            throw new NegocioException(HttpStatus.CONFLICT, CodigoError.PERMISOS_ADMIN_REQUERIDOS,
                    "El perfil ADMINISTRADOR debe conservar el permiso de gestion de seguridad");
        }
        perfil.getPermisos().clear();
        perfil.getPermisos().addAll(nuevos);
        auditoria.cambioPermisos(perfil.getCodigo());
        return nuevos.stream().sorted(java.util.Comparator.comparing(Permiso::getCodigo))
                .map(PermisoResponse::desde).toList();
    }

    private co.edu.konrad.consultaciudadano.usuario.entity.Perfil buscarPerfil(Long perfilId) {
        return perfiles.findById(perfilId).orElseThrow(() ->
                new NegocioException(HttpStatus.NOT_FOUND, CodigoError.NO_ENCONTRADO, "Perfil no encontrado"));
    }
}
