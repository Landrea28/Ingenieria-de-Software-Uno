package co.edu.konrad.consultaciudadano.antecedente.service;

import co.edu.konrad.consultaciudadano.antecedente.dto.AntecedenteResponse;
import co.edu.konrad.consultaciudadano.antecedente.entity.Antecedente;
import co.edu.konrad.consultaciudadano.antecedente.entity.EstadoAntecedente;
import co.edu.konrad.consultaciudadano.antecedente.repository.AntecedenteRepository;
import co.edu.konrad.consultaciudadano.ciudadano.repository.CiudadanoRepository;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AntecedenteService {
    private final AntecedenteRepository antecedentes;
    private final CiudadanoRepository ciudadanos;

    @Transactional(readOnly = true)
    public AntecedenteResponse consultar(String identificacion) {
        return antecedentes.buscarPorIdentificacion(identificacion)
                .map(AntecedenteResponse::desde)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, CodigoError.SIN_INFORMACION,
                        "No hay informacion de antecedentes para esta identificacion"));
    }

    @Transactional
    public AntecedenteResponse establecer(String identificacion, EstadoAntecedente estado) {
        var ciudadano = ciudadanos.findByIdentificacion(identificacion).orElseThrow(() ->
                new NegocioException(HttpStatus.NOT_FOUND, CodigoError.NO_ENCONTRADO, "Ciudadano no encontrado"));
        var antecedente = antecedentes.buscarPorIdentificacion(identificacion).orElse(null);
        if (antecedente == null) {
            antecedente = new Antecedente(ciudadano, estado);
        } else {
            antecedente.cambiarEstado(estado);
        }
        return AntecedenteResponse.desde(antecedentes.save(antecedente));
    }
}
