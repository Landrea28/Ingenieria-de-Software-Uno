package co.edu.konrad.consultaciudadano.reporte.controller;

import co.edu.konrad.consultaciudadano.antecedente.entity.EstadoAntecedente;
import co.edu.konrad.consultaciudadano.reporte.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {
    private final ReporteService service;

    @GetMapping("/ciudadanos")
    public ResponseEntity<byte[]> generar(@RequestParam EstadoAntecedente estado) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ciudadanos-" + estado.name().toLowerCase() + ".pdf")
                .body(service.generar(estado));
    }
}
