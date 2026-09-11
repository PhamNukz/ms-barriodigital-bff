package cl.duoc.barriodigital.bff.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

/**
 * Mismas reglas que los microservicios de dominio: el BFF revalida el JWT
 * (issuer, firma, audience en common security) y aplica los mismos gates de
 * rol, para fallar rapido antes de reenviar la llamada.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final ObjectMapper json = new ObjectMapper();

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/requests/**").hasAnyRole("Vecino", "Funcionario")
                .requestMatchers(HttpMethod.PUT, "/api/requests/**").hasAnyRole("Funcionario", "Admin")
                .requestMatchers(HttpMethod.POST, "/api/catalog/**").hasRole("Admin")
                .requestMatchers(HttpMethod.PUT, "/api/catalog/**").hasRole("Admin")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))
                .authenticationEntryPoint((req, res, ex) ->
                    write(res, HttpServletResponse.SC_UNAUTHORIZED, "token_invalido",
                          "El token esta ausente, expirado o no fue emitido por el IDaaS configurado"))
                .accessDeniedHandler((req, res, ex) ->
                    write(res, HttpServletResponse.SC_FORBIDDEN, "acceso_denegado",
                          "El token es valido pero no tiene el rol requerido para esta operacion")));
        return http.build();
    }

    private void write(HttpServletResponse res, int status, String code, String detail) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        json.writeValue(res.getWriter(), Map.of("error", code, "detalle", detail));
    }
}
