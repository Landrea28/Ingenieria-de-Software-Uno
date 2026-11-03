package co.edu.konrad.consultaciudadano.usuario.repository;

import co.edu.konrad.consultaciudadano.usuario.entity.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @EntityGraph(attributePaths = "perfiles")
    Optional<Usuario> findByIdentificacion(String identificacion);
    @Query("select usuario.passwordHash from Usuario usuario where usuario.identificacion = :identificacion")
    Optional<String> buscarPasswordHash(@Param("identificacion") String identificacion);
    boolean existsByIdentificacion(String identificacion);
    boolean existsByCorreoIgnoreCase(String correo);
}
