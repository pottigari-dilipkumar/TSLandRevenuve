package in.gov.landrevenue.clean.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.gov.landrevenue.clean.config.SecurityConfig;
import in.gov.landrevenue.clean.dto.auth.AuthRequest;
import in.gov.landrevenue.clean.dto.auth.AuthResponse;
import in.gov.landrevenue.clean.exception.GlobalExceptionHandler;
import in.gov.landrevenue.clean.exception.ResourceNotFoundException;
import in.gov.landrevenue.clean.security.JwtAuthenticationFilter;
import in.gov.landrevenue.clean.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void login_shouldSucceed_withoutAuthenticationHeader() throws Exception {
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(new AuthResponse("token-123", "ADMIN"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("admin", "password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_shouldReturnBadRequest_whenFieldsMissing() throws Exception {
        String invalidJson = """
                {
                  "username": "",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void login_shouldReturnNotFound_whenCredentialsInvalid() throws Exception {
        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new ResourceNotFoundException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("ghost", "wrong"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}
