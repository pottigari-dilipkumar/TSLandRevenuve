/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.entity.LegalCase;
import in.gov.landrevenue.clean.repository.LegalCaseRepository;
import in.gov.landrevenue.clean.service.LandMapService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/map")
public class LandMapController {

    private final LandMapService landMapService;
    private final LegalCaseRepository legalCaseRepository;

    public LandMapController(LandMapService landMapService, LegalCaseRepository legalCaseRepository) {
        this.landMapService = landMapService;
        this.legalCaseRepository = legalCaseRepository;
    }

    /** GeoJSON FeatureCollection — all land parcels that have boundary geometry. */
    @GetMapping("/parcels")
    public ResponseEntity<Map<String, Object>> getParcels() {
        return ResponseEntity.ok(landMapService.getFeatureCollection());
    }

    /** Point-in-polygon search: which land parcel contains the given lat/lng? */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam double lat,
            @RequestParam double lng) {
        Optional<Long> found = landMapService.findByCoordinates(lat, lng);
        return ResponseEntity.ok(Map.of(
                "found", found.isPresent(),
                "landId", found.orElse(-1L),
                "lat", lat,
                "lng", lng
        ));
    }

    /** Full ownership and registration history for a land parcel. */
    @GetMapping("/{id}/history")
    public ResponseEntity<Map<String, Object>> history(@PathVariable Long id) {
        return ResponseEntity.ok(landMapService.getLandHistory(id));
    }

    /** Create a legal case record on a land parcel (admin / revenue officer). */
    @PostMapping("/{id}/legal-cases")
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER')")
    public ResponseEntity<LegalCase> addLegalCase(
            @PathVariable Long id,
            @RequestBody LegalCaseRequest req) {
        LegalCase lc = new LegalCase();
        lc.setLandRecordId(id);
        lc.setCaseNumber(req.caseNumber());
        lc.setCourt(req.court());
        lc.setCaseType(req.caseType());
        lc.setStatus(req.status() != null ? req.status() : "PENDING");
        lc.setFiledBy(req.filedBy());
        lc.setFiledDate(req.filedDate());
        lc.setDescription(req.description());
        return ResponseEntity.ok(legalCaseRepository.save(lc));
    }

    record LegalCaseRequest(
        @NotBlank String caseNumber,
        String court,
        String caseType,
        String status,
        String filedBy,
        LocalDate filedDate,
        String description
    ) {}
}
