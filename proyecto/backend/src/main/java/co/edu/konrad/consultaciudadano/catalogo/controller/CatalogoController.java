package co.edu.konrad.consultaciudadano.catalogo.controller;

import co.edu.konrad.consultaciudadano.catalogo.dto.FuerzaResponse;
import co.edu.konrad.consultaciudadano.catalogo.service.CatalogoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {
    private final CatalogoService service;

    @GetMapping("/fuerzas-rangos")
    public List<FuerzaResponse> consultar() { return service.consultar(); }
}
