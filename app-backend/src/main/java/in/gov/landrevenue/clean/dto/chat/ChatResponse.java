/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

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
