package in.gov.landrevenue.clean.dto.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegistrationPartyDto(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "\\d{12}", message = "Aadhaar must be 12 digits") String aadhaarNumber,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "Mobile must be 10 digits") String mobile,
        String email,
        @NotBlank String address
) {}
