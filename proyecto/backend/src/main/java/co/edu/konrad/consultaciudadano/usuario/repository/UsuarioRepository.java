package co.edu.konrad.consultaciudadano.usuario.repository;

import co.edu.konrad.consultaciudadano.usuario.entity.Usuario;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @EntityGraph(attributePaths = "perfiles")
    Optional<Usuario> findByIdentificacion(String identificacion);
    @Query("select usuario.passwordHash from Usuario usuario where usuario.identificacion = :identificacion")
    Optional<String> buscarPasswordHash(@Param("identificacion") String identificacion);
    boolean existsByIdentificacion(String identificacion);
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCaseAndIdentificacionNot(String correo, String identificacion);

    @Query(value = """
            select distinct usuario from Usuario usuario left join usuario.perfiles perfil
            where (:activo is null or usuario.activo = :activo)
              and (:perfilCodigo is null or perfil.codigo = :perfilCodigo)
            """, countQuery = """
            select count(distinct usuario) from Usuario usuario left join usuario.perfiles perfil
            where (:activo is null or usuario.activo = :activo)
              and (:perfilCodigo is null or perfil.codigo = :perfilCodigo)
            """)
    Page<Usuario> buscarUsuarios(@Param("activo") Boolean activo, @Param("perfilCodigo") String perfilCodigo,
            Pageable pageable);

    @Query("""
            select count(distinct usuario) from Usuario usuario join usuario.perfiles perfil
            where usuario.activo = true and perfil.codigo = :perfilCodigo
            """)
    long contarActivosConPerfil(@Param("perfilCodigo") String perfilCodigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select usuario from Usuario usuario join usuario.perfiles perfil
            where usuario.activo = true and perfil.codigo = 'ADMINISTRADOR'
            order by usuario.id
            """)
    java.util.List<Usuario> bloquearAdministradoresActivos();
}
