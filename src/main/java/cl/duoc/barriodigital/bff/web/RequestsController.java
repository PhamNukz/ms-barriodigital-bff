package cl.duoc.barriodigital.bff.web;

import cl.duoc.barriodigital.bff.client.DomainClients;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class RequestsController {

    private final DomainClients clients;

    public RequestsController(DomainClients clients) {
        this.clients = clients;
    }

    @GetMapping
    public List<Map<String, Object>> listar(@RequestParam Map<String, String> filtros, @AuthenticationPrincipal Jwt jwt) {
        UriComponentsBuilder uri = UriComponentsBuilder.newInstance();
        filtros.forEach(uri::queryParam);
        return clients.listarRequests(uri.build().toUriString(), jwt.getTokenValue());
    }

    @GetMapping("/{id}")
    public Map<String, Object> obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return clients.obtenerRequest(id, jwt.getTokenValue());
    }

    @GetMapping("/tipos/{tipoId}/cupo")
    public Map<String, Object> cupoDeHoy(@PathVariable Long tipoId, @AuthenticationPrincipal Jwt jwt) {
        return clients.cupoDeHoy(tipoId, jwt.getTokenValue());
    }

    @GetMapping("/cupos")
    public List<Map<String, Object>> cuposDeHoy(@AuthenticationPrincipal Jwt jwt) {
        return clients.cuposDeHoy(jwt.getTokenValue());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('Vecino','Funcionario')")
    public Map<String, Object> crear(@Valid @RequestBody CrearTramite body, @AuthenticationPrincipal Jwt jwt) {
        return clients.crearRequest(body, jwt.getTokenValue());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('Funcionario','Admin')")
    public Map<String, Object> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstado body,
                                             @AuthenticationPrincipal Jwt jwt) {
        return clients.cambiarEstadoRequest(id, body, jwt.getTokenValue());
    }

    public record CrearTramite(@NotNull Long tipoId, @NotBlank String descripcion, String direccion) {
    }

    public record CambiarEstado(@NotBlank String status) {
    }
}
