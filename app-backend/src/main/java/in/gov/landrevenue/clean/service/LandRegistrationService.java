/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.blockchain.BlockchainAnchorResult;
import in.gov.landrevenue.blockchain.BlockchainRegistrationGateway;
import in.gov.landrevenue.clean.dto.registration.*;
import in.gov.landrevenue.clean.entity.*;
import in.gov.landrevenue.clean.enums.LandType;
import in.gov.landrevenue.clean.enums.RegistrationStatus;
import in.gov.landrevenue.clean.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class LandRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(LandRegistrationService.class);
    private static final BigDecimal STAMP_DUTY_RATE = new BigDecimal("0.07");

    private final LandRegistrationRepository registrationRepository;
    private final MarketValueRepository marketValueRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final NotificationBellService notificationBellService;
    private final RegistrationBlockchainEventRepository eventRepository;
    private final BlockchainRegistrationGateway blockchainGateway;
    private final LandRecordRepository landRecordRepository;
    private final OwnerRepository ownerRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public LandRegistrationService(LandRegistrationRepository registrationRepository,
                                    MarketValueRepository marketValueRepository,
                                    UserRepository userRepository,
                                    NotificationService notificationService,
                                    NotificationBellService notificationBellService,
                                    RegistrationBlockchainEventRepository eventRepository,
                                    BlockchainRegistrationGateway blockchainGateway,
                                    LandRecordRepository landRecordRepository,
                                    OwnerRepository ownerRepository) {
        this.registrationRepository = registrationRepository;
        this.marketValueRepository = marketValueRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.notificationBellService = notificationBellService;
        this.eventRepository = eventRepository;
        this.blockchainGateway = blockchainGateway;
        this.landRecordRepository = landRecordRepository;
        this.ownerRepository = ownerRepository;
    }

    @Transactional
    public RegistrationResponse createDraft(RegistrationDraftRequest request, Long draftedByUserId) {
        LandRegistration reg = new LandRegistration();
        reg.setRegistrationRef(UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        reg.setStatus(RegistrationStatus.DRAFT);

        reg.setPropertyDistrict(request.propertyDistrict());
        reg.setPropertyVillage(request.propertyVillage());
        reg.setPropertySurveyNumber(request.propertySurveyNumber());
        reg.setPropertyAreaInAcres(request.propertyAreaInAcres());
        reg.setConsiderationAmount(request.considerationAmount());
        reg.setPropertyLatitude(request.propertyLatitude());
        reg.setPropertyLongitude(request.propertyLongitude());
        reg.setPropertyGeometry(request.propertyGeometry());
        reg.setPropertyPlusCode(request.propertyPlusCode());

        marketValueRepository.findCurrentRate(request.propertyDistrict(), request.propertyVillage())
                .ifPresent(mv -> {
                    reg.setMarketValuePerAcre(mv.getRatePerAcre());
                    BigDecimal total = mv.getRatePerAcre().multiply(request.propertyAreaInAcres())
                            .setScale(2, RoundingMode.HALF_UP);
                    reg.setTotalMarketValue(total);
                    reg.setStampDuty(total.multiply(STAMP_DUTY_RATE).setScale(2, RoundingMode.HALF_UP));
                });

        RegistrationPartyDto seller = request.seller();
        reg.setSellerName(seller.name());
        reg.setSellerAadhaar(seller.aadhaarNumber());
        reg.setSellerMobile(seller.mobile());
        reg.setSellerEmail(seller.email());
        reg.setSellerAddress(seller.address());

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

        String actor = resolveUsername(draftedByUserId);
        logEvent(saved.getRegistrationRef(), "DRAFTED", actor, "STAFF",
                "Draft created by " + actor +
                (saved.getPropertyLatitude() != null
                        ? String.format(" at (%.6f, %.6f)", saved.getPropertyLatitude(), saved.getPropertyLongitude())
                        : ""));

        return toResponse(saved);
    }

    @Transactional
    public RegistrationResponse submitForApproval(String registrationRef, String actorUsername) {
        LandRegistration reg = findByRef(registrationRef);
        if (reg.getStatus() != RegistrationStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT registrations can be submitted");
        }
        reg.setStatus(RegistrationStatus.PENDING_APPROVAL);
        reg.setSubmittedAt(Instant.now());
        LandRegistration saved = registrationRepository.save(reg);

        logEvent(registrationRef, "SUBMITTED", actorUsername, "STAFF",
                "Submitted for SRO approval by " + actorUsername);

        notificationService.notifyRegistrationDrafted(reg.getBuyerEmail(), reg.getBuyerMobile(),
                reg.getSellerEmail(), reg.getSellerMobile(), registrationRef);
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

        String actor = resolveUsername(sroUserId);
        logEvent(registrationRef, "APPROVED", actor, "SRO", "Approved by SRO: " + actor);

        // Attempt blockchain anchoring
        try {
            BlockchainAnchorResult anchor = blockchainGateway.anchorLandRegistration(
                    registrationRef, reg.getPropertySurveyNumber(), reg.getPropertyDistrict());

            saved.setBlockchainSyncStatus(anchor.syncStatus());
            saved.setBlockchainSyncedAt(anchor.syncedAt() != null ? anchor.syncedAt() : Instant.now());
            if (anchor.transactionHash() != null) saved.setBlockchainTxHash(anchor.transactionHash());
            if (anchor.blockNumber() != null) saved.setBlockchainBlockNumber(anchor.blockNumber());
            saved = registrationRepository.save(saved);

            logEvent(registrationRef, "BLOCKCHAIN_ANCHORED", "SYSTEM", "SYSTEM",
                    "Chain anchor: status=" + anchor.syncStatus()
                    + (anchor.transactionHash() != null ? ", tx=" + anchor.transactionHash() : "")
                    + (anchor.errorMessage() != null ? ", err=" + anchor.errorMessage() : ""),
                    anchor.transactionHash(), anchor.blockNumber(), anchor.syncStatus());
        } catch (Exception ex) {
            logEvent(registrationRef, "BLOCKCHAIN_ANCHORED", "SYSTEM", "SYSTEM",
                    "Chain anchor failed: " + ex.getMessage(), null, null, "FAILED");
        }

        // Auto-create / update LandRecord so the buyer becomes the new owner
        upsertLandRecord(saved);

        notificationService.notifyRegistrationApproved(reg.getBuyerEmail(), reg.getBuyerMobile(),
                reg.getSellerEmail(), reg.getSellerMobile(), registrationRef);
        notificationBellService.onApproved(saved.getSellerUserId(), saved.getBuyerAadhaar(),
                saved.getBuyerName(), registrationRef, saved.getPropertySurveyNumber());
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

        String actor = resolveUsername(sroUserId);
        logEvent(registrationRef, "REJECTED", actor, "SRO",
                "Rejected by SRO: " + actor + " — reason: " + reason);

        notificationService.notifyRegistrationRejected(reg.getBuyerEmail(), reg.getBuyerMobile(),
                reg.getSellerEmail(), reg.getSellerMobile(), registrationRef, reason);
        notificationBellService.onRejected(saved.getSellerUserId(), saved.getBuyerAadhaar(),
                registrationRef, saved.getPropertySurveyNumber(), reason);
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

    /** Citizen creates a sale request for a land they own. */
    @Transactional
    public RegistrationResponse createSaleRequest(SaleRequestCreateRequest request, String sellerUsername) {
        User seller = userRepository.findByUsername(sellerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + sellerUsername));

        LandRecord land = landRecordRepository.findById(request.landRecordId())
                .orElseThrow(() -> new IllegalArgumentException("Land record not found: " + request.landRecordId()));

        if (land.getOwner() == null || !land.getOwner().getNationalId().equals(seller.getAadhaarNumber())) {
            throw new IllegalStateException("You can only sell land that you own");
        }

        LandRegistration reg = new LandRegistration();
        reg.setRegistrationRef(UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        reg.setStatus(RegistrationStatus.DRAFT);
        reg.setInitiatedByCitizen(true);
        reg.setSellerUserId(seller.getId());
        reg.setDraftedByUserId(seller.getId());

        reg.setPropertyDistrict(land.getDistrict());
        reg.setPropertyVillage(land.getVillage());
        reg.setPropertySurveyNumber(land.getSurveyNumber());
        reg.setPropertyAreaInAcres(land.getAreaInAcres());
        reg.setPropertyGeometry(land.getGeometry());
        reg.setPropertyPlusCode(land.getPlusCode());
        reg.setConsiderationAmount(request.considerationAmount());

        marketValueRepository.findCurrentRate(land.getDistrict(), land.getVillage())
                .ifPresent(mv -> {
                    reg.setMarketValuePerAcre(mv.getRatePerAcre());
                    java.math.BigDecimal total = mv.getRatePerAcre().multiply(land.getAreaInAcres())
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                    reg.setTotalMarketValue(total);
                    reg.setStampDuty(total.multiply(STAMP_DUTY_RATE).setScale(2, java.math.RoundingMode.HALF_UP));
                });

        reg.setSellerName(seller.getFullName() != null ? seller.getFullName() : seller.getUsername());
        reg.setSellerAadhaar(seller.getAadhaarNumber());
        reg.setSellerMobile(seller.getMobile());
        reg.setSellerEmail(seller.getEmail());
        reg.setSellerAddress(seller.getAddress());

        reg.setBuyerName(request.buyerName());
        reg.setBuyerAadhaar(request.buyerAadhaar());
        reg.setBuyerMobile(request.buyerMobile());
        reg.setBuyerEmail(request.buyerEmail());
        reg.setBuyerAddress(request.buyerAddress());
        reg.setNotes(request.notes());
        reg.setCreatedAt(Instant.now());

        LandRegistration saved = registrationRepository.save(reg);
        logEvent(saved.getRegistrationRef(), "DRAFTED", seller.getUsername(), "CITIZEN",
                "Sale request created by citizen seller " + seller.getUsername());
        return toResponse(saved);
    }

    /** Seller submits sale request to buyer for consent. DRAFT → AWAITING_BUYER_APPROVAL */
    @Transactional
    public RegistrationResponse sellerSubmit(String ref, String sellerUsername) {
        LandRegistration reg = findByRef(ref);
        verifyIsSeller(reg, sellerUsername);
        if (reg.getStatus() != RegistrationStatus.DRAFT && reg.getStatus() != RegistrationStatus.REVISION_REQUIRED) {
            throw new IllegalStateException("Cannot submit in current status: " + reg.getStatus());
        }
        reg.setStatus(RegistrationStatus.AWAITING_BUYER_APPROVAL);
        reg.setSubmittedAt(Instant.now());
        reg.setRevisionNotes(null);
        LandRegistration saved = registrationRepository.save(reg);
        logEvent(ref, "SUBMITTED_TO_BUYER", sellerUsername, "CITIZEN",
                "Seller submitted for buyer approval");
        notificationBellService.onSellerSubmitted(saved.getSellerUserId(), saved.getBuyerAadhaar(),
                ref, saved.getPropertySurveyNumber(), saved.getSellerName());
        return toResponse(saved);
    }

    /** Buyer consents to the sale. AWAITING_BUYER_APPROVAL → PENDING_REVIEW */
    @Transactional
    public RegistrationResponse buyerApprove(String ref, String buyerUsername) {
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + buyerUsername));
        LandRegistration reg = findByRef(ref);
        if (reg.getStatus() != RegistrationStatus.AWAITING_BUYER_APPROVAL) {
            throw new IllegalStateException("Registration is not awaiting buyer approval");
        }
        if (!reg.getBuyerAadhaar().equals(buyer.getAadhaarNumber())) {
            throw new IllegalStateException("Your Aadhaar does not match the buyer on this registration");
        }
        reg.setStatus(RegistrationStatus.PENDING_REVIEW);
        reg.setBuyerUserId(buyer.getId());
        reg.setBuyerApprovedAt(Instant.now());
        LandRegistration saved = registrationRepository.save(reg);
        logEvent(ref, "BUYER_APPROVED", buyerUsername, "CITIZEN",
                "Buyer " + buyerUsername + " consented to the sale");
        notificationBellService.onBuyerApproved(saved.getSellerUserId(), saved.getBuyerName(),
                ref, saved.getPropertySurveyNumber());
        return toResponse(saved);
    }

    /** Buyer rejects the sale. AWAITING_BUYER_APPROVAL → BUYER_REJECTED */
    @Transactional
    public RegistrationResponse buyerReject(String ref, String reason, String buyerUsername) {
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + buyerUsername));
        LandRegistration reg = findByRef(ref);
        if (reg.getStatus() != RegistrationStatus.AWAITING_BUYER_APPROVAL) {
            throw new IllegalStateException("Registration is not awaiting buyer approval");
        }
        if (!reg.getBuyerAadhaar().equals(buyer.getAadhaarNumber())) {
            throw new IllegalStateException("Your Aadhaar does not match the buyer on this registration");
        }
        reg.setStatus(RegistrationStatus.BUYER_REJECTED);
        reg.setRejectionReason(reason);
        reg.setDecidedAt(Instant.now());
        LandRegistration saved = registrationRepository.save(reg);
        logEvent(ref, "BUYER_REJECTED", buyerUsername, "CITIZEN",
                "Buyer rejected the sale: " + reason);
        notificationBellService.onBuyerRejected(saved.getSellerUserId(), saved.getBuyerName(),
                ref, saved.getPropertySurveyNumber(), reason);
        return toResponse(saved);
    }

    /** SRO Assistant sends back for revision. PENDING_REVIEW → REVISION_REQUIRED */
    @Transactional
    public RegistrationResponse sroAssistantSendBack(String ref, String notes, String assistantUsername) {
        User assistant = userRepository.findByUsername(assistantUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + assistantUsername));
        LandRegistration reg = findByRef(ref);
        if (reg.getStatus() != RegistrationStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Registration is not under review");
        }
        reg.setStatus(RegistrationStatus.REVISION_REQUIRED);
        reg.setRevisionNotes(notes);
        reg.setSroAssistantUserId(assistant.getId());
        reg.setSroAssistantReviewedAt(Instant.now());
        LandRegistration saved = registrationRepository.save(reg);
        logEvent(ref, "SENT_BACK", assistantUsername, "SRO_ASSISTANT",
                "Sent back for revision by " + assistantUsername + ": " + notes);
        notificationBellService.onRevisionRequired(saved.getSellerUserId(), ref,
                saved.getPropertySurveyNumber(), notes);
        return toResponse(saved);
    }

    /** SRO Assistant forwards to SRO for final approval. PENDING_REVIEW → PENDING_APPROVAL */
    @Transactional
    public RegistrationResponse sroAssistantForward(String ref, String assistantUsername) {
        User assistant = userRepository.findByUsername(assistantUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + assistantUsername));
        LandRegistration reg = findByRef(ref);
        if (reg.getStatus() != RegistrationStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Registration is not under review");
        }
        reg.setStatus(RegistrationStatus.PENDING_APPROVAL);
        reg.setSroAssistantUserId(assistant.getId());
        reg.setSroAssistantReviewedAt(Instant.now());
        LandRegistration saved = registrationRepository.save(reg);
        logEvent(ref, "FORWARDED_TO_SRO", assistantUsername, "SRO_ASSISTANT",
                "Forwarded to SRO for final approval by " + assistantUsername);
        notificationBellService.onForwardedToSro(ref, saved.getPropertySurveyNumber());
        return toResponse(saved);
    }

    /** Seller resubmits after revision. REVISION_REQUIRED → PENDING_REVIEW */
    @Transactional
    public RegistrationResponse sellerResubmit(String ref, String sellerUsername) {
        LandRegistration reg = findByRef(ref);
        verifyIsSeller(reg, sellerUsername);
        if (reg.getStatus() != RegistrationStatus.REVISION_REQUIRED) {
            throw new IllegalStateException("Registration is not awaiting revision");
        }
        reg.setStatus(RegistrationStatus.PENDING_REVIEW);
        reg.setRevisionNotes(null);
        LandRegistration saved = registrationRepository.save(reg);
        logEvent(ref, "RESUBMITTED", sellerUsername, "CITIZEN",
                "Seller resubmitted after revision");
        notificationBellService.onResubmitted(ref, saved.getPropertySurveyNumber());
        return toResponse(saved);
    }

    /** Get all sale requests initiated by the given seller citizen. */
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getMySaleRequests(String sellerUsername) {
        User seller = userRepository.findByUsername(sellerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + sellerUsername));
        return registrationRepository.findBySellerUserIdOrderByCreatedAtDesc(seller.getId())
                .stream().map(this::toResponse).toList();
    }

    /** Get registrations where the given buyer citizen needs to approve. */
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getPendingBuyerApprovals(String buyerUsername) {
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + buyerUsername));
        if (buyer.getAadhaarNumber() == null) return List.of();
        return registrationRepository.findByBuyerAadhaarAndStatusOrderByCreatedAtDesc(
                buyer.getAadhaarNumber(), RegistrationStatus.AWAITING_BUYER_APPROVAL)
                .stream().map(this::toResponse).toList();
    }

    private void verifyIsSeller(LandRegistration reg, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (!user.getId().equals(reg.getSellerUserId())) {
            throw new IllegalStateException("Only the seller can perform this action");
        }
    }

    @Transactional(readOnly = true)
    public List<BlockchainEventResponse> getEvents(String registrationRef) {
        return eventRepository.findByRegistrationRefOrderBySequenceAsc(registrationRef)
                .stream()
                .map(e -> new BlockchainEventResponse(
                        e.getId(), e.getRegistrationRef(), e.getEventType(),
                        e.getActorUsername(), e.getActorRole(), e.getPayloadHash(),
                        e.getTxHash(), e.getBlockNumber(), e.getChainSyncStatus(),
                        e.getTimestamp(), e.getSequence(), e.getDetails()))
                .toList();
    }

    // ── Private helpers ──────────────────────────────────────────────────────────

    /**
     * On registration approval, create or update the corresponding LandRecord
     * so the buyer is recorded as the new owner with the polygon boundary.
     */
    private void upsertLandRecord(LandRegistration reg) {
        try {
            // Find or create an Owner record for the buyer
            Owner buyer = ownerRepository.findByNationalId(reg.getBuyerAadhaar())
                    .orElseGet(() -> {
                        Owner o = new Owner();
                        o.setName(reg.getBuyerName());
                        o.setNationalId(reg.getBuyerAadhaar());
                        return ownerRepository.save(o);
                    });

            // Find existing land record by survey number (unique key)
            LandRecord lr = landRecordRepository.findBySurveyNumber(reg.getPropertySurveyNumber())
                    .orElse(new LandRecord());

            lr.setSurveyNumber(reg.getPropertySurveyNumber());
            lr.setDistrict(reg.getPropertyDistrict());
            lr.setVillage(reg.getPropertyVillage());
            lr.setAreaInAcres(reg.getPropertyAreaInAcres());
            lr.setOwner(buyer);
            if (lr.getLandType() == null) lr.setLandType(LandType.PRIVATE);
            if (reg.getPropertyGeometry() != null) lr.setGeometry(reg.getPropertyGeometry());
            if (reg.getPropertyPlusCode() != null) lr.setPlusCode(reg.getPropertyPlusCode());

            landRecordRepository.save(lr);
            log.info("Land record upserted for survey {} with buyer {}", reg.getPropertySurveyNumber(), reg.getBuyerName());
        } catch (Exception ex) {
            log.warn("Failed to upsert land record for registration {}: {}", reg.getRegistrationRef(), ex.getMessage());
        }
    }

    // ── Event / response helpers ─────────────────────────────────────────────

    private void logEvent(String ref, String eventType, String actor, String role, String details) {
        logEvent(ref, eventType, actor, role, details, null, null, null);
    }

    private void logEvent(String ref, String eventType, String actor, String role, String details,
                          String txHash, Long blockNumber, String chainSyncStatus) {
        Instant now = Instant.now();
        long seq = eventRepository.countByRegistrationRef(ref) + 1;

        RegistrationBlockchainEvent ev = new RegistrationBlockchainEvent();
        ev.setRegistrationRef(ref);
        ev.setEventType(eventType);
        ev.setActorUsername(actor);
        ev.setActorRole(role);
        ev.setTimestamp(now);
        ev.setSequence(seq);
        ev.setDetails(details);
        ev.setTxHash(txHash);
        ev.setBlockNumber(blockNumber);
        ev.setChainSyncStatus(chainSyncStatus);
        ev.setPayloadHash(sha256(eventType + "|" + ref + "|" + now.toEpochMilli() + "|" + actor));
        eventRepository.save(ev);
    }

    private LandRegistration findByRef(String registrationRef) {
        return registrationRepository.findByRegistrationRef(registrationRef)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found: " + registrationRef));
    }

    private String resolveUsername(Long userId) {
        if (userId == null) return "SYSTEM";
        return userRepository.findById(userId).map(u -> u.getUsername()).orElse("SYSTEM");
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return "HASH_ERROR";
        }
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
                reg.isInitiatedByCitizen(), reg.getSellerUserId(), reg.getBuyerUserId(),
                reg.getBuyerApprovedAt(), reg.getSroAssistantUserId(), reg.getRevisionNotes(),
                witnesses, documents,
                reg.getPropertyLatitude(), reg.getPropertyLongitude(),
                reg.getPropertyGeometry(), reg.getPropertyPlusCode(),
                reg.getBlockchainTxHash(), reg.getBlockchainBlockNumber(),
                reg.getBlockchainSyncStatus(), reg.getBlockchainSyncedAt()
        );
    }
}
