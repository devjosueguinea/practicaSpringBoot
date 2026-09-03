package SystemITR.JosueGuinea.modules.authentication.controller;

import SystemITR.JosueGuinea.config.jwt.JwtUtils;
import SystemITR.JosueGuinea.modules.authentication.model.dto.AuthRequestDTO;
import SystemITR.JosueGuinea.modules.authentication.model.dto.AuthResponseDTO;
import SystemITR.JosueGuinea.modules.authentication.model.entity.UsuarioEntity;
import SystemITR.JosueGuinea.modules.authentication.service.AuthService;
import SystemITR.JosueGuinea.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService service;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login (@Valid @RequestBody AuthRequestDTO request, HttpServletResponse response){
        if (request.getUsername().trim() == null || request.getUsername().trim().isBlank() || request.getUsername().trim().isEmpty() ||
                request.getPassword().trim() == null || request.getPassword().trim().isBlank() || request.getPassword().trim().isEmpty()){
            ApiResponse<AuthResponseDTO> credencialesNoEncontradas = new ApiResponse<>(false, "Credenciales incompletas");
            return ResponseEntity.status(404).body(credencialesNoEncontradas);
        }

        AuthResponseDTO responseDTO = service.login(request);
        if (responseDTO != null){
            addTokenCookie(responseDTO, response);
            ApiResponse<AuthResponseDTO> AccesoValido = new ApiResponse<>(true, "Inicio de sesión exitoso", responseDTO);
            return ResponseEntity.ok(AccesoValido);
        }

        ApiResponse<AuthResponseDTO> AccesoInvalido = new ApiResponse<>(false, "Acceso denegado");
        return ResponseEntity.status(401).body(AccesoInvalido);
    }

    private void addTokenCookie(AuthResponseDTO dataToken, HttpServletResponse response) {
        String token = jwtUtils.create(
                String.valueOf(dataToken.getId()),
                dataToken.getUsername(),
                dataToken.getRol(),
                dataToken.getMensaje()
        );

        ResponseCookie cookie = ResponseCookie.from("authToken", token)
                        .path("/")
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Lax")
                        .maxAge(86400)
                        .build();

        response.addHeader("Set-Cookie", cookie.toString());
        response.addHeader("Access-Control-Expose-Header", "Set-Cookie");
    }

}
