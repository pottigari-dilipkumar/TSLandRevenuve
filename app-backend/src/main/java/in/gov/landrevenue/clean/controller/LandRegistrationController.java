package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.registration.*;
import in.gov.landrevenue.clean.enums.RegistrationStatus;
import in.gov.landrevenue.clean.repository.UserRepository;
import in.gov.landrevenue.clean.service.LandRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class LandRegistrationController {

    private final LandRegistrationService registrationService;
    private final UserRepository userRepository;

    public LandRegistrationController(LandRegistrationService registrationService,
                                       UserRepository userRepository) {
        this.registrationService = registrationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SRO', 'SRO_ASSISTANT', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse createDraft(@Valid @RequestBody RegistrationDraftRequest request,
                                             Principal principal) {
        Long userId = resolveUserId(principal.getName());
        return registrationService.createDraft(request, userId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SRO', 'SRO_ASSISTANT', 'ADMIN', 'REVENUE_OFFICER')")
    public List<RegistrationResponse> listAll(@RequestParam(required = false) String status) {
        if (status != null) {
            return registrationService.getByStatus(RegistrationStatus.valueOf(status.toUpperCase()));
        }
        return registrationService.getAll();
    }

    @GetMapping("/{ref}")
    @PreAuthorize("hasAnyRole('SRO', 'SRO_ASSISTANT', 'ADMIN', 'REVENUE_OFFICER')")
    public RegistrationResponse getByRef(@PathVariable String ref) {
        return registrationService.getByRef(ref);
    }

    @PutMapping("/{ref}/submit")
    @PreAuthorize("hasAnyRole('SRO', 'SRO_ASSISTANT', 'ADMIN')")
    public RegistrationResponse submitForApproval(@PathVariable String ref, Principal principal) {
        return registrationService.submitForApproval(ref, principal.getName());
    }

    @GetMapping("/{ref}/events")
    @PreAuthorize("hasAnyRole('SRO', 'SRO_ASSISTANT', 'ADMIN', 'REVENUE_OFFICER', 'CITIZEN')")
    public List<BlockchainEventResponse> getEvents(@PathVariable String ref) {
        return registrationService.getEvents(ref);
    }

    @PutMapping("/{ref}/approve")
    @PreAuthorize("hasAnyRole('SRO', 'ADMIN')")
    public RegistrationResponse approve(@PathVariable String ref, Principal principal) {
        Long userId = resolveUserId(principal.getName());
        return registrationService.approve(ref, userId);
    }

    @PutMapping("/{ref}/reject")
    @PreAuthorize("hasAnyRole('SRO', 'ADMIN')")
    public RegistrationResponse reject(@PathVariable String ref,
                                        @RequestBody RegistrationApprovalRequest request,
                                        Principal principal) {
        Long userId = resolveUserId(principal.getName());
        return registrationService.reject(ref, request.reason(), userId);
    }

    private Long resolveUserId(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
