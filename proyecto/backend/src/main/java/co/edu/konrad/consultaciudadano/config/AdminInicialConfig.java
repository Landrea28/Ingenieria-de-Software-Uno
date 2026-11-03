package co.edu.konrad.consultaciudadano.config;

import co.edu.konrad.consultaciudadano.usuario.service.AdminInicialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminInicialConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminInicialConfig.class);

    @Bean
    ApplicationRunner crearAdminInicial(AdminInicialService servicio,
            @Value("${admin.initial.identificacion:}") String identificacion,
            @Value("${admin.initial.email:}") String correo,
            @Value("${admin.initial.password:}") String contrasena) {
        return args -> {
            if (identificacion.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                LOGGER.warn("Admin inicial omitido: configure ADMIN_INITIAL_IDENTIFICACION, "
                        + "ADMIN_INITIAL_EMAIL y ADMIN_INITIAL_PASSWORD");
                return;
            }
            if (servicio.crearSiNoExiste(identificacion, correo, contrasena)) {
                LOGGER.info("Cuenta de administrador inicial creada con contraseña hash");
            }
        };
    }
}
