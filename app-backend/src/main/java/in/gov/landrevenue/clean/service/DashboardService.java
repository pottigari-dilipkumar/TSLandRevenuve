package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.dashboard.DashboardStatsResponse;
import in.gov.landrevenue.clean.dto.dashboard.RevenueSummaryResponse;
import in.gov.landrevenue.clean.dto.dashboard.RevenueTrendResponse;
import in.gov.landrevenue.clean.entity.RevenueRecord;
import in.gov.landrevenue.clean.repository.LandRecordRepository;
import in.gov.landrevenue.clean.repository.RevenueRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class DashboardService {
    private final LandRecordRepository landRecordRepository;
    private final RevenueRecordRepository revenueRecordRepository;

    public DashboardService(LandRecordRepository landRecordRepository, RevenueRecordRepository revenueRecordRepository) {
        this.landRecordRepository = landRecordRepository;
        this.revenueRecordRepository = revenueRecordRepository;
    }

    public DashboardStatsResponse stats() {
        long total = landRecordRepository.count();
        long pendingMutations = Math.round(total * 0.07);
        BigDecimal monthlyTotal = revenueRecordRepository.findAll().stream()
                .filter(r -> r.getPaymentDate().getYear() == LocalDate.now().getYear() && r.getPaymentDate().getMonth() == LocalDate.now().getMonth())
                .map(RevenueRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String monthlyCollections = "₹ " + monthlyTotal;
        long disputesOpen = Math.round(total * 0.02);
        return new DashboardStatsResponse(total, pendingMutations, monthlyCollections, disputesOpen);
    }

    public List<RevenueTrendResponse> revenueTrend() {
        Map<String, BigDecimal> monthly = new LinkedHashMap<>();
        LocalDate current = LocalDate.now().withDayOfMonth(1).minusMonths(5);
        for (int i = 0; i < 6; i++) {
            String label = current.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            monthly.put(label, BigDecimal.ZERO);
            current = current.plusMonths(1);
        }

        for (RevenueRecord record : revenueRecordRepository.findAll()) {
            LocalDate paymentDate = record.getPaymentDate();
            LocalDate threshold = LocalDate.now().withDayOfMonth(1).minusMonths(5);
            if (!paymentDate.isBefore(threshold)) {
                String label = paymentDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                monthly.computeIfPresent(label, (k, v) -> v.add(record.getAmount()));
            }
        }

        List<RevenueTrendResponse> out = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : monthly.entrySet()) {
            out.add(new RevenueTrendResponse(entry.getKey(), entry.getValue()));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<RevenueSummaryResponse> revenueSummaryByDistrict() {
        Map<String, BigDecimal> demandMap = new HashMap<>();
        landRecordRepository.findAll().forEach(land ->
                demandMap.merge(land.getDistrict(), land.getAreaInAcres().multiply(new BigDecimal("1000")), BigDecimal::add)
        );

        Map<String, BigDecimal> collectedMap = new HashMap<>();
        revenueRecordRepository.findAll().forEach(record ->
                collectedMap.merge(record.getLandRecord().getDistrict(), record.getAmount(), BigDecimal::add)
        );

        List<RevenueSummaryResponse> out = new ArrayList<>();
        for (String district : demandMap.keySet()) {
            BigDecimal demand = demandMap.getOrDefault(district, BigDecimal.ZERO);
            BigDecimal collected = collectedMap.getOrDefault(district, BigDecimal.ZERO);
            BigDecimal arrears = demand.subtract(collected).max(BigDecimal.ZERO);
            out.add(new RevenueSummaryResponse(district, demand, collected, arrears));
        }
        return out;
    }
}
