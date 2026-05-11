package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.chat.ChatRequest;
import in.gov.landrevenue.clean.dto.chat.ChatResponse;
import in.gov.landrevenue.clean.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/chat")
public class PublicChatController {

    private final ChatService chatService;

    public PublicChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> message(@Valid @RequestBody ChatRequest req) {
        return ResponseEntity.ok(chatService.publicResponse(req.message()));
    }
}
