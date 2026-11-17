package co.edu.konrad.consultaciudadano.catalogo.repository;

import co.edu.konrad.consultaciudadano.catalogo.entity.FuerzaPublica;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos del catálogo de fuerzas públicas.
 */
public interface FuerzaPublicaRepository extends JpaRepository<FuerzaPublica, String> {}
