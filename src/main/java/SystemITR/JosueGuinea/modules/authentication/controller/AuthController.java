package SystemITR.JosueGuinea.modules.authentication.controller;

import SystemITR.JosueGuinea.config.jwt.JwtUtils;
import SystemITR.JosueGuinea.modules.authentication.model.dto.AuthRequestDTO;
import SystemITR.JosueGuinea.modules.authentication.model.dto.AuthResponseDTO;
import SystemITR.JosueGuinea.modules.authentication.service.AuthService;
import SystemITR.JosueGuinea.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService service;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login (@Valid @RequestBody AuthRequestDTO request, HttpServletResponse response){

        //Verificando si las credenciales poseen errores
        if (request.getUsername().trim().isBlank() || request.getUsername().trim().isEmpty() ||
                request.getPassword().trim().isBlank() || request.getPassword().trim().isEmpty()){
            ApiResponse<AuthResponseDTO> credencialesIncompletas = new ApiResponse<>(false, "E001 - Credenciales incompletas");
            return ResponseEntity.status(400).body(credencialesIncompletas);
        }

        AuthResponseDTO responseDTO = service.login(request);
        if (responseDTO != null){
            addTokenCookie(responseDTO, response);
            ApiResponse<AuthResponseDTO> AccesoValido = new ApiResponse<>(true, "Inicio de sesión exitoso", responseDTO);
            return ResponseEntity.ok(AccesoValido);
        }

        ApiResponse<AuthResponseDTO> AccesoInvalido = new ApiResponse<>(false, "E002 - Acceso denegado");
        return ResponseEntity.status(401).body(AccesoInvalido);
    }

    private void addTokenCookie(AuthResponseDTO responseDTO, HttpServletResponse response) {
        //Crear token
        String token = jwtUtils.create(
                String.valueOf(responseDTO.getId()),
                responseDTO.getUsername(),
                responseDTO.getRol(),
                responseDTO.getMensaje()
        );

        ResponseCookie cookie = ResponseCookie.from("authToken", token)
                .path("/")
                .httpOnly(true)
                .secure(true)
                //El atributo sameSite en las cookies sirve paa controlar si las cookies se envían o no cuando se realizan solicitudes
                //entre diferentes sitios web. Su objetivo principal es proteger la privacidad y seguridad del usuario, mitigando de
                //forma efectiva los ataques de tipo CSRF.
                //Posibles valores de SameSite (Strict, Lax, None)
                .sameSite("None")
                .maxAge(86400)
                .domain("systemrh-5d827726e203.herokuapp.com")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        response.addHeader("Access-Control-Expose-Header", "Set-Cookie");
    }
}
