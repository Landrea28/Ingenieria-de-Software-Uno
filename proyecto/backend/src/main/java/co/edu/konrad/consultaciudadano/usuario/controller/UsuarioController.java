package co.edu.konrad.consultaciudadano.usuario.controller;

import co.edu.konrad.consultaciudadano.usuario.dto.ActualizarUsuarioRequest;
import co.edu.konrad.consultaciudadano.usuario.dto.CrearUsuarioRequest;
import co.edu.konrad.consultaciudadano.usuario.dto.EstadoUsuario;
import co.edu.konrad.consultaciudadano.usuario.dto.UsuarioResponse;
import co.edu.konrad.consultaciudadano.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService service;

    @GetMapping
    public Page<UsuarioResponse> listar(
            @RequestParam(required = false) EstadoUsuario estado,
            @RequestParam(required = false) @Size(max = 50) String perfil,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.listar(estado, perfil, page, size);
    }

    @GetMapping("/{identificacion}")
    public UsuarioResponse buscar(@PathVariable @Pattern(regexp = "[0-9]{6,10}") String identificacion) {
        return service.buscar(identificacion);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody CrearUsuarioRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{identificacion}")
    public UsuarioResponse actualizar(@PathVariable @Pattern(regexp = "[0-9]{6,10}") String identificacion,
            @Valid @RequestBody ActualizarUsuarioRequest request) {
        return service.actualizar(identificacion, request);
    }
}
