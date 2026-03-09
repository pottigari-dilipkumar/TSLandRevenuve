package in.gov.landrevenue.service.platform;

import in.gov.landrevenue.dto.platform.LandCreateRequest;
import in.gov.landrevenue.model.platform.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlatformService {
    private final GeometryService geometryService;
    private final Map<String, SystemUser> users = new ConcurrentHashMap<>();
    private final Map<String, String> pending2fa = new ConcurrentHashMap<>();
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final Map<String, LandParcel> lands = new ConcurrentHashMap<>();
    private boolean emailNotifications = true;
    private boolean smsNotifications = true;

    public PlatformService(GeometryService geometryService) {
        this.geometryService = geometryService;
        seedUsers();
    }

    private void seedUsers() {
        users.put("admin", new SystemUser("admin", "admin123", Role.ADMIN, "admin@gov.in", "+919999999001"));
        users.put("sro", new SystemUser("sro", "sro123", Role.SRO, "sro@gov.in", "+919999999002"));
        users.put("user", new SystemUser("user", "user123", Role.USER, "user@gov.in", "+919999999003"));
    }

    public Map<String, String> signIn(String username, String password) {
        SystemUser user = users.get(username);
        if (user == null || !user.password().equals(password)) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        pending2fa.put(username, otp);
        return Map.of(
                "message", "2FA OTP sent",
                "demoOtp", otp,
                "email", user.email(),
                "mobile", user.mobile()
        );
    }

    public Map<String, String> verify2fa(String username, String otp) {
        String expected = pending2fa.get(username);
        if (expected == null || !expected.equals(otp)) {
            throw new IllegalArgumentException("Invalid 2FA OTP");
        }
        pending2fa.remove(username);
        String token = UUID.randomUUID().toString();
        sessions.put(token, username);
        return Map.of(
                "sessionToken", token,
                "role", users.get(username).role().name()
        );
    }

    public List<SystemUser> listUsers() {
        return users.values().stream().toList();
    }

    public LandParcel createLand(LandCreateRequest req) {
        if (lands.containsKey(req.landId())) {
            throw new IllegalArgumentException("Land ID already exists");
        }
        for (LandParcel existing : lands.values()) {
            if (geometryService.polygonsOverlap(req.polygon(), existing.polygon())) {
                throw new IllegalArgumentException("Coordinates overlap with existing land: " + existing.landId());
            }
        }

        List<LandEvent> history = new ArrayList<>();
        history.add(new LandEvent(Instant.now(), req.actor(), "LAND_SELECTED", "Land selected and verified"));
        history.add(new LandEvent(Instant.now(), req.actor(), "REGISTRATION_INITIATED", "Registration started by SRO"));

        List<LandDocument> docs = List.of(
                new LandDocument("DOC-SALE-DEED", "SaleDeed.pdf", "Sample sale deed content"),
                new LandDocument("DOC-MUTATION", "MutationOrder.pdf", "Sample mutation order content")
        );

        LandParcel parcel = new LandParcel(req.landId(), req.village(), req.surveyNumber(), req.seller(), req.buyer(), req.polygon(), history, docs);
        lands.put(req.landId(), parcel);
        return parcel;
    }

    public Collection<LandParcel> listLands() {
        return lands.values();
    }

    public LandParcel getLand(String landId) {
        LandParcel parcel = lands.get(landId);
        if (parcel == null) {
            throw new IllegalArgumentException("Land not found");
        }
        return parcel;
    }

    public List<LandEvent> getHistory(String landId) {
        return getLand(landId).history();
    }

    public LandDocument getDocument(String landId, String documentId) {
        return getLand(landId).documents().stream()
                .filter(d -> d.documentId().equals(documentId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }

    public Map<String, Object> configureNotifications(boolean emailEnabled, boolean smsEnabled) {
        this.emailNotifications = emailEnabled;
        this.smsNotifications = smsEnabled;
        return Map.of("emailEnabled", emailNotifications, "smsEnabled", smsNotifications);
    }

    public Map<String, Object> getNotificationsConfig() {
        return Map.of("emailEnabled", emailNotifications, "smsEnabled", smsNotifications);
    }
}
