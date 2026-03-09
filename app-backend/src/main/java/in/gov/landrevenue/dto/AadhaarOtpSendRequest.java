package in.gov.landrevenue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AadhaarOtpSendRequest(
        @NotBlank
        @Pattern(regexp = "^[2-9][0-9]{11}$", message = "Aadhaar must be a valid 12-digit number")
        String aadhaarNumber
) {
}
