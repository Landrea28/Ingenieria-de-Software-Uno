package co.edu.konrad.consultaciudadano.web.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF mínimo de una página, hecho a mano, solo para el perfil "mock" (el real lo genera el backend).
 * Un PDF es texto con objetos numerados y, al final, una tabla 'xref' con la posición en bytes de cada objeto.
 */
final class PdfSimulado {

    // Helvetica con WinAnsiEncoding: permite tildes y ñ usando la codificación windows-1252.
    private static final Charset WIN_ANSI = Charset.forName("windows-1252");
    private static final int MAX_LINEAS = 45;

    private PdfSimulado() {}

    static byte[] generar(String titulo, List<String> lineas) {
        StringBuilder contenido = new StringBuilder();
        contenido.append("BT /F1 16 Tf 50 790 Td (").append(escapar(titulo)).append(") Tj ET\n");
        int y = 760;
        for (String linea : lineas.subList(0, Math.min(lineas.size(), MAX_LINEAS))) {
            contenido.append("BT /F1 11 Tf 50 ").append(y).append(" Td (").append(escapar(linea)).append(") Tj ET\n");
            y -= 16;
        }
        byte[] flujo = contenido.toString().getBytes(WIN_ANSI);

        List<byte[]> objetos = new ArrayList<>();
        objetos.add(ascii("<< /Type /Catalog /Pages 2 0 R >>"));
        objetos.add(ascii("<< /Type /Pages /Kids [3 0 R] /Count 1 >>"));
        objetos.add(ascii("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                + "/Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>"));
        objetos.add(concatenar(ascii("<< /Length " + flujo.length + " >>\nstream\n"), flujo, ascii("\nendstream")));
        objetos.add(ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"));

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        escribir(pdf, ascii("%PDF-1.4\n"));
        List<Integer> posiciones = new ArrayList<>();
        for (int i = 0; i < objetos.size(); i++) {
            posiciones.add(pdf.size());
            escribir(pdf, ascii((i + 1) + " 0 obj\n"));
            escribir(pdf, objetos.get(i));
            escribir(pdf, ascii("\nendobj\n"));
        }
        int inicioXref = pdf.size();
        StringBuilder xref = new StringBuilder("xref\n0 " + (objetos.size() + 1) + "\n0000000000 65535 f \n");
        posiciones.forEach(p -> xref.append(String.format("%010d 00000 n \n", p))); // cada entrada: 20 bytes
        xref.append("trailer\n<< /Size ").append(objetos.size() + 1).append(" /Root 1 0 R >>\n")
                .append("startxref\n").append(inicioXref).append("\n%%EOF\n");
        escribir(pdf, ascii(xref.toString()));
        return pdf.toByteArray();
    }

    // En las cadenas PDF, '(' ')' y '\' tienen significado especial.
    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private static byte[] ascii(String texto) {
        return texto.getBytes(WIN_ANSI);
    }

    private static byte[] concatenar(byte[]... partes) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        for (byte[] parte : partes) {
            escribir(salida, parte);
        }
        return salida.toByteArray();
    }

    private static void escribir(ByteArrayOutputStream salida, byte[] bytes) {
        salida.write(bytes, 0, bytes.length);
    }
}
