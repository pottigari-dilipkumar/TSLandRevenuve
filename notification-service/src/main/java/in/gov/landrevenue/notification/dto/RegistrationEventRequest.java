package in.gov.landrevenue.notification.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Dispatches multi-channel notifications for a registration lifecycle event.
 * eventType: DRAFTED | APPROVED | REJECTED | MUTATION_APPLIED | MUTATION_APPROVED
 */
public record RegistrationEventRequest(
    @NotBlank String eventType,
    @NotBlank String registrationRef,
    String buyerEmail,
    String buyerMobile,
    String sellerEmail,
    String sellerMobile,
    String reason              // populated for REJECTED events
) {}
