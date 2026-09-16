package com.taskinator.taskinator.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taskinator.taskinator.web.AbstractIntegrationTest;
import com.taskinator.taskinator.web.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "application.rate-limit.enabled=true",
    "application.rate-limit.max-requests=2",
    "application.rate-limit.window-seconds=60"
})
class AuthRateLimitingIT extends AbstractIntegrationTest {

    @Test
    void rateLimit_shouldReturn429_afterTooManyRequests() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "rate.limit@example.com", "SecurePass123!", "Rate", "Limit");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isTooManyRequests());
    }
}
