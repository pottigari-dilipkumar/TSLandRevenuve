package in.gov.landrevenue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegistrationCreateRequest(
        @NotBlank String parcelId,
        @NotBlank String sellerName,
        @NotBlank String buyerName,
        @NotBlank String registrationRef,
        @NotBlank String deedHash,
        @NotBlank String verifiedIdentityToken,
        @Pattern(regexp = "^$|^0x[a-fA-F0-9]{40}$", message = "ownerWalletAddress must be a valid hex Ethereum address")
        String ownerWalletAddress
) {
}
