package co.edu.konrad.consultaciudadano.usuario.controller;

import co.edu.konrad.consultaciudadano.usuario.dto.ActualizarPermisosRequest;
import co.edu.konrad.consultaciudadano.usuario.dto.PermisoResponse;
import co.edu.konrad.consultaciudadano.usuario.dto.PerfilResponse;
import co.edu.konrad.consultaciudadano.usuario.service.PerfilService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PerfilController {
    private final PerfilService service;

    @GetMapping("/perfiles")
    public List<PerfilResponse> listarPerfiles() {
        return service.listarPerfiles();
    }

    @GetMapping("/permisos")
    public List<PermisoResponse> listarPermisos() {
        return service.listarPermisos();
    }

    @GetMapping("/perfiles/{id}/permisos")
    public List<PermisoResponse> permisos(@PathVariable @Positive Long id) {
        return service.permisosDe(id);
    }

    @PutMapping("/perfiles/{id}/permisos")
    public List<PermisoResponse> reemplazarPermisos(@PathVariable @Positive Long id,
            @Valid @RequestBody ActualizarPermisosRequest request) {
        return service.reemplazarPermisos(id, request.permisosIds());
    }
}
