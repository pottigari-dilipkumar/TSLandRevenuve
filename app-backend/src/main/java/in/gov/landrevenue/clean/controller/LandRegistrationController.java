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

    // ── Staff: create draft ───────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('SRO', 'SRO_ASSISTANT', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse createDraft(@Valid @RequestBody RegistrationDraftRequest request,
                                             Principal principal) {
        Long userId = resolveUserId(principal.getName());
        return registrationService.createDraft(request, userId);
    }

    // ── List / detail ─────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('SRO', 'SRO_ASSISTANT', 'ADMIN', 'REVENUE_OFFICER')")
    public List<RegistrationResponse> listAll(@RequestParam(required = false) String status) {
        if (status != null) {
            return registrationService.getByStatus(RegistrationStatus.valueOf(status.toUpperCase()));
        }
        return registrationService.getAll();
    }

    @GetMapping("/{ref}")
    @PreAuthorize("hasAnyRole('SRO', 'SRO_ASSISTANT', 'ADMIN', 'REVENUE_OFFICER', 'CITIZEN')")
    public RegistrationResponse getByRef(@PathVariable String ref) {
        return registrationService.getByRef(ref);
    }

    @GetMapping("/{ref}/events")
    @PreAuthorize("hasAnyRole('SRO', 'SRO_ASSISTANT', 'ADMIN', 'REVENUE_OFFICER', 'CITIZEN')")
    public List<BlockchainEventResponse> getEvents(@PathVariable String ref) {
        return registrationService.getEvents(ref);
    }

    // ── Staff submit (existing) ───────────────────────────────────────────────

    @PutMapping("/{ref}/submit")
    @PreAuthorize("hasAnyRole('SRO', 'SRO_ASSISTANT', 'ADMIN')")
    public RegistrationResponse submitForApproval(@PathVariable String ref, Principal principal) {
        return registrationService.submitForApproval(ref, principal.getName());
    }

    // ── Citizen: create & manage sale request ────────────────────────────────

    @PostMapping("/sale-request")
    @PreAuthorize("hasRole('CITIZEN')")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse createSaleRequest(@Valid @RequestBody SaleRequestCreateRequest request,
                                                   Principal principal) {
        return registrationService.createSaleRequest(request, principal.getName());
    }

    @PutMapping("/{ref}/seller-submit")
    @PreAuthorize("hasRole('CITIZEN')")
    public RegistrationResponse sellerSubmit(@PathVariable String ref, Principal principal) {
        return registrationService.sellerSubmit(ref, principal.getName());
    }

    @PutMapping("/{ref}/seller-resubmit")
    @PreAuthorize("hasRole('CITIZEN')")
    public RegistrationResponse sellerResubmit(@PathVariable String ref, Principal principal) {
        return registrationService.sellerResubmit(ref, principal.getName());
    }

    // ── Citizen: buyer consent ────────────────────────────────────────────────

    @PutMapping("/{ref}/buyer-approve")
    @PreAuthorize("hasRole('CITIZEN')")
    public RegistrationResponse buyerApprove(@PathVariable String ref, Principal principal) {
        return registrationService.buyerApprove(ref, principal.getName());
    }

    @PutMapping("/{ref}/buyer-reject")
    @PreAuthorize("hasRole('CITIZEN')")
    public RegistrationResponse buyerReject(@PathVariable String ref,
                                             @RequestBody RegistrationApprovalRequest request,
                                             Principal principal) {
        return registrationService.buyerReject(ref, request.reason(), principal.getName());
    }

    // ── SRO Assistant: review ─────────────────────────────────────────────────

    @PutMapping("/{ref}/send-back")
    @PreAuthorize("hasAnyRole('SRO_ASSISTANT', 'ADMIN')")
    public RegistrationResponse sendBack(@PathVariable String ref,
                                          @Valid @RequestBody RevisionNotesRequest request,
                                          Principal principal) {
        return registrationService.sroAssistantSendBack(ref, request.notes(), principal.getName());
    }

    @PutMapping("/{ref}/forward-to-sro")
    @PreAuthorize("hasAnyRole('SRO_ASSISTANT', 'ADMIN')")
    public RegistrationResponse forwardToSro(@PathVariable String ref, Principal principal) {
        return registrationService.sroAssistantForward(ref, principal.getName());
    }

    // ── SRO: final approve / reject ───────────────────────────────────────────

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

    // ── Citizen: my queues ────────────────────────────────────────────────────

    @GetMapping("/my-sales")
    @PreAuthorize("hasRole('CITIZEN')")
    public List<RegistrationResponse> mySales(Principal principal) {
        return registrationService.getMySaleRequests(principal.getName());
    }

    @GetMapping("/pending-my-approval")
    @PreAuthorize("hasRole('CITIZEN')")
    public List<RegistrationResponse> pendingMyApproval(Principal principal) {
        return registrationService.getPendingBuyerApprovals(principal.getName());
    }

    private Long resolveUserId(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
