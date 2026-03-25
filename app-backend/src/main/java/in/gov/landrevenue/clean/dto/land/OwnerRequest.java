package in.gov.landrevenue.clean.dto.land;

import jakarta.validation.constraints.NotBlank;

public record OwnerRequest(
        @NotBlank String name,
        @NotBlank String nationalId
) {}
