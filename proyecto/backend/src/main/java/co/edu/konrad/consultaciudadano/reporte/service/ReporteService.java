package co.edu.konrad.consultaciudadano.reporte.service;

import co.edu.konrad.consultaciudadano.antecedente.entity.Antecedente;
import co.edu.konrad.consultaciudadano.antecedente.entity.EstadoAntecedente;
import co.edu.konrad.consultaciudadano.antecedente.repository.AntecedenteRepository;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReporteService {
    private static final String[] COLUMNAS = {"Identificacion", "Nombres", "Apellidos"};

    private final AntecedenteRepository repository;

    @Transactional(readOnly = true)
    public byte[] generar(EstadoAntecedente estado) {
        var registros = repository.listarPorEstado(estado);
        if (registros.isEmpty()) {
            throw new NegocioException(HttpStatus.NOT_FOUND, CodigoError.SIN_RESULTADOS,
                    "No hay ciudadanos para el estado solicitado");
        }
        return construirPdf(estado, registros);
    }

    private byte[] construirPdf(EstadoAntecedente estado, List<Antecedente> registros) {
        try (var salida = new ByteArrayOutputStream()) {
            var documento = new Document();
            PdfWriter.getInstance(documento, salida);
            documento.open();
            documento.add(new Paragraph("Sistema de Consulta Ciudadano"));
            documento.add(new Paragraph("Reporte de ciudadanos: " + estado.name()));
            documento.add(new Paragraph("Generado: " + LocalDate.now()));
            documento.add(new Paragraph("Datos ficticios de demostracion"));
            documento.add(new Paragraph(" "));
            documento.add(construirTabla(registros));
            documento.close();
            return salida.toByteArray();
        } catch (DocumentException | IOException error) {
            throw new NegocioException(HttpStatus.INTERNAL_SERVER_ERROR, CodigoError.ERROR_INTERNO,
                    "No fue posible generar el reporte");
        }
    }

    private PdfPTable construirTabla(List<Antecedente> registros) {
        var tabla = new PdfPTable(new float[]{2.5f, 4f, 4f});
        tabla.setWidthPercentage(100);
        for (var titulo : COLUMNAS) {
            var celda = new PdfPCell(new Phrase(titulo));
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(celda);
        }
        for (var registro : registros) {
            var ciudadano = registro.getCiudadano();
            tabla.addCell(ciudadano.getIdentificacion());
            tabla.addCell(ciudadano.getNombres());
            tabla.addCell(ciudadano.getApellidos());
        }
        return tabla;
    }
}
