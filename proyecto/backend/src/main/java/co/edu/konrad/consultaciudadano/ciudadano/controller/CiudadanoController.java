package co.edu.konrad.consultaciudadano.ciudadano.controller;

import co.edu.konrad.consultaciudadano.ciudadano.dto.CiudadanoRequest;
import co.edu.konrad.consultaciudadano.ciudadano.dto.CiudadanoResponse;
import co.edu.konrad.consultaciudadano.ciudadano.dto.CiudadanoUpdateRequest;
import co.edu.konrad.consultaciudadano.ciudadano.service.CiudadanoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ciudadanos")
@RequiredArgsConstructor
public class CiudadanoController {
    private final CiudadanoService service;

    @GetMapping
    public Page<CiudadanoResponse> listar(@RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamano) {
        return service.listar(busqueda, pagina, tamano);
    }

    @PostMapping
    public ResponseEntity<CiudadanoResponse> crear(@Valid @RequestBody CiudadanoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping("/{identificacion}")
    public CiudadanoResponse buscar(@PathVariable @Pattern(regexp = "[0-9]{6,10}") String identificacion) {
        return service.buscar(identificacion);
    }

    @PutMapping("/{identificacion}")
    public CiudadanoResponse actualizar(@PathVariable @Pattern(regexp = "[0-9]{6,10}") String identificacion,
            @Valid @RequestBody CiudadanoUpdateRequest request) {
        return service.actualizar(identificacion, request);
    }
}
