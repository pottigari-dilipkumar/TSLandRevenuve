package in.gov.landrevenue.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AadhaarAuthService {
    private static final int OTP_TTL_SECONDS = 300;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();
    private final Map<String, Instant> verifiedTokens = new ConcurrentHashMap<>();

    public String sendOtp(String aadhaarNumber) {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpStore.put(aadhaarNumber, new OtpRecord(otp, Instant.now().plusSeconds(OTP_TTL_SECONDS)));
        // NOTE: For demo purposes only. In production, OTP must be delivered via an approved gateway.
        return otp;
    }

    public String verifyOtp(String aadhaarNumber, String otp) {
        OtpRecord record = otpStore.get(aadhaarNumber);
        if (record == null || Instant.now().isAfter(record.expiresAt()) || !record.otp().equals(otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        otpStore.remove(aadhaarNumber);
        String token = UUID.randomUUID().toString();
        verifiedTokens.put(token, Instant.now().plusSeconds(1800));
        return token;
    }

    public boolean isVerifiedTokenValid(String token) {
        Instant expiresAt = verifiedTokens.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (Instant.now().isAfter(expiresAt)) {
            verifiedTokens.remove(token);
            return false;
        }
        return true;
    }

    private record OtpRecord(String otp, Instant expiresAt) {
    }
}
