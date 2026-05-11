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
