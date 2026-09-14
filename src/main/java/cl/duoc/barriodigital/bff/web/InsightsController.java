package cl.duoc.barriodigital.bff.web;

import cl.duoc.barriodigital.bff.client.DomainClients;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/** Solo lectura: timeline de auditoria (Admin/Auditor) y KPIs (Admin). Reenvia filtros tal cual. */
@RestController
@RequestMapping("/api")
public class InsightsController {

    private final DomainClients clients;

    public InsightsController(DomainClients clients) {
        this.clients = clients;
    }

    @GetMapping("/audit/timeline")
    @PreAuthorize("hasAnyRole('Admin','Auditor')")
    public List<Map<String, Object>> timeline(@RequestParam Map<String, String> filtros, @AuthenticationPrincipal Jwt jwt) {
        return clients.timeline(query(filtros), jwt.getTokenValue());
    }

    @GetMapping("/report/kpis")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> kpis(@RequestParam Map<String, String> filtros, @AuthenticationPrincipal Jwt jwt) {
        return clients.kpis(query(filtros), jwt.getTokenValue());
    }

    @GetMapping("/report/top-procedures")
    @PreAuthorize("hasRole('Admin')")
    public List<Map<String, Object>> topProcedures(@RequestParam Map<String, String> filtros, @AuthenticationPrincipal Jwt jwt) {
        return clients.topProcedures(query(filtros), jwt.getTokenValue());
    }

    private static String query(Map<String, String> filtros) {
        UriComponentsBuilder uri = UriComponentsBuilder.newInstance();
        filtros.forEach(uri::queryParam);
        return uri.build().toUriString();
    }
}
