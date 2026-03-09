package in.gov.landrevenue.controller.platform;

import in.gov.landrevenue.dto.platform.LandCreateRequest;
import in.gov.landrevenue.dto.platform.NotificationConfigRequest;
import in.gov.landrevenue.dto.platform.SignInRequest;
import in.gov.landrevenue.dto.platform.Verify2FaRequest;
import in.gov.landrevenue.model.platform.LandDocument;
import in.gov.landrevenue.model.platform.LandEvent;
import in.gov.landrevenue.model.platform.LandParcel;
import in.gov.landrevenue.model.platform.SystemUser;
import in.gov.landrevenue.service.platform.PlatformService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform")
public class PlatformController {
    private final PlatformService platformService;

    public PlatformController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @PostMapping("/auth/sign-in")
    public Map<String, String> signIn(@Valid @RequestBody SignInRequest request) {
        return platformService.signIn(request.username(), request.password());
    }

    @PostMapping("/auth/verify-2fa")
    public Map<String, String> verify2fa(@Valid @RequestBody Verify2FaRequest request) {
        return platformService.verify2fa(request.username(), request.otp());
    }

    @GetMapping("/users")
    public List<SystemUser> users() {
        return platformService.listUsers();
    }

    @PostMapping("/lands")
    public LandParcel createLand(@Valid @RequestBody LandCreateRequest request) {
        return platformService.createLand(request);
    }

    @GetMapping("/lands")
    public Collection<LandParcel> listLands() {
        return platformService.listLands();
    }

    @GetMapping("/lands/{landId}")
    public LandParcel getLand(@PathVariable String landId) {
        return platformService.getLand(landId);
    }

    @GetMapping("/lands/{landId}/history")
    public List<LandEvent> getHistory(@PathVariable String landId) {
        return platformService.getHistory(landId);
    }

    @GetMapping("/lands/{landId}/documents/{documentId}/download")
    public ResponseEntity<byte[]> download(@PathVariable String landId, @PathVariable String documentId) {
        LandDocument doc = platformService.getDocument(landId, documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.name() + "\"")
                .body(doc.content().getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/notifications/config")
    public Map<String, Object> setNotificationConfig(@RequestBody NotificationConfigRequest request) {
        return platformService.configureNotifications(request.emailEnabled(), request.smsEnabled());
    }

    @GetMapping("/notifications/config")
    public Map<String, Object> getNotificationConfig() {
        return platformService.getNotificationsConfig();
    }
}
