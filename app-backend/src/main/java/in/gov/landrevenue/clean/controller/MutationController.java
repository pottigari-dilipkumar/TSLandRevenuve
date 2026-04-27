package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.mutation.MutationDecisionRequest;
import in.gov.landrevenue.clean.dto.mutation.MutationRequest;
import in.gov.landrevenue.clean.dto.mutation.MutationResponse;
import in.gov.landrevenue.clean.enums.MutationStatus;
import in.gov.landrevenue.clean.service.MutationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import in.gov.landrevenue.clean.repository.UserRepository;

@RestController
@RequestMapping("/api/mutations")
public class MutationController {

    private final MutationService mutationService;
    private final UserRepository userRepository;

    public MutationController(MutationService mutationService, UserRepository userRepository) {
        this.mutationService = mutationService;
        this.userRepository = userRepository;
    }

    /** CITIZEN / DATA_ENTRY / ADMIN — apply for mutation */
    @PostMapping
    @PreAuthorize("hasAnyRole('CITIZEN','DATA_ENTRY','ADMIN')")
    public ResponseEntity<MutationResponse> apply(
            @Valid @RequestBody MutationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(mutationService.apply(request, userId));
    }

    /** List all (staff) with optional status filter */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER','DATA_ENTRY','SRO','SRO_ASSISTANT')")
    public ResponseEntity<Page<MutationResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(mutationService.listByStatus(MutationStatus.valueOf(status.toUpperCase()), pr));
        }
        return ResponseEntity.ok(mutationService.list(pr));
    }

    /** List mutations for a specific land record */
    @GetMapping("/land/{landRecordId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MutationResponse>> listByLand(
            @PathVariable Long landRecordId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        return ResponseEntity.ok(mutationService.listByLand(landRecordId, pr));
    }

    /** Get single mutation */
    @GetMapping("/{ref}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MutationResponse> get(@PathVariable String ref) {
        return ResponseEntity.ok(mutationService.getByRef(ref));
    }

    /** Revenue Officer / Mandal Tahsildar — send to review */
    @PostMapping("/{ref}/review")
    @PreAuthorize("hasAnyRole('REVENUE_OFFICER','ADMIN')")
    public ResponseEntity<MutationResponse> sendToReview(@PathVariable String ref) {
        return ResponseEntity.ok(mutationService.sendToReview(ref));
    }

    /** Revenue Officer — approve */
    @PostMapping("/{ref}/approve")
    @PreAuthorize("hasAnyRole('REVENUE_OFFICER','ADMIN')")
    public ResponseEntity<MutationResponse> approve(
            @PathVariable String ref,
            @RequestBody(required = false) MutationDecisionRequest decision,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(mutationService.approve(ref, decision, userId));
    }

    /** Revenue Officer — reject */
    @PostMapping("/{ref}/reject")
    @PreAuthorize("hasAnyRole('REVENUE_OFFICER','ADMIN')")
    public ResponseEntity<MutationResponse> reject(
            @PathVariable String ref,
            @RequestBody(required = false) MutationDecisionRequest decision,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(mutationService.reject(ref, decision, userId));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}
