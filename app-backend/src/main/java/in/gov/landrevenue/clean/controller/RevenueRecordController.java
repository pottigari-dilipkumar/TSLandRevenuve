package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.revenue.RevenueRecordRequest;
import in.gov.landrevenue.clean.dto.revenue.RevenueRecordResponse;
import in.gov.landrevenue.clean.service.RevenueRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/revenues")
public class RevenueRecordController {
    private final RevenueRecordService revenueRecordService;

    public RevenueRecordController(RevenueRecordService revenueRecordService) {
        this.revenueRecordService = revenueRecordService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER')")
    public RevenueRecordResponse create(@Valid @RequestBody RevenueRecordRequest request) {
        return revenueRecordService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER','CITIZEN')")
    public Page<RevenueRecordResponse> list(Pageable pageable) {
        return revenueRecordService.list(pageable);
    }
}
