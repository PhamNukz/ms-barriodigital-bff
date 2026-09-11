package cl.duoc.barriodigital.bff.web;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class BffExceptionHandler {

    /** Propaga el codigo real que devolvio el microservicio (404, 409, etc.). */
    @ExceptionHandler(RestClientResponseException.class)
    ProblemDetail desdeMicroservicio(RestClientResponseException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(ex.getStatusCode().value()));
        pd.setDetail(ex.getResponseBodyAsString());
        return pd;
    }
}
