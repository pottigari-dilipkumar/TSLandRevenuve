package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.revenue.RevenueRecordRequest;
import in.gov.landrevenue.clean.dto.revenue.RevenueRecordResponse;
import in.gov.landrevenue.clean.entity.LandRecord;
import in.gov.landrevenue.clean.entity.RevenueRecord;
import in.gov.landrevenue.clean.exception.ResourceNotFoundException;
import in.gov.landrevenue.clean.mapper.LandRevenueMapper;
import in.gov.landrevenue.clean.repository.RevenueRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevenueRecordServiceTest {

    @Mock
    private RevenueRecordRepository revenueRecordRepository;
    @Mock
    private LandRecordService landRecordService;
    @Mock
    private LandRevenueMapper mapper;

    @InjectMocks
    private RevenueRecordService revenueRecordService;

    @Test
    void create_shouldCreateRevenueRecordWithLinkedLand() {
        RevenueRecordRequest request = new RevenueRecordRequest(
                new BigDecimal("1200.00"),
                LocalDate.of(2026, 1, 10),
                "PAY-2026-001",
                7L
        );

        LandRecord landRecord = new LandRecord();
        setId(landRecord, 7L);

        RevenueRecord saved = new RevenueRecord();
        saved.setAmount(request.amount());
        saved.setPaymentDate(request.paymentDate());
        saved.setPaymentReference(request.paymentReference());
        saved.setLandRecord(landRecord);
        setId(saved, 55L);

        RevenueRecordResponse response = new RevenueRecordResponse(55L, request.amount(), request.paymentDate(), request.paymentReference(), 7L);

        when(landRecordService.findEntity(7L)).thenReturn(landRecord);
        when(revenueRecordRepository.save(any(RevenueRecord.class))).thenReturn(saved);
        when(mapper.toRevenueResponse(saved)).thenReturn(response);

        RevenueRecordResponse result = revenueRecordService.create(request);

        assertThat(result.id()).isEqualTo(55L);
        assertThat(result.landRecordId()).isEqualTo(7L);
    }

    @Test
    void create_shouldThrow_whenLandRecordMissing() {
        RevenueRecordRequest request = new RevenueRecordRequest(
                new BigDecimal("250.00"),
                LocalDate.of(2026, 2, 10),
                "PAY-MISS",
                999L
        );
        when(landRecordService.findEntity(999L)).thenThrow(new ResourceNotFoundException("Land record not found for ID: 999"));

        assertThatThrownBy(() -> revenueRecordService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Land record not found for ID: 999");
    }

    @Test
    void list_shouldReturnMappedRevenueRecords() {
        RevenueRecord record = new RevenueRecord();
        record.setAmount(new BigDecimal("200.00"));
        record.setPaymentDate(LocalDate.of(2026, 2, 1));
        record.setPaymentReference("R-1");

        when(revenueRecordRepository.findAll(PageRequest.of(0, 20))).thenReturn(new PageImpl<>(List.of(record)));
        when(mapper.toRevenueResponse(record)).thenReturn(
                new RevenueRecordResponse(1L, new BigDecimal("200.00"), LocalDate.of(2026, 2, 1), "R-1", 9L)
        );

        Page<RevenueRecordResponse> result = revenueRecordService.list(PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).paymentReference()).isEqualTo("R-1");
    }

    private void setId(Object target, Long id) {
        try {
            var field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
