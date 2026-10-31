package co.edu.konrad.consultaciudadano.usuario.repository;

import co.edu.konrad.consultaciudadano.usuario.entity.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByIdentificacion(String identificacion);
    boolean existsByIdentificacion(String identificacion);
    boolean existsByCorreoIgnoreCase(String correo);
}
