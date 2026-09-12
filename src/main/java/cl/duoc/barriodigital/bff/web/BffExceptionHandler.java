package cl.duoc.barriodigital.bff.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class BffExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(BffExceptionHandler.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    /** Propaga el codigo real que devolvio el microservicio (404, 409, etc.). */
    @ExceptionHandler(RestClientResponseException.class)
    ProblemDetail desdeMicroservicio(RestClientResponseException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(ex.getStatusCode().value()));
        pd.setDetail(detalleDe(ex.getResponseBodyAsString()));
        return pd;
    }

    /**
     * El microservicio no respondio (caido, reiniciandose o timeout). Antes esto
     * caia sin manejar y salia como 500 sin mensaje, indistinguible de un error
     * de datos; en la practica pasaba al desplegar, mientras el contenedor de
     * destino todavia arrancaba. 503 comunica que es temporal y reintentable.
     */
    @ExceptionHandler(ResourceAccessException.class)
    ProblemDetail microservicioNoDisponible(ResourceAccessException ex) {
        log.error("Microservicio de dominio no disponible", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio no está disponible en este momento. Espera unos segundos y vuelve a intentarlo.");
    }

    /**
     * El cuerpo del microservicio ya viene como ProblemDetail (RFC 7807); se extrae
     * su "detail" para no mostrarle el JSON crudo al usuario.
     */
    private static String detalleDe(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) return "Error en el servicio de dominio.";
        try {
            JsonNode detail = JSON.readTree(cuerpo).get("detail");
            if (detail != null && !detail.asText().isBlank()) return detail.asText();
        } catch (Exception e) {
            // cuerpo no-JSON: se devuelve tal cual mas abajo
        }
        return cuerpo;
    }
}
