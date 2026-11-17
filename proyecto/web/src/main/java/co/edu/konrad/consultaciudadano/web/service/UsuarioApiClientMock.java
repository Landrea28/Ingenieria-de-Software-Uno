package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.PaginaUsuarios;
import co.edu.konrad.consultaciudadano.web.dto.PerfilDto;
import co.edu.konrad.consultaciudadano.web.dto.UsuarioDatos;
import co.edu.konrad.consultaciudadano.web.dto.UsuarioDto;
import co.edu.konrad.consultaciudadano.web.sesion.SesionAdmin;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * DATOS SIMULADOS DE PRUEBA. Solo se activa con el perfil "mock". Usuarios ficticios en memoria.
 * Imita las reglas del backend (UsuarioService): identificación y correo únicos, un administrador
 * no puede desactivarse ni quitarse su perfil, y debe quedar al menos un administrador activo.
 */
@Service
@Profile("mock")
public class UsuarioApiClientMock implements UsuarioApiClient {

    private final List<UsuarioDto> usuarios = new CopyOnWriteArrayList<>(List.of(
            new UsuarioDto("1000000001", "Admin", "Simulado Mock", "admin.mock@example.com",
                    PerfilDto.ADMINISTRADOR, UsuarioDto.ACTIVO),
            new UsuarioDto("1000000002", "Agente Uno", "Simulado Mock", "agente1.mock@example.com",
                    "PERSONAL_FUERZA_PUBLICA", UsuarioDto.ACTIVO),
            new UsuarioDto("1000000003", "Agente Dos", "Simulado Mock", "agente2.mock@example.com",
                    "PERSONAL_FUERZA_PUBLICA", UsuarioDto.INACTIVO)));

    private final SesionAdmin sesion;

    public UsuarioApiClientMock(SesionAdmin sesion) {
        this.sesion = sesion;
    }

    @Override
    public PaginaUsuarios listar(String estado, String perfil, int pagina) {
        List<UsuarioDto> filtrados = usuarios.stream()
                .filter(u -> estado == null || estado.equals(u.estado()))
                .filter(u -> perfil == null || perfil.equals(u.perfil()))
                .sorted(Comparator.comparing(UsuarioDto::identificacion))
                .toList();
        int total = filtrados.size();
        int desde = Math.min(pagina * TAMANO_PAGINA, total);
        int hasta = Math.min(desde + TAMANO_PAGINA, total);
        int totalPaginas = (total + TAMANO_PAGINA - 1) / TAMANO_PAGINA;
        return new PaginaUsuarios(filtrados.subList(desde, hasta), total, totalPaginas, TAMANO_PAGINA, pagina,
                pagina == 0, pagina >= totalPaginas - 1);
    }

    @Override
    public Optional<UsuarioDto> buscar(String identificacion) {
        return usuarios.stream().filter(u -> u.identificacion().equals(identificacion)).findFirst();
    }

    @Override
    public synchronized UsuarioDto crear(UsuarioDatos d) {
        if (buscar(d.identificacion()).isPresent()) {
            throw new ApiConflictoException("La identificacion ya esta registrada");
        }
        if (correoEnUso(d.correo(), d.identificacion())) {
            throw new ApiConflictoException("El correo ya esta registrado");
        }
        UsuarioDto creado = new UsuarioDto(d.identificacion(), d.nombres(), d.apellidos(), d.correo(),
                codigoPerfil(d.perfilId()), UsuarioDto.ACTIVO);
        usuarios.add(creado);
        return creado;
    }

    @Override
    public synchronized UsuarioDto actualizar(String identificacion, UsuarioDatos d) {
        UsuarioDto actual = buscar(identificacion)
                .orElseThrow(() -> new ApiNoEncontradoException("Usuario no encontrado"));
        if (correoEnUso(d.correo(), identificacion)) {
            throw new ApiConflictoException("El correo ya esta registrado");
        }
        String perfil = codigoPerfil(d.perfilId());
        boolean seguiraSiendoAdminActivo = UsuarioDto.ACTIVO.equals(d.estado()) && PerfilDto.ADMINISTRADOR.equals(perfil);
        if (identificacion.equals(sesion.getIdentificacion()) && !seguiraSiendoAdminActivo) {
            throw new ApiConflictoException("Un administrador no puede desactivarse ni quitarse su perfil",
                    ApiConflictoException.AUTODESACTIVACION);
        }
        boolean eraAdminActivo = actual.activo() && PerfilDto.ADMINISTRADOR.equals(actual.perfil());
        long adminsActivos = usuarios.stream()
                .filter(u -> u.activo() && PerfilDto.ADMINISTRADOR.equals(u.perfil())).count();
        if (eraAdminActivo && !seguiraSiendoAdminActivo && adminsActivos <= 1) {
            throw new ApiConflictoException("Debe permanecer al menos un administrador activo",
                    ApiConflictoException.ULTIMO_ADMIN);
        }
        UsuarioDto actualizado = new UsuarioDto(identificacion, d.nombres(), d.apellidos(), d.correo(), perfil, d.estado());
        usuarios.set(usuarios.indexOf(actual), actualizado);
        return actualizado;
    }

    private boolean correoEnUso(String correo, String exceptoIdentificacion) {
        return usuarios.stream().anyMatch(u -> u.correo().equalsIgnoreCase(correo)
                && !u.identificacion().equals(exceptoIdentificacion));
    }

    private static String codigoPerfil(Long perfilId) {
        return PerfilApiClientMock.PERFILES.stream().filter(p -> p.id().equals(perfilId)).findFirst()
                .map(PerfilDto::codigo)
                .orElseThrow(() -> new ApiNoEncontradoException("Perfil no encontrado"));
    }
}
