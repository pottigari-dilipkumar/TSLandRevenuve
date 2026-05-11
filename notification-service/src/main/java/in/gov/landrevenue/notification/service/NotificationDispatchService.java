package in.gov.landrevenue.notification.service;

import in.gov.landrevenue.notification.dto.RegistrationEventRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final EmailService emailService;
    private final SmsService smsService;
    private final WhatsAppService whatsAppService;

    public NotificationDispatchService(EmailService emailService, SmsService smsService,
                                       WhatsAppService whatsAppService) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.whatsAppService = whatsAppService;
    }

    public int dispatchRegistrationEvent(RegistrationEventRequest req) {
        String[] content = buildContent(req);
        String subject = content[0];
        String emailBody = content[1];
        String smsText = content[2];
        String ref = req.registrationRef();

        int count = 0;
        if (req.buyerEmail() != null && !req.buyerEmail().isBlank()) {
            emailService.send(req.buyerEmail(), subject, emailBody, ref);
            count++;
        }
        if (req.sellerEmail() != null && !req.sellerEmail().isBlank()) {
            emailService.send(req.sellerEmail(), subject, emailBody, ref);
            count++;
        }
        if (req.buyerMobile() != null && !req.buyerMobile().isBlank()) {
            smsService.send(req.buyerMobile(), smsText, ref);
            whatsAppService.send(req.buyerMobile(), emailBody, ref);
            count += 2;
        }
        if (req.sellerMobile() != null && !req.sellerMobile().isBlank()) {
            smsService.send(req.sellerMobile(), smsText, ref);
            whatsAppService.send(req.sellerMobile(), emailBody, ref);
            count += 2;
        }
        log.info("[DISPATCH] eventType={} ref={} dispatched={}", req.eventType(), ref, count);
        return count;
    }

    private String[] buildContent(RegistrationEventRequest req) {
        String ref = req.registrationRef();
        return switch (req.eventType().toUpperCase()) {
            case "DRAFTED" -> new String[]{
                "Registration Draft Submitted – Ref: " + ref,
                "Dear Citizen,\n\nYour land registration draft has been submitted with reference number: " + ref
                    + ".\n\nIt will be reviewed by the Sub-Registrar. You will receive updates at each stage.\n\nLand Revenue System",
                "LRMS: Registration " + ref + " submitted. Awaiting SRO review."
            };
            case "APPROVED" -> new String[]{
                "Registration APPROVED – Ref: " + ref,
                "Dear Citizen,\n\nYour land registration " + ref
                    + " has been APPROVED by the Sub-Registrar.\n\nPlease collect your registration certificate from the SRO office.\n\nLand Revenue System",
                "LRMS: Registration " + ref + " APPROVED. Collect certificate from SRO office."
            };
            case "REJECTED" -> new String[]{
                "Registration REJECTED – Ref: " + ref,
                "Dear Citizen,\n\nYour land registration " + ref + " has been REJECTED.\n\nReason: "
                    + req.reason() + "\n\nPlease contact your SRO office for guidance on resubmission.\n\nLand Revenue System",
                "LRMS: Registration " + ref + " REJECTED. Reason: " + req.reason() + ". Contact SRO office."
            };
            case "MUTATION_APPLIED" -> new String[]{
                "Mutation Application Received – Ref: " + ref,
                "Dear Citizen,\n\nYour mutation application " + ref
                    + " has been received and is under review by the Revenue Officer.\n\nLand Revenue System",
                "LRMS: Mutation " + ref + " received. Under Revenue Officer review."
            };
            case "MUTATION_APPROVED" -> new String[]{
                "Mutation APPROVED – Ref: " + ref,
                "Dear Citizen,\n\nYour mutation application " + ref
                    + " has been APPROVED. Land ownership records have been updated.\n\nLand Revenue System",
                "LRMS: Mutation " + ref + " APPROVED. Ownership records updated."
            };
            default -> new String[]{
                "LRMS Update – Ref: " + ref,
                "Dear Citizen,\n\nStatus update for your request " + ref + ": " + req.eventType()
                    + ".\n\nLand Revenue System",
                "LRMS: Update for " + ref + " — " + req.eventType()
            };
        };
    }
}
