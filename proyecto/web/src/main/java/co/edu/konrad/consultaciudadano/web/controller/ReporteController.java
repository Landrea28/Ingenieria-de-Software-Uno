package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.dto.EstadoAntecedente;
import co.edu.konrad.consultaciudadano.web.service.ApiNoDisponibleException;
import co.edu.konrad.consultaciudadano.web.service.ReporteApiClient;
import co.edu.konrad.consultaciudadano.web.service.ReporteSinResultadosException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Pantalla de reportes. El navegador no tiene el token, así que la web pide el PDF a la API
 * (GET /api/reportes/ciudadanos) y se lo entrega al navegador para verlo o descargarlo.
 */
@Controller
public class ReporteController {

    private final ReporteApiClient reportes;

    public ReporteController(ReporteApiClient reportes) {
        this.reportes = reportes;
    }

    /** 'resultado' llega cuando /reportes/pdf no pudo entregar el PDF: vacio | error. */
    @GetMapping("/reportes")
    public String reportes(@RequestParam(required = false) String estado,
            @RequestParam(required = false) String resultado, Model model) {
        Optional<EstadoAntecedente> seleccionado = EstadoAntecedente.desde(estado);
        model.addAttribute("estados", EstadoAntecedente.values());
        model.addAttribute("estadoSeleccionado", seleccionado.orElse(null));
        if ("vacio".equals(resultado) && seleccionado.isPresent()) {
            model.addAttribute("mensajeInfo", new ReporteSinResultadosException(seleccionado.get()).getMessage());
        } else if ("error".equals(resultado)) {
            model.addAttribute("mensajeError", ApiNoDisponibleException.MENSAJE + " No se generó el reporte.");
        } else if ("estado".equals(resultado)) {
            model.addAttribute("mensajeError", "Seleccione un estado: Requerido o No requerido.");
        }
        return "reportes";
    }

    /**
     * Devuelve el PDF: modo=ver lo abre en el navegador; modo=descargar lo guarda como archivo.
     * Si no hay PDF, vuelve a /reportes con el motivo. 401/403 los maneja ManejoErroresApi.
     */
    @GetMapping("/reportes/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "descargar") String modo) {
        Optional<EstadoAntecedente> seleccionado = EstadoAntecedente.desde(estado);
        if (seleccionado.isEmpty()) {
            return volver(null, "estado");
        }
        byte[] pdf;
        try {
            pdf = reportes.ciudadanosPorEstado(seleccionado.get());
        } catch (ReporteSinResultadosException e) {
            return volver(seleccionado.get(), "vacio");
        } catch (ApiNoDisponibleException e) {
            return volver(seleccionado.get(), "error");
        }
        String archivo = "ciudadanos-" + seleccionado.get().name().toLowerCase(Locale.ROOT).replace('_', '-')
                + "-" + LocalDate.now() + ".pdf";
        ContentDisposition disposicion = ("ver".equals(modo) ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(archivo).build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                // Contiene datos personales: que el navegador no lo guarde en caché.
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(pdf);
    }

    // 303: el navegador vuelve a la pantalla con GET mostrando el motivo.
    private static ResponseEntity<byte[]> volver(EstadoAntecedente estado, String resultado) {
        UriComponentsBuilder url = ServletUriComponentsBuilder.fromCurrentContextPath().path("/reportes");
        if (estado != null) {
            url.queryParam("estado", estado.name());
        }
        url.queryParam("resultado", resultado);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(url.build().toUri()).build();
    }
}
