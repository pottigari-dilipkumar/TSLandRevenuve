package in.gov.landrevenue.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
    @NotBlank String recipient,   // email address or mobile number
    String subject,               // for email channel only
    @NotBlank String message,
    String reference              // optional correlation ID (e.g. REG-2024-001)
) {}
