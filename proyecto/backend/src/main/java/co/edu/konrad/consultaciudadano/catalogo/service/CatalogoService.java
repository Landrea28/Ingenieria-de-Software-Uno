package co.edu.konrad.consultaciudadano.catalogo.service;

import co.edu.konrad.consultaciudadano.catalogo.dto.FuerzaResponse;
import co.edu.konrad.consultaciudadano.catalogo.dto.RangoResponse;
import co.edu.konrad.consultaciudadano.catalogo.repository.FuerzaPublicaRepository;
import co.edu.konrad.consultaciudadano.catalogo.repository.RangoRepository;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogoService {
    private final FuerzaPublicaRepository fuerzas;
    private final RangoRepository rangos;

    @Transactional(readOnly = true)
    public List<FuerzaResponse> consultar() {
        return fuerzas.findAll().stream().map(fuerza ->
                new FuerzaResponse(fuerza.getCodigo(), fuerza.getNombre(), rangos
                        .findByFuerzaCodigoOrderByNombre(fuerza.getCodigo()).stream()
                        .map(rango -> new RangoResponse(rango.getId(), rango.getNombre())).toList())).toList();
    }

    @Transactional(readOnly = true)
    public void validar(String fuerzaCodigo, Long rangoId) {
        if (!fuerzas.existsById(fuerzaCodigo) || rangos.findById(rangoId)
                .filter(rango -> rango.getFuerza().getCodigo().equals(fuerzaCodigo)).isEmpty()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, CodigoError.VALIDACION,
                    "Fuerza publica y rango no coinciden");
        }
    }
}
