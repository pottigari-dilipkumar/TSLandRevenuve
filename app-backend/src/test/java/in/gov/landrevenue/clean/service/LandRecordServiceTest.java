package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.land.LandRecordRequest;
import in.gov.landrevenue.clean.dto.land.LandRecordResponse;
import in.gov.landrevenue.clean.entity.LandRecord;
import in.gov.landrevenue.clean.entity.Owner;
import in.gov.landrevenue.clean.exception.ResourceNotFoundException;
import in.gov.landrevenue.clean.mapper.LandRevenueMapper;
import in.gov.landrevenue.clean.repository.LandRecordRepository;
import in.gov.landrevenue.clean.repository.OwnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LandRecordServiceTest {

    @Mock
    private LandRecordRepository landRecordRepository;
    @Mock
    private OwnerRepository ownerRepository;
    @Mock
    private LandRevenueMapper mapper;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private LandRecordService landRecordService;

    @Test
    void create_shouldPersistRecord_whenOwnerExists() {
        Owner owner = owner(10L, "Ravi Kumar");
        LandRecordRequest request = new LandRecordRequest("SN-1", "Hyderabad", "Madhapur", new BigDecimal("3.50"), 10L);

        LandRecord saved = landRecord(100L, "SN-1", owner);
        LandRecordResponse response = new LandRecordResponse(100L, "SN-1", "Hyderabad", "Madhapur", new BigDecimal("3.50"), 10L, "Ravi Kumar");

        when(ownerRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(landRecordRepository.save(any(LandRecord.class))).thenReturn(saved);
        when(mapper.toLandResponse(saved)).thenReturn(response);

        LandRecordResponse result = landRecordService.create(request);

        assertThat(result.id()).isEqualTo(100L);
        verify(landRecordRepository).save(any(LandRecord.class));
    }

    @Test
    void create_shouldThrowNotFound_whenOwnerMissing() {
        LandRecordRequest request = new LandRecordRequest("SN-404", "Pune", "Khed", new BigDecimal("1.00"), 999L);
        when(ownerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> landRecordService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Owner not found");

        verify(landRecordRepository, never()).save(any());
    }

    @Test
    void list_shouldMapPageResponses() {
        Owner owner = owner(1L, "Meera");
        LandRecord record = landRecord(5L, "SN-5", owner);
        PageRequest pageable = PageRequest.of(0, 10);
        when(landRecordRepository.findAll(org.mockito.ArgumentMatchers.<Specification<LandRecord>>any(), eq(pageable))).thenReturn(new PageImpl<>(List.of(record)));
        when(mapper.toLandResponse(record)).thenReturn(
                new LandRecordResponse(5L, "SN-5", "Hyderabad", "Madhapur", new BigDecimal("2.00"), 1L, "Meera")
        );

        Page<LandRecordResponse> result = landRecordService.list(null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).surveyNumber()).isEqualTo("SN-5");
    }

    @Test
    void getById_shouldReturnMappedRecord_whenFound() {
        Owner owner = owner(7L, "Kiran");
        LandRecord existing = landRecord(77L, "SN-77", owner);
        LandRecordResponse mapped = new LandRecordResponse(77L, "SN-77", "Hyderabad", "Madhapur", new BigDecimal("2.00"), 7L, "Kiran");
        when(landRecordRepository.findById(77L)).thenReturn(Optional.of(existing));
        when(mapper.toLandResponse(existing)).thenReturn(mapped);

        LandRecordResponse result = landRecordService.getById(77L);

        assertThat(result.id()).isEqualTo(77L);
        assertThat(result.ownerId()).isEqualTo(7L);
    }

    @Test
    void update_shouldSaveUpdatedRecord_whenLandAndOwnerExist() {
        Owner oldOwner = owner(1L, "Old Owner");
        Owner newOwner = owner(2L, "New Owner");
        LandRecord existing = landRecord(12L, "SN-12", oldOwner);
        LandRecordRequest request = new LandRecordRequest("SN-12", "Warangal", "Kazipet", new BigDecimal("8.10"), 2L);

        when(landRecordRepository.findById(12L)).thenReturn(Optional.of(existing));
        when(ownerRepository.findById(2L)).thenReturn(Optional.of(newOwner));
        when(landRecordRepository.save(existing)).thenReturn(existing);
        when(mapper.toLandResponse(existing)).thenReturn(new LandRecordResponse(12L, "SN-12", "Warangal", "Kazipet", new BigDecimal("8.10"), 2L, "New Owner"));

        LandRecordResponse result = landRecordService.update(12L, request);

        assertThat(result.ownerId()).isEqualTo(2L);
        assertThat(existing.getDistrict()).isEqualTo("Warangal");
        assertThat(existing.getAreaInAcres()).isEqualTo(new BigDecimal("8.10"));
    }

    @Test
    void update_shouldThrowNotFound_whenLandRecordMissing() {
        LandRecordRequest request = new LandRecordRequest("SN-X", "N", "V", new BigDecimal("1.00"), 1L);
        when(landRecordRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> landRecordService.update(404L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Land record not found");
    }

    @Test
    void findEntity_shouldThrowNotFound_whenRecordMissing() {
        when(landRecordRepository.findById(500L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> landRecordService.findEntity(500L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Land record not found for ID: 500");
    }

    @Test
    void delete_shouldRemoveExistingRecord() {
        Owner owner = owner(2L, "Arjun");
        LandRecord record = landRecord(11L, "SN-11", owner);
        when(landRecordRepository.findById(11L)).thenReturn(Optional.of(record));

        landRecordService.delete(11L);

        verify(landRecordRepository).delete(record);
    }

    private Owner owner(Long id, String name) {
        Owner owner = new Owner();
        owner.setName(name);
        owner.setNationalId("NID-" + id);
        setId(owner, id);
        return owner;
    }

    private LandRecord landRecord(Long id, String surveyNumber, Owner owner) {
        LandRecord record = new LandRecord();
        record.setSurveyNumber(surveyNumber);
        record.setDistrict("Hyderabad");
        record.setVillage("Madhapur");
        record.setAreaInAcres(new BigDecimal("2.00"));
        record.setOwner(owner);
        setId(record, id);
        return record;
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
