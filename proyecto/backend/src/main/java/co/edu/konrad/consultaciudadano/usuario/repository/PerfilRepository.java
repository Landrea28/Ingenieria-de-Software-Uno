package co.edu.konrad.consultaciudadano.usuario.repository;

import co.edu.konrad.consultaciudadano.usuario.entity.Perfil;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByCodigo(String codigo);

    @Override
    @EntityGraph(attributePaths = "permisos")
    java.util.List<Perfil> findAll();

    @Override
    @EntityGraph(attributePaths = "permisos")
    Optional<Perfil> findById(Long id);
}
