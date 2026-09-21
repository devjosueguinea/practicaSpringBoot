package SystemITR.JosueGuinea.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.bytecode.internal.bytebuddy.PassThroughInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtCookieAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtCookieAuthFilter.class);
    //Nombre de la cookie que almacena el token JWT
    private static final String AUTH_COOKIE_NAME="authToken";
    //Servicio de utilidad para manipulación, parseo y extracción de claims del JWT
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Omitir validación si la ruta es un endpoint público
        if (isPublicEndPoint(request)){
            filterChain.doFilter(request, response);
            return;
        }

        try{
            // 2. Extraer el token de las cookies de la petición (request)
            String token = extractTokenFromCookies(request);
            // 3. Validar la presencia del token en la cookie
            if (token == null || token.isBlank()){
                //Si la ruta requiere autenticación y no hay token, detiene la cadena enviando el error 401
                if (!isPublicEndPoint(request)){
                    sendError(response, "Token no encontrado", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Decodificar el token y extaer sus atributos (claims) y el rol
            Claims claims = jwtUtils.parseTokenAndClaims(token);
            String rol = jwtUtils.extractRol(token);

            // 5. Crear la autoridad (rol) asignandole el prefijo estándar 'ROLE_'
            Collection<? extends GrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol));

            // 6. Genear el objeto de autenticación para SpringSecurity
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            claims.getSubject(),    //Usuario contenido en el token
                            null,
                            authorities             // Lista de roles asignados
                    );

            // 7. Establecer la autenticación activa en el SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 8. Permitir el paso de la request hacia el controlador correspondiente
            filterChain.doFilter(request, response);
        }catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            sendError(response, "Token expirado", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (MalformedJwtException e) {
            log.warn("Token malformado: {}", e.getMessage());
            sendError(response, "Token inválido", HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            log.error("Error de autenticación", e);
            sendError(response, "Error de autenticación", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    private void sendError(HttpServletResponse response, String mensaje, int status) throws IOException{
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write(String.format(
                "{\"error\": \"%s\", \"status\": %d}", mensaje, status
        ));
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(c -> AUTH_COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    private boolean isPublicEndPoint(HttpServletRequest request) {
        //Extracción del endpoint
        String path = request.getRequestURI();
        //Extracción el método HTTP utilizado en la request
        String method = request.getMethod();

        return (path.equals("/api/auth/login") && method.equals("POST")) ||
                (path.equals("/api/auth/register") && method.equals("POST")) ||
                (path.equals("/api/forgotPassword") && method.equals("POST"));
    }
}
