package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.land.LandRecordRequest;
import in.gov.landrevenue.clean.dto.land.LandRecordResponse;
import in.gov.landrevenue.clean.entity.LandRecord;
import in.gov.landrevenue.clean.entity.Owner;
import in.gov.landrevenue.clean.enums.Role;
import in.gov.landrevenue.clean.exception.ResourceNotFoundException;
import in.gov.landrevenue.clean.mapper.LandRevenueMapper;
import in.gov.landrevenue.clean.repository.LandRecordRepository;
import in.gov.landrevenue.clean.repository.OwnerRepository;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LandRecordService {
    private static final Logger log = LoggerFactory.getLogger(LandRecordService.class);
    private final LandRecordRepository landRecordRepository;
    private final OwnerRepository ownerRepository;
    private final LandRevenueMapper mapper;
    private final AuditLogService auditLogService;

    public LandRecordService(LandRecordRepository landRecordRepository,
                             OwnerRepository ownerRepository,
                             LandRevenueMapper mapper,
                             AuditLogService auditLogService) {
        this.landRecordRepository = landRecordRepository;
        this.ownerRepository = ownerRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
    }

    public LandRecordResponse create(LandRecordRequest request) {
        Owner owner = ownerRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found for ID: " + request.ownerId()));
        LandRecord landRecord = new LandRecord();
        mapRequest(landRecord, request, owner);
        LandRecord saved = landRecordRepository.save(landRecord);
        auditLogService.log("CREATE", "LandRecord", "Created land record id=" + saved.getId());
        log.info("Land record created with ID {}", saved.getId());
        return mapper.toLandResponse(saved);
    }

    public Page<LandRecordResponse> list(String surveyNumber, String district, String village, String ownerName, Pageable pageable) {
        Specification<LandRecord> spec = Specification.where(null);
        if (surveyNumber != null && !surveyNumber.isBlank()) {
            String query = surveyNumber.trim().toLowerCase();
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("surveyNumber")), "%" + query + "%"));
        }
        if (district != null && !district.isBlank()) {
            String query = district.trim().toLowerCase();
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("district")), "%" + query + "%"));
        }
        if (village != null && !village.isBlank()) {
            String query = village.trim().toLowerCase();
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("village")), "%" + query + "%"));
        }
        if (ownerName != null && !ownerName.isBlank()) {
            String query = ownerName.trim().toLowerCase();
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.join("owner", JoinType.LEFT).get("name")), "%" + query + "%"));
        }

        return landRecordRepository.findAll(spec, pageable).map(mapper::toLandResponse);
    }

    public List<LandRecordResponse> listForExport(String surveyNumber, String district, String village, String ownerName) {
        return list(surveyNumber, district, village, ownerName, Pageable.unpaged()).getContent();
    }

    public LandRecordResponse getById(Long id) {
        LandRecord record = findEntity(id);
        return mapper.toLandResponse(record);
    }

    public LandRecordResponse update(Long id, LandRecordRequest request) {
        LandRecord existing = findEntity(id);
        Owner owner = ownerRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found for ID: " + request.ownerId()));
        mapRequest(existing, request, owner);
        LandRecord saved = landRecordRepository.save(existing);
        auditLogService.log("UPDATE", "LandRecord", "Updated land record id=" + id);
        log.info("Land record {} updated", id);
        return mapper.toLandResponse(saved);
    }

    public void delete(Long id) {
        LandRecord existing = findEntity(id);
        landRecordRepository.delete(existing);
        auditLogService.log("DELETE", "LandRecord", "Deleted land record id=" + id);
        log.info("Land record {} deleted", id);
    }

    public LandRecord findEntity(Long id) {
        if (isCitizen()) {
            return landRecordRepository.findByIdAndOwner_NationalId(id, currentUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Land record not found for ID: " + id));
        }
        return landRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land record not found for ID: " + id));
    }

    private boolean isCitizen() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + Role.CITIZEN.name()));
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ResourceNotFoundException("Unauthenticated user");
        }
        return authentication.getName();
    }

    private void mapRequest(LandRecord landRecord, LandRecordRequest request, Owner owner) {
        landRecord.setSurveyNumber(request.surveyNumber());
        landRecord.setDistrict(request.district());
        landRecord.setVillage(request.village());
        landRecord.setAreaInAcres(request.areaInAcres());
        landRecord.setOwner(owner);
    }
}
