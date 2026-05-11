package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.chat.ChatResponse;
import in.gov.landrevenue.clean.entity.ChatMessage;
import in.gov.landrevenue.clean.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository repository;

    @InjectMocks
    private ChatService chatService;

    private ChatMessage botMessage(String text) {
        ChatMessage m = new ChatMessage("user1", ChatMessage.SenderType.BOT, text);
        setId(m, 42L);
        return m;
    }

    private void setId(ChatMessage m, Long id) {
        try {
            var f = ChatMessage.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(m, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void processMessage_savesUserAndBotMessages() {
        ChatMessage saved = botMessage("Hello! Welcome to LRMS Support.");
        when(repository.save(any())).thenReturn(saved);

        ChatResponse resp = chatService.processMessage("user1", "hello");

        assertThat(resp.sender()).isEqualTo("BOT");
        assertThat(resp.botMessage()).isNotBlank();
        verify(repository, times(2)).save(any(ChatMessage.class));
    }

    @Test
    void processMessage_userSavedWithCorrectUsername() {
        ChatMessage saved = botMessage("Some reply");
        when(repository.save(any())).thenReturn(saved);
        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);

        chatService.processMessage("alice", "hi");

        verify(repository, atLeast(1)).save(captor.capture());
        ChatMessage first = captor.getAllValues().get(0);
        assertThat(first.getUsername()).isEqualTo("alice");
        assertThat(first.getSender()).isEqualTo(ChatMessage.SenderType.USER);
        assertThat(first.getMessage()).isEqualTo("hi");
    }

    @Test
    void getHistory_returnsMappedResponses() {
        ChatMessage m1 = new ChatMessage("user1", ChatMessage.SenderType.USER, "hello");
        ChatMessage m2 = new ChatMessage("user1", ChatMessage.SenderType.BOT, "Hi there!");
        when(repository.findTop50ByUsernameOrderByCreatedAtAsc("user1")).thenReturn(List.of(m1, m2));

        List<ChatResponse> history = chatService.getHistory("user1");

        assertThat(history).hasSize(2);
        assertThat(history.get(0).sender()).isEqualTo("USER");
        assertThat(history.get(1).sender()).isEqualTo("BOT");
    }

    @Test
    void clearHistory_invokesDeleteByUsername() {
        chatService.clearHistory("alice");
        verify(repository).deleteByUsername("alice");
    }

    @Test
    void processMessage_documentKeywordReturnsDocumentList() {
        ChatMessage saved = botMessage("docs");
        when(repository.save(any())).thenReturn(saved);

        ChatResponse resp = chatService.processMessage("user1", "What documents are required?");
        assertThat(resp.botMessage()).isNotBlank();
    }

    @Test
    void processMessage_feeKeywordReturnsFeeInfo() {
        ChatMessage saved = botMessage("fees");
        when(repository.save(any())).thenReturn(saved);

        ChatResponse resp = chatService.processMessage("user1", "What are the stamp duty fees?");
        assertThat(resp.botMessage()).isNotBlank();
    }

    @Test
    void processMessage_unknownInputReturnsFallback() {
        ChatMessage saved = botMessage("fallback");
        when(repository.save(any())).thenReturn(saved);

        ChatResponse resp = chatService.processMessage("user1", "xyzzyabcde12345random");
        assertThat(resp.botMessage()).isNotBlank();
    }
}
