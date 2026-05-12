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

import in.gov.landrevenue.clean.dto.citizen.MarketValueResponse;
import in.gov.landrevenue.clean.service.MarketValueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market-values")
public class MarketValueController {

    private final MarketValueService marketValueService;

    public MarketValueController(MarketValueService marketValueService) {
        this.marketValueService = marketValueService;
    }

    @GetMapping
    public List<MarketValueResponse> getAll(@RequestParam(required = false) String district) {
        if (district != null && !district.isBlank()) {
            return marketValueService.getRatesByDistrict(district);
        }
        return marketValueService.getAllCurrentRates();
    }

    @GetMapping("/lookup")
    public ResponseEntity<MarketValueResponse> lookup(@RequestParam String district,
                                                       @RequestParam String village) {
        return marketValueService.getCurrentRate(district, village)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
