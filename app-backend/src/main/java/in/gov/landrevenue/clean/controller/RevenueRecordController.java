package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.revenue.RevenueRecordRequest;
import in.gov.landrevenue.clean.dto.revenue.RevenueRecordResponse;
import in.gov.landrevenue.clean.service.RevenueRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

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
    public Page<RevenueRecordResponse> list(@RequestParam(required = false) Long landRecordId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                            @RequestParam(required = false) BigDecimal minAmount,
                                            @RequestParam(required = false) BigDecimal maxAmount,
                                            @RequestParam(required = false) String paymentReference,
                                            Pageable pageable) {
        validateDateRange(fromDate, toDate);
        return revenueRecordService.list(landRecordId, fromDate, toDate, minAmount, maxAmount, paymentReference, pageable);
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER')")
    public ResponseEntity<String> exportCsv(@RequestParam(required = false) Long landRecordId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                            @RequestParam(required = false) BigDecimal minAmount,
                                            @RequestParam(required = false) BigDecimal maxAmount,
                                            @RequestParam(required = false) String paymentReference) {
        validateDateRange(fromDate, toDate);
        StringBuilder csv = new StringBuilder("id,landRecordId,amount,paymentDate,paymentReference\n");
        for (RevenueRecordResponse row : revenueRecordService.listForExport(landRecordId, fromDate, toDate, minAmount, maxAmount, paymentReference)) {
            csv.append(row.id()).append(',')
                    .append(row.landRecordId()).append(',')
                    .append(row.amount()).append(',')
                    .append(row.paymentDate()).append(',')
                    .append(csvEscape(row.paymentReference()))
                    .append('\n');
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=revenue-records.csv")
                .body(csv.toString());
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must be on or before toDate");
        }
    }

    private String csvEscape(String value) {
        String safe = Objects.toString(value, "");
        String escaped = safe.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
