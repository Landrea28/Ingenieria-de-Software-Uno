package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.PerfilDto;
import co.edu.konrad.consultaciudadano.web.dto.PermisoDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * DATOS SIMULADOS DE PRUEBA. Solo se activa con el perfil "mock".
 * Mismo catálogo que carga el backend (2 perfiles y 7 permisos); los cambios viven en memoria.
 */
@Service
@Profile("mock")
public class PerfilApiClientMock implements PerfilApiClient {

    static final List<PerfilDto> PERFILES = List.of(
            new PerfilDto(1L, "ADMINISTRADOR", "Administrador"),
            new PerfilDto(2L, "PERSONAL_FUERZA_PUBLICA", "Personal de fuerza publica"));

    private static final List<PermisoDto> PERMISOS = List.of(
            new PermisoDto(1L, "CIUDADANOS_LEER", "Consultar ciudadanos"),
            new PermisoDto(2L, "CIUDADANOS_CREAR", "Registrar ciudadanos"),
            new PermisoDto(3L, "CIUDADANOS_ACTUALIZAR", "Actualizar ciudadanos"),
            new PermisoDto(4L, "ANTECEDENTES_CONSULTAR", "Consultar antecedentes"),
            new PermisoDto(5L, "ANTECEDENTES_ACTUALIZAR", "Actualizar estados de demostracion"),
            new PermisoDto(6L, "REPORTES_GENERAR", "Generar reportes"),
            new PermisoDto(7L, "SEGURIDAD_ADMINISTRAR", "Administrar usuarios y perfiles"));

    private static final long ID_SEGURIDAD = 7L;

    private final Map<Long, Set<Long>> asignados = new ConcurrentHashMap<>(Map.of(
            1L, new TreeSet<>(Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L)),
            2L, new TreeSet<>(Set.of(4L))));

    @Override
    public List<PerfilDto> perfiles() {
        return PERFILES;
    }

    @Override
    public List<PermisoDto> permisos() {
        return PERMISOS;
    }

    @Override
    public List<PermisoDto> permisosDe(long perfilId) {
        Set<Long> ids = asignados.get(perfilId);
        if (ids == null) {
            throw new ApiNoEncontradoException("Perfil no encontrado");
        }
        return PERMISOS.stream().filter(p -> ids.contains(p.id())).toList();
    }

    // Imita las reglas del backend (PerfilService.reemplazarPermisos).
    @Override
    public synchronized List<PermisoDto> reemplazarPermisos(long perfilId, List<Long> permisosIds) {
        if (!asignados.containsKey(perfilId)) {
            throw new ApiNoEncontradoException("Perfil no encontrado");
        }
        Set<Long> nuevos = new TreeSet<>(permisosIds);
        if (nuevos.stream().anyMatch(id -> PERMISOS.stream().noneMatch(p -> p.id().equals(id)))) {
            throw new ApiNoEncontradoException("Uno o mas permisos no existen");
        }
        if (perfilId == 1L && !nuevos.contains(ID_SEGURIDAD)) {
            throw new ApiConflictoException("El perfil ADMINISTRADOR debe conservar el permiso de gestion de seguridad",
                    ApiConflictoException.PERMISOS_ADMIN);
        }
        asignados.put(perfilId, nuevos);
        return permisosDe(perfilId);
    }
}
