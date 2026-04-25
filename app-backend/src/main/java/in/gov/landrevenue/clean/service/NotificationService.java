package in.gov.landrevenue.clean.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.notification.from-email:noreply@landrevenue.gov.in}")
    private String fromEmail;

    @Value("${app.notification.from-name:Land Revenue System}")
    private String fromName;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public void sendEmail(String to, String subject, String body) {
        if (mailSender == null || mailHost.isBlank()) {
            log.info("[EMAIL MOCK] To: {} | Subject: {} | Body: {}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public void sendSms(String mobile, String message) {
        // Mock SMS — integrate with SMS gateway (e.g., Twilio, AWS SNS) in production
        log.info("[SMS MOCK] To: {} | Message: {}", mobile, message);
    }

    public void notifyRegistrationDrafted(String buyerEmail, String buyerMobile,
                                          String sellerEmail, String sellerMobile,
                                          String registrationRef) {
        String subject = "Registration Draft Created - Ref: " + registrationRef;
        String body = "Dear Citizen,\n\nA land registration draft has been created with reference number: "
                + registrationRef + ".\n\nYou will be notified once it is approved by the SRO.\n\nLand Revenue System";

        if (buyerEmail != null) sendEmail(buyerEmail, subject, body);
        if (sellerEmail != null) sendEmail(sellerEmail, subject, body);
        if (buyerMobile != null) sendSms(buyerMobile, "Registration draft " + registrationRef + " created. Awaiting SRO approval.");
        if (sellerMobile != null) sendSms(sellerMobile, "Registration draft " + registrationRef + " created. Awaiting SRO approval.");
    }

    public void notifyRegistrationApproved(String buyerEmail, String buyerMobile,
                                            String sellerEmail, String sellerMobile,
                                            String registrationRef) {
        String subject = "Registration APPROVED - Ref: " + registrationRef;
        String body = "Dear Citizen,\n\nYour land registration with reference number: " + registrationRef
                + " has been APPROVED by the Sub-Registrar.\n\nLand Revenue System";

        if (buyerEmail != null) sendEmail(buyerEmail, subject, body);
        if (sellerEmail != null) sendEmail(sellerEmail, subject, body);
        if (buyerMobile != null) sendSms(buyerMobile, "Registration " + registrationRef + " APPROVED by SRO.");
        if (sellerMobile != null) sendSms(sellerMobile, "Registration " + registrationRef + " APPROVED by SRO.");
    }

    public void notifyRegistrationRejected(String buyerEmail, String buyerMobile,
                                            String sellerEmail, String sellerMobile,
                                            String registrationRef, String reason) {
        String subject = "Registration REJECTED - Ref: " + registrationRef;
        String body = "Dear Citizen,\n\nYour land registration with reference number: " + registrationRef
                + " has been REJECTED.\n\nReason: " + reason + "\n\nPlease contact your SRO office.\n\nLand Revenue System";

        if (buyerEmail != null) sendEmail(buyerEmail, subject, body);
        if (sellerEmail != null) sendEmail(sellerEmail, subject, body);
        if (buyerMobile != null) sendSms(buyerMobile, "Registration " + registrationRef + " REJECTED. Reason: " + reason);
        if (sellerMobile != null) sendSms(sellerMobile, "Registration " + registrationRef + " REJECTED. Reason: " + reason);
    }
}
