package cl.duoc.barriodigital.bff;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
