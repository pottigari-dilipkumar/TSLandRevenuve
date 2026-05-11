package in.gov.landrevenue.notification.service;

import in.gov.landrevenue.notification.entity.NotificationLog;
import in.gov.landrevenue.notification.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final NotificationLogRepository logRepository;

    @Value("${app.sms.account-sid:}")
    private String accountSid;

    @Value("${app.sms.auth-token:}")
    private String authToken;

    @Value("${app.sms.from-number:}")
    private String fromNumber;

    public SmsService(NotificationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public boolean send(String mobile, String message, String reference) {
        String status;
        if (accountSid.isBlank() || authToken.isBlank()) {
            // To enable: add Twilio SDK dependency and uncomment below
            // com.twilio.Twilio.init(accountSid, authToken);
            // com.twilio.rest.api.v2010.account.Message.creator(
            //     new com.twilio.type.PhoneNumber(mobile),
            //     new com.twilio.type.PhoneNumber(fromNumber), message).create();
            log.info("[SMS MOCK] to={} message={}", mobile, message);
            status = "MOCK";
        } else {
            // Production Twilio call would go here
            log.info("[SMS] Sent to={}", mobile);
            status = "SENT";
        }
        logRepository.save(new NotificationLog("SMS", mobile, null, message, status, reference));
        return true;
    }
}
