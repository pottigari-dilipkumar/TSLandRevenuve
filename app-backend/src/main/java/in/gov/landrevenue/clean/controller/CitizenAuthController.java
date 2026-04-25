package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.auth.*;
import in.gov.landrevenue.clean.repository.UserRepository;
import in.gov.landrevenue.clean.service.CitizenAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/citizen/auth")
public class CitizenAuthController {

    private final CitizenAuthService citizenAuthService;
    private final UserRepository userRepository;

    public CitizenAuthController(CitizenAuthService citizenAuthService, UserRepository userRepository) {
        this.citizenAuthService = citizenAuthService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody CitizenOtpSendRequest request) {
        String demoOtp = citizenAuthService.sendOtp(request.aadhaarNumber());
        return ResponseEntity.ok(Map.of(
                "message", "OTP sent to registered mobile (demo mode)",
                "demoOtp", demoOtp
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<CitizenAuthResponse> verifyOtp(@Valid @RequestBody CitizenOtpVerifyRequest request) {
        CitizenAuthResponse response = citizenAuthService.verifyOtpAndLogin(
                request.aadhaarNumber(), request.otp());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<CitizenAuthResponse> updateProfile(
            @Valid @RequestBody CitizenProfileRequest request,
            Principal principal) {
        String aadhaarNumber = resolveAadhaar(principal.getName());
        CitizenAuthResponse response = citizenAuthService.updateProfile(aadhaarNumber, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<CitizenAuthResponse> getProfile(Principal principal) {
        String aadhaarNumber = resolveAadhaar(principal.getName());
        return ResponseEntity.ok(citizenAuthService.getProfile(aadhaarNumber));
    }

    private String resolveAadhaar(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getAadhaarNumber())
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found"));
    }
}
