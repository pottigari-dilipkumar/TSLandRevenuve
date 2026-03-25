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
import org.springframework.stereotype.Service;

@Service
public class RevenueRecordService {
    private static final Logger log = LoggerFactory.getLogger(RevenueRecordService.class);
    private final RevenueRecordRepository revenueRecordRepository;
    private final LandRecordService landRecordService;
    private final LandRevenueMapper mapper;

    public RevenueRecordService(RevenueRecordRepository revenueRecordRepository, LandRecordService landRecordService, LandRevenueMapper mapper) {
        this.revenueRecordRepository = revenueRecordRepository;
        this.landRecordService = landRecordService;
        this.mapper = mapper;
    }

    public RevenueRecordResponse create(RevenueRecordRequest request) {
        RevenueRecord revenueRecord = new RevenueRecord();
        revenueRecord.setAmount(request.amount());
        revenueRecord.setPaymentDate(request.paymentDate());
        revenueRecord.setPaymentReference(request.paymentReference());
        revenueRecord.setLandRecord(landRecordService.findEntity(request.landRecordId()));

        RevenueRecord saved = revenueRecordRepository.save(revenueRecord);
        log.info("Revenue record created with ID {}", saved.getId());
        return mapper.toRevenueResponse(saved);
    }

    public Page<RevenueRecordResponse> list(Pageable pageable) {
        return revenueRecordRepository.findAll(pageable).map(mapper::toRevenueResponse);
    }
}
