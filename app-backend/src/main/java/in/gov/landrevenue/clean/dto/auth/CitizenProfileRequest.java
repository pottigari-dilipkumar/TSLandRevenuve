package in.gov.landrevenue.clean.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CitizenProfileRequest(
        @NotBlank String fullName,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "Mobile must be 10 digits") String mobile,
        String email,
        @NotBlank String address
) {}
