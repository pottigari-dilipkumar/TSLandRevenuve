package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.entity.Notification;
import in.gov.landrevenue.clean.entity.User;
import in.gov.landrevenue.clean.enums.Role;
import in.gov.landrevenue.clean.repository.NotificationRepository;
import in.gov.landrevenue.clean.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationBellService {

    private static final Logger log = LoggerFactory.getLogger(NotificationBellService.class);

    private final NotificationRepository notifRepo;
    private final UserRepository userRepo;

    public NotificationBellService(NotificationRepository notifRepo, UserRepository userRepo) {
        this.notifRepo = notifRepo;
        this.userRepo = userRepo;
    }

    // ── Create helpers ────────────────────────────────────────────────────────

    public void notifyUser(Long userId, String type, String title, String message,
                           String ref, String refType) {
        if (userId == null) return;
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setReferenceRef(ref);
        n.setReferenceType(refType);
        notifRepo.save(n);
    }

    public void notifyByAadhaar(String aadhaar, String type, String title, String message,
                                String ref, String refType) {
        if (aadhaar == null) return;
        userRepo.findByAadhaarNumber(aadhaar)
                .ifPresent(u -> notifyUser(u.getId(), type, title, message, ref, refType));
    }

    public void notifyAllWithRole(Role role, String type, String title, String message,
                                  String ref, String refType) {
        userRepo.findByRole(role).forEach(u ->
                notifyUser(u.getId(), type, title, message, ref, refType));
    }

    // ── Registration workflow notifications ───────────────────────────────────

    public void onSellerSubmitted(Long sellerUserId, String buyerAadhaar,
                                  String ref, String surveyNumber, String sellerName) {
        // Buyer: you received a sale request
        notifyByAadhaar(buyerAadhaar, "SALE_REQUEST_RECEIVED",
                "Sale Request Received",
                sellerName + " wants to sell you land (Survey: " + surveyNumber + "). Please review and consent.",
                ref, "REGISTRATION");
    }

    public void onBuyerApproved(Long sellerUserId, String buyerName,
                                String ref, String surveyNumber) {
        // Seller: buyer consented
        notifyUser(sellerUserId, "BUYER_CONSENTED",
                "Buyer Consented ✓",
                buyerName + " has approved the sale of Survey " + surveyNumber + ". Now under SRO review.",
                ref, "REGISTRATION");
        // SRO Assistants: new case to review
        notifyAllWithRole(Role.SRO_ASSISTANT, "REVIEW_REQUESTED",
                "New Registration for Review",
                "Sale request " + ref + " (Survey: " + surveyNumber + ") is ready for SRO Assistant review.",
                ref, "REGISTRATION");
    }

    public void onBuyerRejected(Long sellerUserId, String buyerName,
                                String ref, String surveyNumber, String reason) {
        notifyUser(sellerUserId, "BUYER_REJECTED",
                "Sale Rejected by Buyer",
                buyerName + " rejected the sale of Survey " + surveyNumber
                        + (reason != null ? ": " + reason : "."),
                ref, "REGISTRATION");
    }

    public void onRevisionRequired(Long sellerUserId, String ref,
                                   String surveyNumber, String notes) {
        notifyUser(sellerUserId, "REVISION_REQUIRED",
                "Revision Required",
                "SRO Assistant requires changes for " + ref + " (Survey: " + surveyNumber + "): " + notes,
                ref, "REGISTRATION");
    }

    public void onForwardedToSro(String ref, String surveyNumber) {
        notifyAllWithRole(Role.SRO, "PENDING_FINAL_APPROVAL",
                "Registration Pending Final Approval",
                "Registration " + ref + " (Survey: " + surveyNumber + ") has been reviewed by assistant and needs your approval.",
                ref, "REGISTRATION");
    }

    public void onResubmitted(String ref, String surveyNumber) {
        notifyAllWithRole(Role.SRO_ASSISTANT, "RESUBMITTED",
                "Registration Resubmitted",
                "Seller has resubmitted " + ref + " (Survey: " + surveyNumber + ") after revision.",
                ref, "REGISTRATION");
    }

    public void onApproved(Long sellerUserId, String buyerAadhaar, String buyerName,
                           String ref, String surveyNumber) {
        notifyUser(sellerUserId, "REGISTRATION_APPROVED",
                "Registration Approved ✓",
                "Your sale of Survey " + surveyNumber + " (Ref: " + ref + ") has been approved by the SRO. " + buyerName + " is now the owner.",
                ref, "REGISTRATION");
        notifyByAadhaar(buyerAadhaar, "REGISTRATION_APPROVED",
                "Land Ownership Transferred to You ✓",
                "Congratulations! Survey " + surveyNumber + " has been registered in your name (Ref: " + ref + ").",
                ref, "REGISTRATION");
    }

    public void onRejected(Long sellerUserId, String buyerAadhaar,
                           String ref, String surveyNumber, String reason) {
        String msg = "Registration " + ref + " (Survey: " + surveyNumber + ") was rejected"
                + (reason != null ? ": " + reason : ".");
        notifyUser(sellerUserId, "REGISTRATION_REJECTED", "Registration Rejected", msg, ref, "REGISTRATION");
        notifyByAadhaar(buyerAadhaar, "REGISTRATION_REJECTED", "Registration Rejected", msg, ref, "REGISTRATION");
    }

    // ── Query ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getNotifications(Long userId) {
        return notifRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(50)
                .map(n -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", n.getId());
                    m.put("type", n.getType());
                    m.put("title", n.getTitle());
                    m.put("message", n.getMessage());
                    m.put("referenceRef", n.getReferenceRef());
                    m.put("referenceType", n.getReferenceType());
                    m.put("read", n.isRead());
                    m.put("createdAt", n.getCreatedAt());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notifRepo.countUnreadByUserId(userId);
    }

    @Transactional
    public void markRead(Long notifId, Long userId) {
        notifRepo.findById(notifId).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setRead(true);
                notifRepo.save(n);
            }
        });
    }

    @Transactional
    public void markAllRead(Long userId) {
        notifRepo.markAllReadByUserId(userId);
    }
}
