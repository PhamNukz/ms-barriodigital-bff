package cl.duoc.barriodigital.bff;

import org.junit.jupiter.api.Test;
import cl.duoc.barriodigital.bff.client.DomainClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BffSecurityTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    JwtDecoder jwtDecoder;

    @MockBean
    DomainClients clients;

    @Test
    void sin_token_devuelve_401_con_cuerpo_json() throws Exception {
        mvc.perform(get("/api/requests"))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.error").value("token_invalido"));
    }

    @Test
    void vecino_no_puede_crear_tipos_de_tramite_403() throws Exception {
        mvc.perform(post("/api/catalog/procedures")
                .contentType("application/json")
                .content("{\"nombre\":\"x\",\"cupoDiario\":5}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Vecino"))))
           .andExpect(status().isForbidden())
           .andExpect(jsonPath("$.error").value("acceso_denegado"));
    }

    @Test
    void vecino_no_ve_kpis_403() throws Exception {
        mvc.perform(get("/api/report/kpis")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Vecino"))))
           .andExpect(status().isForbidden())
           .andExpect(jsonPath("$.error").value("acceso_denegado"));
    }

    @Test
    void auditor_no_ve_kpis_pero_si_timeline() throws Exception {
        var auditor = jwt().authorities(new SimpleGrantedAuthority("ROLE_Auditor"));
        mvc.perform(get("/api/report/kpis").with(auditor)).andExpect(status().isForbidden());
        mvc.perform(get("/api/audit/timeline").with(auditor)).andExpect(status().isOk());
    }

    @Test
    void funcionario_ve_el_timeline_de_auditoria() throws Exception {
        mvc.perform(get("/api/audit/timeline").param("persona", "vecino1@duocuc.cl")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Funcionario"))))
           .andExpect(status().isOk());
    }

    @Test
    void solo_admin_ve_las_colas_de_rabbitmq() throws Exception {
        mvc.perform(get("/api/admin/queues").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Funcionario"))))
           .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/queues").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Admin"))))
           .andExpect(status().isOk());
    }

    @Test
    void vecino_no_puede_programar_pasos_del_seguimiento_403() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/requests/1/seguimiento/ADMITIDO")
                .contentType("application/json").content("{\"nota\":\"x\"}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Vecino"))))
           .andExpect(status().isForbidden());
    }

    @Test
    void vecino_puede_ver_el_seguimiento() throws Exception {
        mvc.perform(get("/api/requests/1/seguimiento").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Vecino"))))
           .andExpect(status().isOk());
    }

    /** El BFF tiene su propio DTO: un campo que no declare se pierde sin error antes de llegar a requests. */
    @Test
    void crear_tramite_reenvia_el_punto_del_mapa_y_la_referencia() throws Exception {
        mvc.perform(post("/api/requests")
                .contentType("application/json")
                .content("{\"tipoId\":1,\"descripcion\":\"Bache\",\"lat\":-33.0418,\"lng\":-71.6269,\"referencia\":\"Casa azul\"}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Vecino"))))
           .andExpect(status().isCreated());

        var enviado = org.mockito.ArgumentCaptor.forClass(Object.class);
        org.mockito.Mockito.verify(clients).crearRequest(enviado.capture(), org.mockito.ArgumentMatchers.any());
        var body = (cl.duoc.barriodigital.bff.web.RequestsController.CrearTramite) enviado.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(new java.math.BigDecimal("-33.0418"), body.lat());
        org.junit.jupiter.api.Assertions.assertEquals(new java.math.BigDecimal("-71.6269"), body.lng());
        org.junit.jupiter.api.Assertions.assertEquals("Casa azul", body.referencia());
    }

    @Test
    void rutas_de_visita_y_responsable_llegan_a_requests() throws Exception {
        var vecino = jwt().authorities(new SimpleGrantedAuthority("ROLE_Vecino"));
        var funcionario = jwt().authorities(new SimpleGrantedAuthority("ROLE_Funcionario"));

        mvc.perform(post("/api/requests/5/visita/respuesta").contentType("application/json")
                .content("{\"respuesta\":\"CONFIRMAR\"}").with(vecino)).andExpect(status().isOk());
        mvc.perform(put("/api/requests/5/visita").contentType("application/json")
                .content("{\"fecha\":\"2030-01-15\",\"franja\":\"MANANA\",\"cuadrilla\":\"Norte\"}")
                .with(funcionario)).andExpect(status().isOk());
        mvc.perform(put("/api/requests/5/responsable").contentType("application/json")
                .content("{\"responsable\":\"a@duocuc.cl\"}").with(funcionario)).andExpect(status().isOk());
        mvc.perform(put("/api/requests/5/en-camino").with(funcionario)).andExpect(status().isOk());

        verify(clients).accionRequest(eq(HttpMethod.POST), eq(5L), eq("visita/respuesta"), any(), any());
        verify(clients).accionRequest(eq(HttpMethod.PUT), eq(5L), eq("en-camino"), isNull(), any());
    }

    @Test
    void vecino_no_propone_visita_403() throws Exception {
        mvc.perform(put("/api/requests/5/visita").contentType("application/json").content("{}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Vecino"))))
           .andExpect(status().isForbidden());
    }
}
