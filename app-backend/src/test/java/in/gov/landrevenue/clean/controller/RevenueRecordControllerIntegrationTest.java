package in.gov.landrevenue.clean.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.gov.landrevenue.clean.config.SecurityConfig;
import in.gov.landrevenue.clean.dto.revenue.RevenueRecordRequest;
import in.gov.landrevenue.clean.dto.revenue.RevenueRecordResponse;
import in.gov.landrevenue.clean.exception.GlobalExceptionHandler;
import in.gov.landrevenue.clean.exception.ResourceNotFoundException;
import in.gov.landrevenue.clean.security.JwtAuthenticationFilter;
import in.gov.landrevenue.clean.service.RevenueRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RevenueRecordController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class RevenueRecordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RevenueRecordService revenueRecordService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void list_shouldReturnUnauthorized_whenNoAuth() throws Exception {
        mockMvc.perform(get("/api/revenues"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CITIZEN")
    void create_shouldReturnForbidden_forCitizen() throws Exception {
        RevenueRecordRequest request = new RevenueRecordRequest(new BigDecimal("300"), LocalDate.of(2026, 1, 2), "P-1", 1L);

        mockMvc.perform(post("/api/revenues")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "REVENUE_OFFICER")
    void create_shouldValidateInput() throws Exception {
        String invalidJson = """
                {
                  "amount": 0,
                  "paymentDate": null,
                  "paymentReference": "",
                  "landRecordId": null
                }
                """;

        mockMvc.perform(post("/api/revenues")
                        .contentType(APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.paymentReference").exists());
    }

    @Test
    @WithMockUser(roles = "REVENUE_OFFICER")
    void create_shouldReturnNotFound_whenLandRecordMissing() throws Exception {
        RevenueRecordRequest request = new RevenueRecordRequest(new BigDecimal("330.75"), LocalDate.of(2026, 3, 10), "PAY-X", 8L);
        when(revenueRecordService.create(any(RevenueRecordRequest.class)))
                .thenThrow(new ResourceNotFoundException("Land record not found for ID: 8"));

        mockMvc.perform(post("/api/revenues")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Land record not found for ID: 8"));
    }

    @Test
    @WithMockUser(roles = "REVENUE_OFFICER")
    void create_shouldReturnCreatedRecord() throws Exception {
        RevenueRecordRequest request = new RevenueRecordRequest(new BigDecimal("330.75"), LocalDate.of(2026, 3, 10), "PAY-X", 8L);
        RevenueRecordResponse response = new RevenueRecordResponse(20L, new BigDecimal("330.75"), LocalDate.of(2026, 3, 10), "PAY-X", 8L);

        when(revenueRecordService.create(any(RevenueRecordRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/revenues")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20L));
    }

    @Test
    @WithMockUser(roles = "CITIZEN")
    void list_shouldReturnRecords_forCitizen() throws Exception {
        when(revenueRecordService.list(any())).thenReturn(new PageImpl<>(List.of(
                new RevenueRecordResponse(1L, new BigDecimal("90.00"), LocalDate.of(2026, 1, 1), "R-REF", 7L)
        )));

        mockMvc.perform(get("/api/revenues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].paymentReference").value("R-REF"));
    }
}
