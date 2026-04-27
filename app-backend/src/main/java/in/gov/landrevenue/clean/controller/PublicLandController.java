package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.land.LandRecordResponse;
import in.gov.landrevenue.clean.dto.mutation.MutationResponse;
import in.gov.landrevenue.clean.dto.registration.RegistrationResponse;
import in.gov.landrevenue.clean.entity.LandRecord;
import in.gov.landrevenue.clean.entity.LandRegistration;
import in.gov.landrevenue.clean.entity.MutationApplication;
import in.gov.landrevenue.clean.enums.RegistrationStatus;
import in.gov.landrevenue.clean.mapper.LandRevenueMapper;
import in.gov.landrevenue.clean.repository.LandRecordRepository;
import in.gov.landrevenue.clean.repository.LandRegistrationRepository;
import in.gov.landrevenue.clean.repository.MutationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Public endpoints — no authentication required.
 * Provides:
 *  - Property search by district/village/surveyNumber
 *  - Encumbrance Certificate (EC) query
 */
@RestController
@RequestMapping("/api/public")
public class PublicLandController {

    private final LandRecordRepository landRecordRepository;
    private final LandRegistrationRepository registrationRepository;
    private final MutationRepository mutationRepository;
    private final LandRevenueMapper mapper;

    public PublicLandController(LandRecordRepository landRecordRepository,
                                LandRegistrationRepository registrationRepository,
                                MutationRepository mutationRepository,
                                LandRevenueMapper mapper) {
        this.landRecordRepository = landRecordRepository;
        this.registrationRepository = registrationRepository;
        this.mutationRepository = mutationRepository;
        this.mapper = mapper;
    }

    /**
     * Public property search — returns land records matching district + village + optional surveyNumber.
     * GET /api/public/search?district=Rangareddy&village=Shamshabad&surveyNumber=123
     */
    @GetMapping("/search")
    public ResponseEntity<List<LandRecordResponse>> searchProperty(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String village,
            @RequestParam(required = false) String surveyNumber) {

        Specification<LandRecord> spec = Specification.where(null);
        if (district != null && !district.isBlank()) {
            String q = district.trim().toLowerCase();
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("district")), "%" + q + "%"));
        }
        if (village != null && !village.isBlank()) {
            String q = village.trim().toLowerCase();
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("village")), "%" + q + "%"));
        }
        if (surveyNumber != null && !surveyNumber.isBlank()) {
            String q = surveyNumber.trim().toLowerCase();
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("surveyNumber")), "%" + q + "%"));
        }

        List<LandRecordResponse> results = landRecordRepository.findAll(spec,
                        PageRequest.of(0, 50, Sort.by("surveyNumber")))
                .stream()
                .map(mapper::toLandResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    /**
     * Encumbrance Certificate (EC) — returns approved registrations and mutations for a property.
     * GET /api/public/ec?district=Rangareddy&village=Shamshabad&surveyNumber=123
     */
    @GetMapping("/ec")
    public ResponseEntity<Map<String, Object>> encumbranceCertificate(
            @RequestParam String district,
            @RequestParam String village,
            @RequestParam String surveyNumber) {

        // Find approved registrations for this property
        List<LandRegistration> registrations = registrationRepository
                .findAll()
                .stream()
                .filter(r -> r.getStatus() == RegistrationStatus.APPROVED
                        && district.equalsIgnoreCase(r.getPropertyDistrict())
                        && village.equalsIgnoreCase(r.getPropertyVillage())
                        && surveyNumber.equalsIgnoreCase(r.getPropertySurveyNumber()))
                .collect(Collectors.toList());

        // Find land record(s) matching this property to get mutation history
        Specification<LandRecord> spec = Specification.where(null);
        spec = spec.and((root, cq, cb) -> cb.equal(cb.lower(root.get("district")), district.toLowerCase()));
        spec = spec.and((root, cq, cb) -> cb.equal(cb.lower(root.get("village")), village.toLowerCase()));
        spec = spec.and((root, cq, cb) -> cb.equal(cb.lower(root.get("surveyNumber")), surveyNumber.toLowerCase()));

        List<LandRecord> landRecords = landRecordRepository.findAll(spec, PageRequest.of(0, 10, Sort.unsorted())).getContent();

        List<MutationApplication> mutations = landRecords.stream()
                .flatMap(lr -> mutationRepository.findByLandRecordIdOrderByAppliedAtDesc(lr.getId()).stream())
                .collect(Collectors.toList());

        Map<String, Object> ec = new HashMap<>();
        ec.put("generatedAt", Instant.now().toString());
        ec.put("district", district);
        ec.put("village", village);
        ec.put("surveyNumber", surveyNumber);
        ec.put("landRecords", landRecords.stream().map(mapper::toLandResponse).collect(Collectors.toList()));
        ec.put("registrations", registrations.stream().map(this::toRegistrationSummary).collect(Collectors.toList()));
        ec.put("mutations", mutations.stream().map(this::toMutationSummary).collect(Collectors.toList()));
        ec.put("encumbranceCount", registrations.size() + mutations.size());

        return ResponseEntity.ok(ec);
    }

    private Map<String, Object> toRegistrationSummary(LandRegistration r) {
        Map<String, Object> m = new HashMap<>();
        m.put("registrationRef", r.getRegistrationRef());
        m.put("status", r.getStatus().name());
        m.put("sellerName", r.getSellerName());
        m.put("buyerName", r.getBuyerName());
        m.put("considerationAmount", r.getConsiderationAmount());
        m.put("createdAt", r.getCreatedAt());
        m.put("blockchainTxHash", r.getBlockchainTxHash());
        return m;
    }

    private Map<String, Object> toMutationSummary(MutationApplication m) {
        Map<String, Object> map = new HashMap<>();
        map.put("mutationRef", m.getMutationRef());
        map.put("mutationType", m.getMutationType());
        map.put("status", m.getStatus().name());
        map.put("previousOwnerName", m.getPreviousOwnerName());
        map.put("newOwnerName", m.getNewOwnerName());
        map.put("appliedAt", m.getAppliedAt());
        map.put("decidedAt", m.getDecidedAt());
        return map;
    }
}
