package in.gov.landrevenue.notification.dto;

public record NotificationResponse(
    boolean success,
    String channel,
    String recipient,
    String status
) {}
