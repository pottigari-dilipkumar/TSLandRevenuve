package in.gov.landrevenue.clean.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.gov.landrevenue.clean.config.SecurityConfig;
import in.gov.landrevenue.clean.dto.land.LandRecordRequest;
import in.gov.landrevenue.clean.dto.land.LandRecordResponse;
import in.gov.landrevenue.clean.exception.GlobalExceptionHandler;
import in.gov.landrevenue.clean.exception.ResourceNotFoundException;
import in.gov.landrevenue.clean.security.JwtAuthenticationFilter;
import in.gov.landrevenue.clean.service.LandRecordService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LandRecordController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class LandRecordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LandRecordService landRecordService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void passThroughJwtFilter() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void list_shouldReturnUnauthorized_whenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/lands"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CITIZEN")
    void create_shouldReturnForbidden_forCitizenRole() throws Exception {
        LandRecordRequest request = new LandRecordRequest("SN-9", "Hyderabad", "Madhapur", new BigDecimal("1.20"), 1L);

        mockMvc.perform(post("/api/lands")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturnBadRequest_whenInputInvalid() throws Exception {
        String invalidJson = """
                {
                  "surveyNumber": "",
                  "district": "",
                  "village": "",
                  "areaInAcres": 0,
                  "ownerId": null
                }
                """;

        mockMvc.perform(post("/api/lands")
                        .contentType(APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.ownerId").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturnNotFound_whenOwnerMissing() throws Exception {
        LandRecordRequest request = new LandRecordRequest("SN-55", "Hyderabad", "Madhapur", new BigDecimal("2.50"), 555L);
        when(landRecordService.create(any(LandRecordRequest.class)))
                .thenThrow(new ResourceNotFoundException("Owner not found for ID: 555"));

        mockMvc.perform(post("/api/lands")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Owner not found for ID: 555"));
    }

    @Test
    @WithMockUser(roles = "CITIZEN")
    void list_shouldReturnData_forAuthorizedRole() throws Exception {
        when(landRecordService.list(isNull(), isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(
                new LandRecordResponse(3L, "SN-3", "Hyderabad", "Village-A", new BigDecimal("1.00"), 1L, "Owner-1")
        )));

        mockMvc.perform(get("/api/lands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].surveyNumber").value("SN-3"));
    }

    @Test
    @WithMockUser(roles = "CITIZEN")
    void getById_shouldReturnRecord_whenAuthorized() throws Exception {
        when(landRecordService.getById(33L)).thenReturn(
                new LandRecordResponse(33L, "SN-33", "Hyderabad", "Village-B", new BigDecimal("2.20"), 10L, "Owner-10")
        );

        mockMvc.perform(get("/api/lands/33"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surveyNumber").value("SN-33"))
                .andExpect(jsonPath("$.ownerId").value(10));
    }

    @Test
    @WithMockUser(roles = "DATA_ENTRY")
    void update_shouldReturnUpdatedRecord() throws Exception {
        LandRecordRequest request = new LandRecordRequest("SN-12", "Warangal", "Kazipet", new BigDecimal("4.40"), 2L);
        when(landRecordService.update(eq(12L), any(LandRecordRequest.class))).thenReturn(
                new LandRecordResponse(12L, "SN-12", "Warangal", "Kazipet", new BigDecimal("4.40"), 2L, "New Owner")
        );

        mockMvc.perform(put("/api/lands/12")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.district").value("Warangal"));
    }

    @Test
    @WithMockUser(roles = "CITIZEN")
    void update_shouldReturnForbidden_forCitizen() throws Exception {
        LandRecordRequest request = new LandRecordRequest("SN-12", "Warangal", "Kazipet", new BigDecimal("4.40"), 2L);

        mockMvc.perform(put("/api/lands/12")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturnNoContent() throws Exception {
        doNothing().when(landRecordService).delete(eq(99L));

        mockMvc.perform(delete("/api/lands/99"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturnNotFound_whenRecordMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Land record not found for ID: 500"))
                .when(landRecordService).delete(500L);

        mockMvc.perform(delete("/api/lands/500"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Land record not found for ID: 500"));
    }
}
