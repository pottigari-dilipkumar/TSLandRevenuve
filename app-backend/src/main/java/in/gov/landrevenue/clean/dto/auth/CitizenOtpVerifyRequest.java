package in.gov.landrevenue.clean.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CitizenOtpVerifyRequest(
        @NotBlank @Pattern(regexp = "\\d{12}", message = "Aadhaar must be 12 digits") String aadhaarNumber,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits") String otp
) {}
