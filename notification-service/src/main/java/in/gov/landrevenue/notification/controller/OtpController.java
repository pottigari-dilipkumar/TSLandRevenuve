package in.gov.landrevenue.notification.controller;

import in.gov.landrevenue.notification.dto.OtpSendRequest;
import in.gov.landrevenue.notification.dto.OtpVerifyRequest;
import in.gov.landrevenue.notification.dto.OtpVerifyResponse;
import in.gov.landrevenue.notification.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notify/otp")
@Tag(name = "OTP", description = "OTP generation, delivery, and verification")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    @Operation(summary = "Generate and send a 6-digit OTP to the given identifier via SMS, Email, or WhatsApp")
    public ResponseEntity<Map<String, Object>> send(@Valid @RequestBody OtpSendRequest req) {
        otpService.generateAndSend(req.identifier(), req.purpose(), req.channel());
        String id = req.identifier();
        String masked = id.length() > 4
                ? id.substring(0, 2) + "****" + id.substring(id.length() - 2)
                : "****";
        return ResponseEntity.ok(Map.of(
                "success", true,
                "maskedIdentifier", masked,
                "expiresInMinutes", 10
        ));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify an OTP. Returns valid=true and deletes the OTP on success (single-use)")
    public ResponseEntity<OtpVerifyResponse> verify(@Valid @RequestBody OtpVerifyRequest req) {
        boolean valid = otpService.verify(req.identifier(), req.purpose(), req.otp());
        return ResponseEntity.ok(new OtpVerifyResponse(
                valid,
                valid ? "OTP verified successfully." : "Invalid or expired OTP."
        ));
    }
}
