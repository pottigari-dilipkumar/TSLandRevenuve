package in.gov.landrevenue.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrationCreateRequest(
        @NotBlank String parcelId,
        @NotBlank String sellerName,
        @NotBlank String buyerName,
        @NotBlank String registrationRef,
        @NotBlank String deedHash,
        @NotBlank String verifiedIdentityToken
) {
}
