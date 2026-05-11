package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop50ByUsernameOrderByCreatedAtAsc(String username);

    @Modifying
    @Query("DELETE FROM ChatMessage c WHERE c.username = :username")
    void deleteByUsername(String username);
}
