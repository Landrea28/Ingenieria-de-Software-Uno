package co.edu.konrad.consultaciudadano.antecedente.repository;

import co.edu.konrad.consultaciudadano.antecedente.entity.Antecedente;
import co.edu.konrad.consultaciudadano.antecedente.entity.EstadoAntecedente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AntecedenteRepository extends JpaRepository<Antecedente, Long> {
    @Query("select a from Antecedente a join fetch a.ciudadano c where c.identificacion = :identificacion")
    Optional<Antecedente> buscarPorIdentificacion(@Param("identificacion") String identificacion);

    @Query("select a from Antecedente a join fetch a.ciudadano c where a.estado = :estado order by c.apellidos, c.nombres")
    List<Antecedente> listarPorEstado(@Param("estado") EstadoAntecedente estado);
}
