package co.edu.konrad.consultaciudadano.usuario.service;

import co.edu.konrad.consultaciudadano.usuario.entity.Usuario;
import co.edu.konrad.consultaciudadano.usuario.repository.PerfilRepository;
import co.edu.konrad.consultaciudadano.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminInicialService {
    private final UsuarioRepository usuarios;
    private final PerfilRepository perfiles;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public boolean crearSiNoExiste(String identificacion, String correo, String contrasena) {
        if (usuarios.existsByIdentificacion(identificacion)) {
            return false;
        }
        var perfil = perfiles.findByCodigo("ADMINISTRADOR")
                .orElseThrow(() -> new IllegalStateException("Falta el perfil ADMINISTRADOR en la base de datos"));
        var admin = new Usuario(identificacion, "Administrador", "Inicial", correo,
                null, null, null, passwordEncoder.encode(contrasena));
        admin.getPerfiles().add(perfil);
        usuarios.save(admin);
        return true;
    }
}
