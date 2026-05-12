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

import in.gov.landrevenue.clean.dto.dashboard.DashboardStatsResponse;
import in.gov.landrevenue.clean.dto.dashboard.RevenueSummaryResponse;
import in.gov.landrevenue.clean.dto.dashboard.RevenueTrendResponse;
import in.gov.landrevenue.clean.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER','DATA_ENTRY','CITIZEN')")
    public DashboardStatsResponse getStats() {
        return dashboardService.stats();
    }

    @GetMapping("/revenue/trend")
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER','DATA_ENTRY','CITIZEN')")
    public List<RevenueTrendResponse> getTrend() {
        return dashboardService.revenueTrend();
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN','REVENUE_OFFICER','DATA_ENTRY','CITIZEN')")
    public List<RevenueSummaryResponse> getSummary() {
        return dashboardService.revenueSummaryByDistrict();
    }
}
