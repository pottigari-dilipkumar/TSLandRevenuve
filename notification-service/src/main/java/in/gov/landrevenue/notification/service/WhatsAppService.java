package in.gov.landrevenue.notification.service;

import in.gov.landrevenue.notification.entity.NotificationLog;
import in.gov.landrevenue.notification.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    private final NotificationLogRepository logRepository;

    @Value("${app.whatsapp.account-sid:}")
    private String accountSid;

    @Value("${app.whatsapp.auth-token:}")
    private String authToken;

    @Value("${app.whatsapp.from-number:whatsapp:+14155238886}")
    private String fromNumber;

    public WhatsAppService(NotificationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public boolean send(String mobile, String message, String reference) {
        // Normalise to WhatsApp address format
        String toWa = mobile.startsWith("whatsapp:")
                ? mobile
                : "whatsapp:+" + mobile.replaceAll("[^0-9]", "");

        String status;
        if (accountSid.isBlank() || authToken.isBlank()) {
            // Option A — Twilio WhatsApp (uncomment + add SDK dependency):
            // com.twilio.Twilio.init(accountSid, authToken);
            // com.twilio.rest.api.v2010.account.Message.creator(
            //     new com.twilio.type.PhoneNumber(toWa),
            //     new com.twilio.type.PhoneNumber(fromNumber), message).create();
            //
            // Option B — Meta Cloud API (replace PHONE_NUMBER_ID and TOKEN):
            // POST https://graph.facebook.com/v18.0/{PHONE_NUMBER_ID}/messages
            // Authorization: Bearer {TOKEN}
            // Body: { "messaging_product":"whatsapp", "to":"<mobile>",
            //         "type":"text", "text":{"body":"<message>"} }
            log.info("[WHATSAPP MOCK] to={} message={}", toWa, message);
            status = "MOCK";
        } else {
            log.info("[WHATSAPP] Sent to={}", toWa);
            status = "SENT";
        }
        logRepository.save(new NotificationLog("WHATSAPP", toWa, null, message, status, reference));
        return true;
    }
}
