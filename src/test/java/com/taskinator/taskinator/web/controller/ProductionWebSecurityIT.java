package com.taskinator.taskinator.web.controller;

import com.taskinator.taskinator.web.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.cors.allowed-origins=https://app.taskinator.example")
class ProductionWebSecurityIT extends AbstractIntegrationTest {

    @Test
    void health_shouldBePublicForRailwayHealthchecks() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void cors_shouldAllowOnlyConfiguredOrigins() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                .header(HttpHeaders.ORIGIN, "https://app.taskinator.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                "https://app.taskinator.example"));

        mockMvc.perform(options("/api/v1/auth/login")
                .header(HttpHeaders.ORIGIN, "https://evil.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isForbidden());
    }
}
