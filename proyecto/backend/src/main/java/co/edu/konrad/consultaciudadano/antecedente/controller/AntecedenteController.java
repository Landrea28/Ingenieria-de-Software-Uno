package co.edu.konrad.consultaciudadano.antecedente.controller;

import co.edu.konrad.consultaciudadano.antecedente.dto.AntecedenteResponse;
import co.edu.konrad.consultaciudadano.antecedente.dto.CambiarAntecedenteRequest;
import co.edu.konrad.consultaciudadano.antecedente.service.AntecedenteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de antecedentes ({@code /api/antecedentes}): consulta el estado de un ciudadano y, solo para demostraciones, permite establecerlo.
 * No contiene reglas de negocio: delega en {@link AntecedenteService}.
 */
@Validated
@RestController
@RequestMapping("/api/antecedentes")
@RequiredArgsConstructor
public class AntecedenteController {
    private final AntecedenteService service;

    @GetMapping("/{identificacion}")
    @PreAuthorize("hasAnyRole('PERSONAL_FUERZA_PUBLICA', 'ADMINISTRADOR')")
    public AntecedenteResponse consultar(@PathVariable @Pattern(regexp = "[0-9]{6,10}") String identificacion) {
        return service.consultar(identificacion);
    }

    @PutMapping("/{identificacion}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AntecedenteResponse establecer(@PathVariable @Pattern(regexp = "[0-9]{6,10}") String identificacion,
            @Valid @RequestBody CambiarAntecedenteRequest request) {
        return service.establecer(identificacion, request.estado());
    }
}
