package cl.duoc.barriodigital.bff.web;

import cl.duoc.barriodigital.bff.client.DomainClients;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final DomainClients clients;

    public CatalogController(DomainClients clients) {
        this.clients = clients;
    }

    @GetMapping("/procedures")
    public List<Map<String, Object>> listar(@AuthenticationPrincipal Jwt jwt) {
        return clients.listarCatalogo(jwt.getTokenValue());
    }

    @PostMapping("/procedures")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> crear(@Valid @RequestBody CrearTipo body, @AuthenticationPrincipal Jwt jwt) {
        return clients.crearTipoTramite(body, jwt.getTokenValue());
    }

    @PutMapping("/procedures/{id}")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarTipo body,
                                          @AuthenticationPrincipal Jwt jwt) {
        return clients.actualizarTipoTramite(id, body, jwt.getTokenValue());
    }

    public record CrearTipo(@NotBlank String nombre, String requisitos, @NotNull @Min(0) Integer cupoDiario) {
    }

    public record ActualizarTipo(String requisitos, @NotNull @Min(0) Integer cupoDiario, boolean activo) {
    }
}
