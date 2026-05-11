package in.gov.landrevenue.notification.controller;

import in.gov.landrevenue.notification.dto.NotificationRequest;
import in.gov.landrevenue.notification.dto.NotificationResponse;
import in.gov.landrevenue.notification.dto.RegistrationEventRequest;
import in.gov.landrevenue.notification.entity.NotificationLog;
import in.gov.landrevenue.notification.repository.NotificationLogRepository;
import in.gov.landrevenue.notification.service.EmailService;
import in.gov.landrevenue.notification.service.NotificationDispatchService;
import in.gov.landrevenue.notification.service.SmsService;
import in.gov.landrevenue.notification.service.WhatsAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notify")
@Tag(name = "Notifications", description = "Email, SMS, WhatsApp, and registration event dispatch")
public class NotificationController {

    private final EmailService emailService;
    private final SmsService smsService;
    private final WhatsAppService whatsAppService;
    private final NotificationDispatchService dispatchService;
    private final NotificationLogRepository logRepository;

    public NotificationController(EmailService emailService, SmsService smsService,
                                   WhatsAppService whatsAppService,
                                   NotificationDispatchService dispatchService,
                                   NotificationLogRepository logRepository) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.whatsAppService = whatsAppService;
        this.dispatchService = dispatchService;
        this.logRepository = logRepository;
    }

    @PostMapping("/email")
    @Operation(summary = "Send an email notification")
    public ResponseEntity<NotificationResponse> sendEmail(@Valid @RequestBody NotificationRequest req) {
        boolean ok = emailService.send(req.recipient(), req.subject(), req.message(), req.reference());
        return ResponseEntity.ok(new NotificationResponse(ok, "EMAIL", req.recipient(), ok ? "SENT" : "FAILED"));
    }

    @PostMapping("/sms")
    @Operation(summary = "Send an SMS notification")
    public ResponseEntity<NotificationResponse> sendSms(@Valid @RequestBody NotificationRequest req) {
        boolean ok = smsService.send(req.recipient(), req.message(), req.reference());
        return ResponseEntity.ok(new NotificationResponse(ok, "SMS", req.recipient(), ok ? "SENT" : "FAILED"));
    }

    @PostMapping("/whatsapp")
    @Operation(summary = "Send a WhatsApp notification")
    public ResponseEntity<NotificationResponse> sendWhatsApp(@Valid @RequestBody NotificationRequest req) {
        boolean ok = whatsAppService.send(req.recipient(), req.message(), req.reference());
        return ResponseEntity.ok(new NotificationResponse(ok, "WHATSAPP", req.recipient(), ok ? "SENT" : "FAILED"));
    }

    @PostMapping("/registration-event")
    @Operation(summary = "Dispatch all-channel notifications for a registration lifecycle event (email + SMS + WhatsApp)")
    public ResponseEntity<Map<String, Object>> registrationEvent(@Valid @RequestBody RegistrationEventRequest req) {
        int dispatched = dispatchService.dispatchRegistrationEvent(req);
        return ResponseEntity.ok(Map.of(
                "dispatched", dispatched,
                "eventType", req.eventType(),
                "registrationRef", req.registrationRef()
        ));
    }

    @GetMapping("/logs")
    @Operation(summary = "Query notification audit log. Filter by channel (EMAIL/SMS/WHATSAPP) or reference ID")
    public Page<NotificationLog> getLogs(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        if (channel != null && !channel.isBlank()) {
            return logRepository.findByChannel(channel.toUpperCase(), pageable);
        }
        if (reference != null && !reference.isBlank()) {
            return logRepository.findByReference(reference, pageable);
        }
        if (status != null && !status.isBlank()) {
            return logRepository.findByStatus(status.toUpperCase(), pageable);
        }
        return logRepository.findAll(pageable);
    }

    @GetMapping("/health")
    @Operation(summary = "Service health check")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "notification-service"));
    }
}
