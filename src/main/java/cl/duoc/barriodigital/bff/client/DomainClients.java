package cl.duoc.barriodigital.bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** Clientes hacia los microservicios de dominio; reenvian el bearer del usuario. */
@Component
public class DomainClients {

    private final RestClient requests;
    private final RestClient catalog;

    public DomainClients(@Value("${barriodigital.requests.base-url}") String requestsUrl,
                         @Value("${barriodigital.catalog.base-url}") String catalogUrl) {
        this.requests = RestClient.builder().baseUrl(requestsUrl).build();
        this.catalog = RestClient.builder().baseUrl(catalogUrl).build();
    }

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> ONE =
            new ParameterizedTypeReference<>() {};

    // ---- requests ----
    public List<Map<String, Object>> listarRequests(String query, String bearer) {
        return requests.get().uri("/requests" + query)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer).retrieve().body(LIST);
    }

    public Map<String, Object> obtenerRequest(Long id, String bearer) {
        return requests.get().uri("/requests/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer).retrieve().body(ONE);
    }

    public Map<String, Object> crearRequest(Object body, String bearer) {
        return requests.post().uri("/requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer).body(body).retrieve().body(ONE);
    }

    public Map<String, Object> cambiarEstadoRequest(Long id, Object body, String bearer) {
        return requests.put().uri("/requests/{id}/status", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer).body(body).retrieve().body(ONE);
    }

    // ---- catalog ----
    public List<Map<String, Object>> listarCatalogo(String bearer) {
        return catalog.get().uri("/catalog/procedures")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer).retrieve().body(LIST);
    }

    public Map<String, Object> crearTipoTramite(Object body, String bearer) {
        return catalog.post().uri("/catalog/procedures")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer).body(body).retrieve().body(ONE);
    }

    public Map<String, Object> actualizarTipoTramite(Long id, Object body, String bearer) {
        return catalog.put().uri("/catalog/procedures/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer).body(body).retrieve().body(ONE);
    }
}
