package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.revenue.RevenueRecordRequest;
import in.gov.landrevenue.clean.dto.revenue.RevenueRecordResponse;
import in.gov.landrevenue.clean.entity.RevenueRecord;
import in.gov.landrevenue.clean.mapper.LandRevenueMapper;
import in.gov.landrevenue.clean.repository.RevenueRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RevenueRecordService {
    private static final Logger log = LoggerFactory.getLogger(RevenueRecordService.class);
    private final RevenueRecordRepository revenueRecordRepository;
    private final LandRecordService landRecordService;
    private final LandRevenueMapper mapper;
    private final AuditLogService auditLogService;

    public RevenueRecordService(RevenueRecordRepository revenueRecordRepository,
                                LandRecordService landRecordService,
                                LandRevenueMapper mapper,
                                AuditLogService auditLogService) {
        this.revenueRecordRepository = revenueRecordRepository;
        this.landRecordService = landRecordService;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
    }

    public RevenueRecordResponse create(RevenueRecordRequest request) {
        RevenueRecord revenueRecord = new RevenueRecord();
        revenueRecord.setAmount(request.amount());
        revenueRecord.setPaymentDate(request.paymentDate());
        revenueRecord.setPaymentReference(request.paymentReference());
        revenueRecord.setLandRecord(landRecordService.findEntity(request.landRecordId()));

        RevenueRecord saved = revenueRecordRepository.save(revenueRecord);
        auditLogService.log("CREATE", "RevenueRecord", "Created revenue record id=" + saved.getId());
        log.info("Revenue record created with ID {}", saved.getId());
        return mapper.toRevenueResponse(saved);
    }

    public Page<RevenueRecordResponse> list(Long landRecordId,
                                            LocalDate fromDate,
                                            LocalDate toDate,
                                            BigDecimal minAmount,
                                            BigDecimal maxAmount,
                                            String paymentReference,
                                            Pageable pageable) {
        Specification<RevenueRecord> spec = Specification.where(null);
        if (landRecordId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("landRecord").get("id"), landRecordId));
        }
        if (fromDate != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("paymentDate"), fromDate));
        }
        if (toDate != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("paymentDate"), toDate));
        }
        if (minAmount != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
        }
        if (maxAmount != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
        }
        if (paymentReference != null && !paymentReference.isBlank()) {
            String query = paymentReference.trim().toLowerCase();
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("paymentReference")), "%" + query + "%"));
        }
        return revenueRecordRepository.findAll(spec, pageable).map(mapper::toRevenueResponse);
    }

    public List<RevenueRecordResponse> listForExport(Long landRecordId,
                                                     LocalDate fromDate,
                                                     LocalDate toDate,
                                                     BigDecimal minAmount,
                                                     BigDecimal maxAmount,
                                                     String paymentReference) {
        return list(landRecordId, fromDate, toDate, minAmount, maxAmount, paymentReference, Pageable.unpaged()).getContent();
    }
}
