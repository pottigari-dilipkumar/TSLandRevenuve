package in.gov.landrevenue.controller;

import in.gov.landrevenue.dto.AadhaarOtpSendRequest;
import in.gov.landrevenue.dto.AadhaarOtpVerifyRequest;
import in.gov.landrevenue.service.AadhaarAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/aadhaar")
public class AuthController {
    private final AadhaarAuthService aadhaarAuthService;

    public AuthController(AadhaarAuthService aadhaarAuthService) {
        this.aadhaarAuthService = aadhaarAuthService;
    }

    @PostMapping("/send-otp")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> sendOtp(@Valid @RequestBody AadhaarOtpSendRequest request) {
        String otp = aadhaarAuthService.sendOtp(request.aadhaarNumber());
        return Map.of(
                "message", "OTP sent successfully (demo mode)",
                "demoOtp", otp
        );
    }

    @PostMapping("/verify-otp")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> verifyOtp(@Valid @RequestBody AadhaarOtpVerifyRequest request) {
        String token = aadhaarAuthService.verifyOtp(request.aadhaarNumber(), request.otp());
        return Map.of(
                "message", "Identity verified",
                "verifiedIdentityToken", token
        );
    }
}
