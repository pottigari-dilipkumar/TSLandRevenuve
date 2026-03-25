package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.land.LandRecordRequest;
import in.gov.landrevenue.clean.dto.land.LandRecordResponse;
import in.gov.landrevenue.clean.entity.LandRecord;
import in.gov.landrevenue.clean.entity.Owner;
import in.gov.landrevenue.clean.exception.ResourceNotFoundException;
import in.gov.landrevenue.clean.mapper.LandRevenueMapper;
import in.gov.landrevenue.clean.repository.LandRecordRepository;
import in.gov.landrevenue.clean.repository.OwnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LandRecordService {
    private static final Logger log = LoggerFactory.getLogger(LandRecordService.class);
    private final LandRecordRepository landRecordRepository;
    private final OwnerRepository ownerRepository;
    private final LandRevenueMapper mapper;

    public LandRecordService(LandRecordRepository landRecordRepository, OwnerRepository ownerRepository, LandRevenueMapper mapper) {
        this.landRecordRepository = landRecordRepository;
        this.ownerRepository = ownerRepository;
        this.mapper = mapper;
    }

    public LandRecordResponse create(LandRecordRequest request) {
        Owner owner = ownerRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found for ID: " + request.ownerId()));
        LandRecord landRecord = new LandRecord();
        mapRequest(landRecord, request, owner);
        LandRecord saved = landRecordRepository.save(landRecord);
        log.info("Land record created with ID {}", saved.getId());
        return mapper.toLandResponse(saved);
    }

    public Page<LandRecordResponse> list(Pageable pageable) {
        return landRecordRepository.findAll(pageable).map(mapper::toLandResponse);
    }

    public LandRecordResponse getById(Long id) {
        LandRecord record = findEntity(id);
        return mapper.toLandResponse(record);
    }

    public LandRecordResponse update(Long id, LandRecordRequest request) {
        LandRecord existing = findEntity(id);
        Owner owner = ownerRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found for ID: " + request.ownerId()));
        mapRequest(existing, request, owner);
        LandRecord saved = landRecordRepository.save(existing);
        log.info("Land record {} updated", id);
        return mapper.toLandResponse(saved);
    }

    public void delete(Long id) {
        LandRecord existing = findEntity(id);
        landRecordRepository.delete(existing);
        log.info("Land record {} deleted", id);
    }

    public LandRecord findEntity(Long id) {
        return landRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land record not found for ID: " + id));
    }

    private void mapRequest(LandRecord landRecord, LandRecordRequest request, Owner owner) {
        landRecord.setSurveyNumber(request.surveyNumber());
        landRecord.setDistrict(request.district());
        landRecord.setVillage(request.village());
        landRecord.setAreaInAcres(request.areaInAcres());
        landRecord.setOwner(owner);
    }
}
