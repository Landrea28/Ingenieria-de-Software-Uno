package co.edu.konrad.consultaciudadano;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConsultaCiudadanoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsultaCiudadanoApplication.class, args);
    }
}
