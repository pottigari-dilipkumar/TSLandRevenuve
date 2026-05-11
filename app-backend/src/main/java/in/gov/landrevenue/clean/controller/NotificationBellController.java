package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.service.NotificationBellService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationBellController {

    private final NotificationBellService bellService;
    private final in.gov.landrevenue.clean.repository.UserRepository userRepository;

    public NotificationBellController(NotificationBellService bellService,
                                       in.gov.landrevenue.clean.repository.UserRepository userRepository) {
        this.bellService = bellService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getNotifications(Principal principal) {
        return bellService.getNotifications(resolveUserId(principal.getName()));
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Principal principal) {
        long count = bellService.getUnreadCount(resolveUserId(principal.getName()));
        return Map.of("count", count);
    }

    @PutMapping("/{id}/read")
    public void markRead(@PathVariable Long id, Principal principal) {
        bellService.markRead(id, resolveUserId(principal.getName()));
    }

    @PutMapping("/read-all")
    public void markAllRead(Principal principal) {
        bellService.markAllRead(resolveUserId(principal.getName()));
    }

    private Long resolveUserId(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
}
