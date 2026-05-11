package in.gov.landrevenue.clean.dto.chat;

import in.gov.landrevenue.clean.entity.ChatMessage;

import java.time.LocalDateTime;

public record ChatResponse(
    Long id,
    String sender,
    String message,
    LocalDateTime createdAt,
    String botMessage     // populated only on POST /message responses
) {
    public static ChatResponse fromMessage(ChatMessage msg) {
        return new ChatResponse(msg.getId(), msg.getSender().name(),
                msg.getMessage(), msg.getCreatedAt(), null);
    }
}
