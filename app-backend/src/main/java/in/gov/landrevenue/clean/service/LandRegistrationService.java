package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.registration.*;
import in.gov.landrevenue.clean.entity.LandRegistration;
import in.gov.landrevenue.clean.entity.RegistrationDocument;
import in.gov.landrevenue.clean.entity.RegistrationWitness;
import in.gov.landrevenue.clean.enums.RegistrationStatus;
import in.gov.landrevenue.clean.repository.LandRegistrationRepository;
import in.gov.landrevenue.clean.repository.MarketValueRepository;
import in.gov.landrevenue.clean.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LandRegistrationService {

    private static final BigDecimal STAMP_DUTY_RATE = new BigDecimal("0.07"); // 7%

    private final LandRegistrationRepository registrationRepository;
    private final MarketValueRepository marketValueRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public LandRegistrationService(LandRegistrationRepository registrationRepository,
                                    MarketValueRepository marketValueRepository,
                                    UserRepository userRepository,
                                    NotificationService notificationService) {
        this.registrationRepository = registrationRepository;
        this.marketValueRepository = marketValueRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public RegistrationResponse createDraft(RegistrationDraftRequest request, Long draftedByUserId) {
        LandRegistration reg = new LandRegistration();
        reg.setRegistrationRef(UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        reg.setStatus(RegistrationStatus.DRAFT);

        // Property
        reg.setPropertyDistrict(request.propertyDistrict());
        reg.setPropertyVillage(request.propertyVillage());
        reg.setPropertySurveyNumber(request.propertySurveyNumber());
        reg.setPropertyAreaInAcres(request.propertyAreaInAcres());
        reg.setConsiderationAmount(request.considerationAmount());

        // Market value lookup
        marketValueRepository.findCurrentRate(request.propertyDistrict(), request.propertyVillage())
                .ifPresent(mv -> {
                    reg.setMarketValuePerAcre(mv.getRatePerAcre());
                    BigDecimal total = mv.getRatePerAcre().multiply(request.propertyAreaInAcres())
                            .setScale(2, RoundingMode.HALF_UP);
                    reg.setTotalMarketValue(total);
                    reg.setStampDuty(total.multiply(STAMP_DUTY_RATE).setScale(2, RoundingMode.HALF_UP));
                });

        // Seller
        RegistrationPartyDto seller = request.seller();
        reg.setSellerName(seller.name());
        reg.setSellerAadhaar(seller.aadhaarNumber());
        reg.setSellerMobile(seller.mobile());
        reg.setSellerEmail(seller.email());
        reg.setSellerAddress(seller.address());

        // Buyer
        RegistrationPartyDto buyer = request.buyer();
        reg.setBuyerName(buyer.name());
        reg.setBuyerAadhaar(buyer.aadhaarNumber());
        reg.setBuyerMobile(buyer.mobile());
        reg.setBuyerEmail(buyer.email());
        reg.setBuyerAddress(buyer.address());

        reg.setNotes(request.notes());
        reg.setDraftedByUserId(draftedByUserId);
        reg.setCreatedAt(Instant.now());

        LandRegistration saved = registrationRepository.save(reg);

        // Witnesses
        if (request.witnesses() != null) {
            int seq = 1;
            for (RegistrationWitnessDto wd : request.witnesses()) {
                RegistrationWitness w = new RegistrationWitness();
                w.setLandRegistration(saved);
                w.setName(wd.name());
                w.setAadhaarNumber(wd.aadhaarNumber());
                w.setMobile(wd.mobile());
                w.setAddress(wd.address());
                w.setSequenceNumber(seq++);
                saved.getWitnesses().add(w);
            }
            saved = registrationRepository.save(saved);
        }

        return toResponse(saved);
    }

    @Transactional
    public RegistrationResponse submitForApproval(String registrationRef) {
        LandRegistration reg = findByRef(registrationRef);
        if (reg.getStatus() != RegistrationStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT registrations can be submitted");
        }
        reg.setStatus(RegistrationStatus.PENDING_APPROVAL);
        reg.setSubmittedAt(Instant.now());
        LandRegistration saved = registrationRepository.save(reg);
        notificationService.notifyRegistrationDrafted(reg.getBuyerEmail(), reg.getBuyerMobile(),
                reg.getSellerEmail(), reg.getSellerMobile(), reg.getRegistrationRef());
        return toResponse(saved);
    }

    @Transactional
    public RegistrationResponse approve(String registrationRef, Long sroUserId) {
        LandRegistration reg = findByRef(registrationRef);
        if (reg.getStatus() != RegistrationStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL registrations can be approved");
        }
        reg.setStatus(RegistrationStatus.APPROVED);
        reg.setApprovedByUserId(sroUserId);
        reg.setDecidedAt(Instant.now());
        LandRegistration saved = registrationRepository.save(reg);
        notificationService.notifyRegistrationApproved(reg.getBuyerEmail(), reg.getBuyerMobile(),
                reg.getSellerEmail(), reg.getSellerMobile(), reg.getRegistrationRef());
        return toResponse(saved);
    }

    @Transactional
    public RegistrationResponse reject(String registrationRef, String reason, Long sroUserId) {
        LandRegistration reg = findByRef(registrationRef);
        if (reg.getStatus() != RegistrationStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL registrations can be rejected");
        }
        reg.setStatus(RegistrationStatus.REJECTED);
        reg.setRejectionReason(reason);
        reg.setApprovedByUserId(sroUserId);
        reg.setDecidedAt(Instant.now());
        LandRegistration saved = registrationRepository.save(reg);
        notificationService.notifyRegistrationRejected(reg.getBuyerEmail(), reg.getBuyerMobile(),
                reg.getSellerEmail(), reg.getSellerMobile(), reg.getRegistrationRef(), reason);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RegistrationResponse getByRef(String registrationRef) {
        return toResponse(findByRef(registrationRef));
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getAll() {
        return registrationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getByStatus(RegistrationStatus status) {
        return registrationRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getForCitizen(String aadhaarNumber) {
        return registrationRepository.findByPartyAadhaar(aadhaarNumber).stream().map(this::toResponse).toList();
    }

    private LandRegistration findByRef(String registrationRef) {
        return registrationRepository.findByRegistrationRef(registrationRef)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found: " + registrationRef));
    }

    private RegistrationResponse toResponse(LandRegistration reg) {
        List<WitnessResponse> witnesses = reg.getWitnesses().stream().map(w ->
                new WitnessResponse(w.getId(), w.getName(), w.getAadhaarNumber(),
                        w.getMobile(), w.getAddress(), w.getSequenceNumber())).toList();

        List<DocumentResponse> documents = reg.getDocuments().stream().map(d ->
                new DocumentResponse(d.getId(), d.getOriginalName(), d.getDocumentType().name(),
                        d.getDescription(), baseUrl + "/api/documents/" + d.getId() + "/download",
                        d.getUploadedAt())).toList();

        return new RegistrationResponse(
                reg.getId(), reg.getRegistrationRef(), reg.getStatus().name(),
                reg.getPropertyDistrict(), reg.getPropertyVillage(), reg.getPropertySurveyNumber(),
                reg.getPropertyAreaInAcres(), reg.getMarketValuePerAcre(), reg.getTotalMarketValue(),
                reg.getConsiderationAmount(), reg.getStampDuty(),
                reg.getSellerName(), reg.getSellerAadhaar(), reg.getSellerMobile(),
                reg.getSellerEmail(), reg.getSellerAddress(),
                reg.getBuyerName(), reg.getBuyerAadhaar(), reg.getBuyerMobile(),
                reg.getBuyerEmail(), reg.getBuyerAddress(),
                reg.getDraftedByUserId(), reg.getApprovedByUserId(),
                reg.getRejectionReason(), reg.getNotes(),
                reg.getCreatedAt(), reg.getSubmittedAt(), reg.getDecidedAt(),
                witnesses, documents
        );
    }
}
