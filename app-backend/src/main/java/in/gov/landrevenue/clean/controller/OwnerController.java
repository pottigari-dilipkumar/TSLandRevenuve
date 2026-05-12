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

import in.gov.landrevenue.clean.dto.land.OwnerRequest;
import in.gov.landrevenue.clean.dto.land.OwnerResponse;
import in.gov.landrevenue.clean.service.OwnerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {
    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DATA_ENTRY')")
    public OwnerResponse create(@Valid @RequestBody OwnerRequest request) {
        return ownerService.createOwner(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER','DATA_ENTRY')")
    public Page<OwnerResponse> list(Pageable pageable) {
        return ownerService.listOwners(pageable);
    }
}
