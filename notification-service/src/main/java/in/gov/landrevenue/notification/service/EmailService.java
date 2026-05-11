package in.gov.landrevenue.notification.service;

import in.gov.landrevenue.notification.entity.NotificationLog;
import in.gov.landrevenue.notification.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private final NotificationLogRepository logRepository;

    @Value("${app.notification.from-email:noreply@landrevenue.gov.in}")
    private String fromEmail;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public EmailService(NotificationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public boolean send(String to, String subject, String body, String reference) {
        String status;
        if (mailSender == null || mailHost.isBlank()) {
            log.info("[EMAIL MOCK] to={} subject={}", to, subject);
            status = "MOCK";
        } else {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(fromEmail);
                msg.setTo(to);
                msg.setSubject(subject);
                msg.setText(body);
                mailSender.send(msg);
                log.info("[EMAIL] Sent to={}", to);
                status = "SENT";
            } catch (Exception e) {
                log.error("[EMAIL] Failed to={}: {}", to, e.getMessage());
                logRepository.save(new NotificationLog("EMAIL", to, subject, body, "FAILED", reference));
                return false;
            }
        }
        logRepository.save(new NotificationLog("EMAIL", to, subject, body, status, reference));
        return true;
    }
}
