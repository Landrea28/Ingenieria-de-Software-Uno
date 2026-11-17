package co.edu.konrad.consultaciudadano.config;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CorsConfigTest {
    @Test
    void rechazaOrigenesCorsConComodin() {
        assertThrows(IllegalArgumentException.class, () -> new CorsConfig("https://app.example,*"));
    }
}
