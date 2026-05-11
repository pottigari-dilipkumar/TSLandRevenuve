package in.gov.landrevenue.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpSendRequest(
    @NotBlank String identifier,   // mobile number or email address
    @NotBlank String purpose,      // LOGIN, REGISTRATION, VERIFICATION, MUTATION
    String channel                 // SMS (default), EMAIL, WHATSAPP
) {}
