package co.edu.konrad.consultaciudadano.usuario.repository;

import co.edu.konrad.consultaciudadano.usuario.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    List<Permiso> findAllByOrderByCodigoAsc();
}
