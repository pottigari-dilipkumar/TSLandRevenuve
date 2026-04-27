package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.mutation.MutationDecisionRequest;
import in.gov.landrevenue.clean.dto.mutation.MutationRequest;
import in.gov.landrevenue.clean.dto.mutation.MutationResponse;
import in.gov.landrevenue.clean.entity.MutationApplication;
import in.gov.landrevenue.clean.enums.MutationStatus;
import in.gov.landrevenue.clean.exception.ResourceNotFoundException;
import in.gov.landrevenue.clean.repository.LandRecordRepository;
import in.gov.landrevenue.clean.repository.MutationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class MutationService {

    private final MutationRepository mutationRepository;
    private final LandRecordRepository landRecordRepository;
    private final AuditLogService auditLogService;

    public MutationService(MutationRepository mutationRepository,
                           LandRecordRepository landRecordRepository,
                           AuditLogService auditLogService) {
        this.mutationRepository = mutationRepository;
        this.landRecordRepository = landRecordRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public MutationResponse apply(MutationRequest request, Long applicantUserId) {
        // Verify land record exists
        landRecordRepository.findById(request.landRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Land record not found: " + request.landRecordId()));

        MutationApplication app = new MutationApplication();
        app.setMutationRef(generateRef());
        mapRequest(app, request);
        app.setAppliedByUserId(applicantUserId);
        app.setAppliedAt(Instant.now());
        app.setStatus(MutationStatus.APPLIED);

        MutationApplication saved = mutationRepository.save(app);
        auditLogService.log("CREATE", "MutationApplication",
                "Mutation " + saved.getMutationRef() + " applied for land=" + request.landRecordId());
        return toResponse(saved);
    }

    public Page<MutationResponse> list(Pageable pageable) {
        return mutationRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<MutationResponse> listByLand(Long landRecordId, Pageable pageable) {
        return mutationRepository.findByLandRecordId(landRecordId, pageable).map(this::toResponse);
    }

    public Page<MutationResponse> listByStatus(MutationStatus status, Pageable pageable) {
        return mutationRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    public MutationResponse getByRef(String ref) {
        return toResponse(findByRef(ref));
    }

    @Transactional
    public MutationResponse sendToReview(String ref) {
        MutationApplication app = findByRef(ref);
        requireStatus(app, MutationStatus.APPLIED);
        app.setStatus(MutationStatus.MANDAL_REVIEW);
        app.setReviewedAt(Instant.now());
        auditLogService.log("UPDATE", "MutationApplication",
                "Mutation " + ref + " sent to Mandal review");
        return toResponse(mutationRepository.save(app));
    }

    @Transactional
    public MutationResponse approve(String ref, MutationDecisionRequest decision, Long officerUserId) {
        MutationApplication app = findByRef(ref);
        requireStatus(app, MutationStatus.MANDAL_REVIEW);
        app.setStatus(MutationStatus.APPROVED);
        app.setDecidedAt(Instant.now());
        app.setDecidedByUserId(officerUserId);
        if (decision != null && decision.remarks() != null) app.setRemarks(decision.remarks());
        auditLogService.log("UPDATE", "MutationApplication",
                "Mutation " + ref + " APPROVED by user=" + officerUserId);
        return toResponse(mutationRepository.save(app));
    }

    @Transactional
    public MutationResponse reject(String ref, MutationDecisionRequest decision, Long officerUserId) {
        MutationApplication app = findByRef(ref);
        if (app.getStatus() == MutationStatus.APPROVED) {
            throw new IllegalStateException("Cannot reject an already approved mutation");
        }
        app.setStatus(MutationStatus.REJECTED);
        app.setDecidedAt(Instant.now());
        app.setDecidedByUserId(officerUserId);
        if (decision != null) {
            app.setRejectionReason(decision.rejectionReason());
            app.setRemarks(decision.remarks());
        }
        auditLogService.log("UPDATE", "MutationApplication",
                "Mutation " + ref + " REJECTED by user=" + officerUserId);
        return toResponse(mutationRepository.save(app));
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private MutationApplication findByRef(String ref) {
        return mutationRepository.findByMutationRef(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Mutation not found: " + ref));
    }

    private void requireStatus(MutationApplication app, MutationStatus expected) {
        if (app.getStatus() != expected) {
            throw new IllegalStateException(
                    "Mutation must be in " + expected + " state, but is " + app.getStatus());
        }
    }

    private void mapRequest(MutationApplication app, MutationRequest req) {
        app.setLandRecordId(req.landRecordId());
        app.setRegistrationRef(req.registrationRef());
        app.setMutationType(req.mutationType().toUpperCase());
        app.setPreviousOwnerName(req.previousOwnerName());
        app.setPreviousOwnerAadhaar(req.previousOwnerAadhaar());
        app.setNewOwnerName(req.newOwnerName());
        app.setNewOwnerAadhaar(req.newOwnerAadhaar());
        app.setNewOwnerMobile(req.newOwnerMobile());
        app.setNewOwnerEmail(req.newOwnerEmail());
        app.setNewOwnerAddress(req.newOwnerAddress());
        app.setRelationToDeceased(req.relationToDeceased());
        app.setDateOfDeath(req.dateOfDeath());
        app.setRemarks(req.remarks());
    }

    private String generateRef() {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return "MUT-" + uid;
    }

    private MutationResponse toResponse(MutationApplication a) {
        return new MutationResponse(
                a.getId(), a.getMutationRef(), a.getLandRecordId(), a.getRegistrationRef(),
                a.getMutationType(), a.getStatus().name(),
                a.getPreviousOwnerName(), a.getPreviousOwnerAadhaar(),
                a.getNewOwnerName(), a.getNewOwnerAadhaar(), a.getNewOwnerMobile(),
                a.getNewOwnerEmail(), a.getNewOwnerAddress(),
                a.getRelationToDeceased(), a.getDateOfDeath(),
                a.getAppliedByUserId(), a.getDecidedByUserId(),
                a.getAppliedAt(), a.getReviewedAt(), a.getDecidedAt(),
                a.getRejectionReason(), a.getRemarks()
        );
    }
}
