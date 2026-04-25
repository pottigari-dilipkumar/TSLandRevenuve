package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.citizen.CitizenLandResponse;
import in.gov.landrevenue.clean.dto.registration.RegistrationResponse;
import in.gov.landrevenue.clean.repository.UserRepository;
import in.gov.landrevenue.clean.service.CitizenDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/citizen")
@PreAuthorize("hasRole('CITIZEN')")
public class CitizenDashboardController {

    private final CitizenDashboardService citizenDashboardService;
    private final UserRepository userRepository;

    public CitizenDashboardController(CitizenDashboardService citizenDashboardService,
                                       UserRepository userRepository) {
        this.citizenDashboardService = citizenDashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/lands")
    public ResponseEntity<List<CitizenLandResponse>> getMyLands(Principal principal) {
        String aadhaar = resolveAadhaar(principal.getName());
        return ResponseEntity.ok(citizenDashboardService.getMyLands(aadhaar));
    }

    @GetMapping("/registrations")
    public ResponseEntity<List<RegistrationResponse>> getMyRegistrations(Principal principal) {
        String aadhaar = resolveAadhaar(principal.getName());
        return ResponseEntity.ok(citizenDashboardService.getMyRegistrations(aadhaar));
    }

    private String resolveAadhaar(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getAadhaarNumber())
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found"));
    }
}
