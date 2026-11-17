package co.edu.konrad.consultaciudadano.web.dto;

import java.util.List;

/** Página de usuarios tal como la devuelve GET /api/usuarios. 'number' es la página actual, base 0. */
public record PaginaUsuarios(
        List<UsuarioDto> content,
        long totalElements,
        int totalPages,
        int size,
        int number,
        boolean first,
        boolean last) {

    public boolean vacia() {
        return content == null || content.isEmpty();
    }

    /** Posición (base 1) del primer elemento mostrado, para "Mostrando X–Y de N". */
    public long desde() {
        return vacia() ? 0 : (long) number * size + 1;
    }

    public long hasta() {
        return vacia() ? 0 : desde() + content.size() - 1;
    }
}
