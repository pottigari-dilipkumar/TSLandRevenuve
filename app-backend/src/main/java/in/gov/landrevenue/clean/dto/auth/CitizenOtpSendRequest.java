package in.gov.landrevenue.clean.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CitizenOtpSendRequest(
        @NotBlank @Pattern(regexp = "\\d{12}", message = "Aadhaar must be 12 digits") String aadhaarNumber
) {}
