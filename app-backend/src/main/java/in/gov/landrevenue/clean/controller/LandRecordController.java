package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.land.LandRecordRequest;
import in.gov.landrevenue.clean.dto.land.LandRecordResponse;
import in.gov.landrevenue.clean.service.LandRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lands")
public class LandRecordController {
    private final LandRecordService landRecordService;

    public LandRecordController(LandRecordService landRecordService) {
        this.landRecordService = landRecordService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DATA_ENTRY')")
    public LandRecordResponse create(@Valid @RequestBody LandRecordRequest request) {
        return landRecordService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER','CITIZEN')")
    public Page<LandRecordResponse> list(Pageable pageable) {
        return landRecordService.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER','CITIZEN')")
    public LandRecordResponse getById(@PathVariable Long id) {
        return landRecordService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DATA_ENTRY')")
    public LandRecordResponse update(@PathVariable Long id, @Valid @RequestBody LandRecordRequest request) {
        return landRecordService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        landRecordService.delete(id);
    }
}
