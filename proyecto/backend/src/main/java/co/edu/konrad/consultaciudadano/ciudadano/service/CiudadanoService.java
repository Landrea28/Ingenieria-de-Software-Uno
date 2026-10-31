package co.edu.konrad.consultaciudadano.ciudadano.service;

import co.edu.konrad.consultaciudadano.ciudadano.dto.CiudadanoRequest;
import co.edu.konrad.consultaciudadano.ciudadano.dto.CiudadanoResponse;
import co.edu.konrad.consultaciudadano.ciudadano.dto.CiudadanoUpdateRequest;
import co.edu.konrad.consultaciudadano.ciudadano.entity.Ciudadano;
import co.edu.konrad.consultaciudadano.ciudadano.repository.CiudadanoRepository;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CiudadanoService {
    private static final String IDENTIFICACION_DUPLICADA = "Identificacion ya registrada";

    private final CiudadanoRepository repository;

    @Transactional
    public CiudadanoResponse crear(CiudadanoRequest request) {
        var datos = request.datos();
        validarFechas(datos);
        if (repository.existsByIdentificacion(request.identificacion())) {
            throw duplicado();
        }
        var ciudadano = new Ciudadano(request.identificacion());
        aplicar(ciudadano, datos);
        try {
            return CiudadanoResponse.desde(repository.saveAndFlush(ciudadano));
        } catch (DataIntegrityViolationException error) {
            throw duplicado();
        }
    }

    @Transactional(readOnly = true)
    public CiudadanoResponse buscar(String identificacion) {
        return CiudadanoResponse.desde(obtener(identificacion));
    }

    @Transactional(readOnly = true)
    public Page<CiudadanoResponse> listar(String busqueda, int pagina, int tamano) {
        var paginacion = PageRequest.of(pagina, tamano, Sort.by("apellidos", "nombres"));
        if (busqueda == null || busqueda.isBlank()) {
            return repository.findAll(paginacion).map(CiudadanoResponse::desde);
        }
        var criterio = busqueda.trim();
        return repository.findByIdentificacionContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                criterio, criterio, criterio, paginacion).map(CiudadanoResponse::desde);
    }

    @Transactional
    public CiudadanoResponse actualizar(String identificacion, CiudadanoUpdateRequest request) {
        validarFechas(request);
        var ciudadano = obtener(identificacion);
        aplicar(ciudadano, request);
        return CiudadanoResponse.desde(repository.save(ciudadano));
    }

    private Ciudadano obtener(String identificacion) {
        return repository.findByIdentificacion(identificacion).orElseThrow(() ->
                new NegocioException(HttpStatus.NOT_FOUND, CodigoError.NO_ENCONTRADO, "Ciudadano no encontrado"));
    }

    private NegocioException duplicado() {
        return new NegocioException(HttpStatus.CONFLICT, CodigoError.DUPLICADO, IDENTIFICACION_DUPLICADA);
    }

    private void validarFechas(CiudadanoUpdateRequest datos) {
        if (!datos.fechaExpedicion().isAfter(datos.fechaNacimiento())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, CodigoError.VALIDACION,
                    "La fecha de expedicion debe ser posterior a la fecha de nacimiento");
        }
    }

    private void aplicar(Ciudadano ciudadano, CiudadanoUpdateRequest datos) {
        ciudadano.setNombres(datos.nombres().trim());
        ciudadano.setApellidos(datos.apellidos().trim());
        ciudadano.setFechaNacimiento(datos.fechaNacimiento());
        ciudadano.setLugarNacimiento(datos.lugarNacimiento().trim());
        ciudadano.setFechaExpedicion(datos.fechaExpedicion());
        ciudadano.setLugarExpedicion(datos.lugarExpedicion().trim());
        ciudadano.setRh(datos.rh());
        ciudadano.setGrupoSanguineo(datos.grupoSanguineo());
        ciudadano.setEstatura(datos.estatura());
    }
}
