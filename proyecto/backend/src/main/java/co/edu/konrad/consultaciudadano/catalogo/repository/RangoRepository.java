package co.edu.konrad.consultaciudadano.catalogo.repository;

import co.edu.konrad.consultaciudadano.catalogo.entity.Rango;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RangoRepository extends JpaRepository<Rango, Long> {
    List<Rango> findByFuerzaCodigoOrderByNombre(String codigo);
}
