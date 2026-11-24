package cl.duoc.barriodigital.bff.web;

import cl.duoc.barriodigital.bff.client.DomainClients;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
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

    /** Endpoint esencial del caso (seccion 5). */
    @GetMapping("/{id}")
    public Map<String, Object> obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return clients.obtenerRequest(id, jwt.getTokenValue());
    }

    @GetMapping("/{id}/seguimiento")
    public List<Map<String, Object>> seguimiento(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return clients.seguimiento(id, jwt.getTokenValue());
    }

    @PutMapping("/{id}/seguimiento/{estado}")
    @PreAuthorize("hasAnyRole('Funcionario','Admin')")
    public List<Map<String, Object>> planificarPaso(@PathVariable Long id, @PathVariable String estado,
                                                   @Valid @RequestBody PlanificarPaso body,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return clients.planificarPaso(id, estado, body, jwt.getTokenValue());
    }

    /** Antes que /{id}: "reglas" no es un id. Las lee cualquier autenticado. */
    @GetMapping("/reglas")
    public Map<String, Object> reglas(@AuthenticationPrincipal Jwt jwt) {
        return clients.reglas(jwt.getTokenValue());
    }

    @PutMapping("/reglas")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> cambiarReglas(@RequestBody Map<String, Object> body, @AuthenticationPrincipal Jwt jwt) {
        return clients.cambiarReglas(body, jwt.getTokenValue());
    }

    // ---- Admin B: visibilidad, papelera y restaurar ----
    @PutMapping("/{id}/visibilidad")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> cambiarVisibilidad(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                  @AuthenticationPrincipal Jwt jwt) {
        return clients.accionRequest(HttpMethod.PUT, id, "visibilidad", body, jwt.getTokenValue());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> enviarAPapelera(@PathVariable Long id, @RequestParam(required = false) String motivo,
                                               @AuthenticationPrincipal Jwt jwt) {
        return clients.eliminarRequest(id, motivo, jwt.getTokenValue());
    }

    /** Admin siempre; Auditor solo con la regla auditorRestaura (lo decide requests). */
    @PostMapping("/{id}/restaurar")
    @PreAuthorize("hasAnyRole('Admin','Auditor')")
    public Map<String, Object> restaurar(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                         @AuthenticationPrincipal Jwt jwt) {
        return clients.accionRequest(HttpMethod.POST, id, "restaurar", body, jwt.getTokenValue());
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

    // Visita y responsable: el body se reenvia tal cual y lo valida requests (sus 400/403/404/409
    // vuelven al front por BffExceptionHandler).
    @PutMapping("/{id}/responsable")
    @PreAuthorize("hasAnyRole('Funcionario','Admin')")
    public Map<String, Object> asignarResponsable(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                  @AuthenticationPrincipal Jwt jwt) {
        return clients.accionRequest(HttpMethod.PUT, id, "responsable", body, jwt.getTokenValue());
    }

    @PutMapping("/{id}/visita")
    @PreAuthorize("hasAnyRole('Funcionario','Admin')")
    public Map<String, Object> proponerVisita(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                              @AuthenticationPrincipal Jwt jwt) {
        return clients.accionRequest(HttpMethod.PUT, id, "visita", body, jwt.getTokenValue());
    }

    /** Solo el vecino dueno; requests responde 403 a cualquier otro. */
    @PostMapping("/{id}/visita/respuesta")
    public Map<String, Object> responderVisita(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                               @AuthenticationPrincipal Jwt jwt) {
        return clients.accionRequest(HttpMethod.POST, id, "visita/respuesta", body, jwt.getTokenValue());
    }

    @PutMapping("/{id}/en-camino")
    @PreAuthorize("hasAnyRole('Funcionario','Admin')")
    public Map<String, Object> avisarEnCamino(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return clients.accionRequest(HttpMethod.PUT, id, "en-camino", null, jwt.getTokenValue());
    }

    /** El funcionario confirmo la visita por telefono; body opcional {nota}. */
    @PutMapping("/{id}/visita/confirmar")
    @PreAuthorize("hasAnyRole('Funcionario','Admin')")
    public Map<String, Object> confirmarVisita(@PathVariable Long id,
                                               @RequestBody(required = false) Map<String, Object> body,
                                               @AuthenticationPrincipal Jwt jwt) {
        return clients.accionRequest(HttpMethod.PUT, id, "visita/confirmar", body, jwt.getTokenValue());
    }

    /** Contactos (familiares en copia): solo el vecino dueno; requests responde 403 a cualquier otro. */
    @PostMapping("/{id}/contactos")
    public Map<String, Object> agregarContacto(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                               @AuthenticationPrincipal Jwt jwt) {
        return clients.accionRequest(HttpMethod.POST, id, "contactos", body, jwt.getTokenValue());
    }

    @DeleteMapping("/{id}/contactos/{contactoId}")
    public Map<String, Object> quitarContacto(@PathVariable Long id, @PathVariable Long contactoId,
                                              @AuthenticationPrincipal Jwt jwt) {
        return clients.accionRequest(HttpMethod.DELETE, id, "contactos/" + contactoId, null, jwt.getTokenValue());
    }

    /**
     * vecinoUsername: solo Funcionario, para ingresar el tramite a nombre de un vecino.
     * lat/lng/referencia: el punto marcado en el mapa; requests valida que caiga en Valparaiso.
     */
    public record CrearTramite(@NotNull Long tipoId, @NotBlank String descripcion, String direccion,
                               @Email @Size(max = 150) String vecinoUsername,
                               BigDecimal lat, BigDecimal lng, @Size(max = 300) String referencia) {
    }

    public record CambiarEstado(@NotBlank String status, @Size(max = 500) String nota) {
    }

    /** fechaEstimada en ISO-8601 (Instant); se reenvia tal cual al microservicio. */
    public record PlanificarPaso(String fechaEstimada, @Size(max = 500) String nota) {
    }
}
