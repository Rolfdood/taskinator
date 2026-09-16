package com.taskinator.taskinator.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taskinator.taskinator.web.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "application.cors.allowed-origins=https://taskinator-client.vercel.app/"
})
class ProductionWebSecurityIT extends AbstractIntegrationTest {

    private static final String ALLOWED_ORIGIN = "https://taskinator-client.vercel.app/";
    private static final String DISALLOWED_ORIGIN = "https://evil.example.com";

    @Test
    void actuatorHealth_shouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }

    @Test
    void actuatorInfo_shouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/info"))
            .andExpect(status().isOk());
    }

    @Test
    void actuatorOtherEndpoints_shouldBeDenied() throws Exception {
        mockMvc.perform(get("/actuator/beans"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/json")));
    }

    @Test
    void authRegister_shouldBePublic() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content("{\"email\":\"test@example.com\",\"password\":\"TestPass123!\",\"firstName\":\"Test\",\"lastName\":\"User\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    void corsPreflight_shouldBeAllowed_forAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/projects")
                .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("Authorization")));
    }

    @Test
    void corsPreflight_shouldRejectDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/projects")
                .header(HttpHeaders.ORIGIN, DISALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
            .andExpect(status().isForbidden());
    }
}
