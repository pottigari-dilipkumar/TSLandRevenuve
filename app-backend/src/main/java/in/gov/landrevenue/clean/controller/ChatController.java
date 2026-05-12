/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.chat.ChatRequest;
import in.gov.landrevenue.clean.dto.chat.ChatResponse;
import in.gov.landrevenue.clean.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/message")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> message(@Valid @RequestBody ChatRequest req, Principal principal) {
        return ResponseEntity.ok(chatService.processMessage(principal.getName(), req.message()));
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatResponse>> history(Principal principal) {
        return ResponseEntity.ok(chatService.getHistory(principal.getName()));
    }

    @DeleteMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> clearHistory(Principal principal) {
        chatService.clearHistory(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
