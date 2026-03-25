package in.gov.landrevenue.clean.dto.dashboard;

public record DashboardStatsResponse(
        long totalLandRecords,
        long pendingMutations,
        String monthlyCollections,
        long disputesOpen
) {}
