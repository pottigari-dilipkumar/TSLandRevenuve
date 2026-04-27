package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.land.LandHistoryEntry;
import in.gov.landrevenue.clean.dto.land.LandRecordRequest;
import in.gov.landrevenue.clean.dto.land.LandRecordResponse;
import in.gov.landrevenue.clean.service.LandRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER','DATA_ENTRY','CITIZEN')")
    public Page<LandRecordResponse> list(@RequestParam(required = false) String surveyNumber,
                                         @RequestParam(required = false) String district,
                                         @RequestParam(required = false) String village,
                                         @RequestParam(required = false) String ownerName,
                                         Pageable pageable) {
        return landRecordService.list(surveyNumber, district, village, ownerName, pageable);
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER','DATA_ENTRY')")
    public ResponseEntity<String> exportCsv(@RequestParam(required = false) String surveyNumber,
                                            @RequestParam(required = false) String district,
                                            @RequestParam(required = false) String village,
                                            @RequestParam(required = false) String ownerName) {
        StringBuilder csv = new StringBuilder("id,surveyNumber,district,village,areaInAcres,ownerId,ownerName\n");
        for (LandRecordResponse row : landRecordService.listForExport(surveyNumber, district, village, ownerName)) {
            csv.append(row.id()).append(',')
                    .append(csvEscape(row.surveyNumber())).append(',')
                    .append(csvEscape(row.district())).append(',')
                    .append(csvEscape(row.village())).append(',')
                    .append(row.areaInAcres()).append(',')
                    .append(row.ownerId()).append(',')
                    .append(csvEscape(row.ownerName()))
                    .append('\n');
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=land-records.csv")
                .body(csv.toString());
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

    /**
     * Ownership & transaction history for a land parcel —
     * approved registrations + all mutations, ordered chronologically.
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LandHistoryEntry>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(landRecordService.getHistory(id));
    }

    private String csvEscape(String value) {
        String safe = Objects.toString(value, "");
        String escaped = safe.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
