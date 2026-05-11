package in.gov.landrevenue.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpVerifyRequest(
    @NotBlank String identifier,
    @NotBlank String purpose,
    @NotBlank @Size(min = 6, max = 6) String otp
) {}
