package SystemITR.JosueGuinea.modules.authentication.service;

import SystemITR.JosueGuinea.modules.authentication.model.dto.AuthRequestDTO;
import SystemITR.JosueGuinea.modules.authentication.model.dto.AuthResponseDTO;
import SystemITR.JosueGuinea.modules.authentication.model.entity.UsuarioEntity;
import SystemITR.JosueGuinea.modules.authentication.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repoUsuario;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDTO login(AuthRequestDTO request) {
        //Validar la existencia del usuario
        UsuarioEntity objUsuario = repoUsuario.findByUsername(request.getUsername())
                .orElse(null);
        if (objUsuario != null){
            //Validar que el usuario este activo
            if ("ACTIVO".equals(objUsuario.getEstado())){
                //Validar que la contraseña sea la correcta
                if (passwordEncoder.matches(request.getPassword(), objUsuario.getPasswordHash())){
                    return new AuthResponseDTO(
                            objUsuario.getUsuarioId(),
                            objUsuario.getUsername(),
                            objUsuario.getRol().getNombreRol(),
                            "Autenticación exitosa"
                    );
                }
            }
        }
        return null;
    }
}
