package in.gov.landrevenue.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redis;
    private final SmsService smsService;
    private final WhatsAppService whatsAppService;
    private final EmailService emailService;

    @Value("${app.notification.otp-ttl-minutes:10}")
    private int otpTtlMinutes;

    public OtpService(StringRedisTemplate redis, SmsService smsService,
                      WhatsAppService whatsAppService, EmailService emailService) {
        this.redis = redis;
        this.smsService = smsService;
        this.whatsAppService = whatsAppService;
        this.emailService = emailService;
    }

    public void generateAndSend(String identifier, String purpose, String channel) {
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        redis.opsForValue().set(redisKey(purpose, identifier), otp, Duration.ofMinutes(otpTtlMinutes));

        String message = "Your LRMS OTP for " + purpose + " is: " + otp
                + ". Valid for " + otpTtlMinutes + " minutes. Do NOT share this code.";

        String ch = (channel == null || channel.isBlank()) ? "SMS" : channel.toUpperCase();
        switch (ch) {
            case "EMAIL"    -> emailService.send(identifier, "LRMS OTP – " + purpose, message, null);
            case "WHATSAPP" -> whatsAppService.send(identifier, message, null);
            default         -> smsService.send(identifier, message, null);
        }
        log.info("[OTP] Sent for identifier={} purpose={} channel={}", mask(identifier), purpose, ch);
    }

    public boolean verify(String identifier, String purpose, String otp) {
        String stored = redis.opsForValue().get(redisKey(purpose, identifier));
        if (stored != null && stored.equals(otp)) {
            redis.delete(redisKey(purpose, identifier));
            return true;
        }
        return false;
    }

    private String redisKey(String purpose, String identifier) {
        return "otp:" + purpose.toUpperCase() + ":" + identifier;
    }

    private String mask(String s) {
        if (s == null || s.length() < 4) return "****";
        return s.substring(0, 2) + "****" + s.substring(s.length() - 2);
    }
}
