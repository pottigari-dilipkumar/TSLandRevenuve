package in.gov.landrevenue.service;

import in.gov.landrevenue.dto.RegistrationCreateRequest;
import in.gov.landrevenue.model.RegistrationRecord;
import in.gov.landrevenue.model.RegistrationRecordEntity;
import in.gov.landrevenue.repository.RegistrationRecordRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RegistrationService {
    private final RegistrationRecordRepository registrationRecordRepository;

    public RegistrationService(RegistrationRecordRepository registrationRecordRepository) {
        this.registrationRecordRepository = registrationRecordRepository;
    }

    @Transactional
    @CachePut(value = "registrationByRef", key = "#result.registrationRef()")
    public RegistrationRecord create(RegistrationCreateRequest request) {
        if (registrationRecordRepository.existsByRegistrationRef(request.registrationRef())) {
            throw new IllegalArgumentException("Registration reference already exists");
        }

        RegistrationRecordEntity entity = new RegistrationRecordEntity();
        entity.setRegistrationRef(request.registrationRef());
        entity.setParcelId(request.parcelId());
        entity.setSellerName(request.sellerName());
        entity.setBuyerName(request.buyerName());
        entity.setDeedHash(request.deedHash());
        entity.setVerifiedIdentityToken(request.verifiedIdentityToken());
        entity.setCreatedAt(Instant.now());
        entity.setStatus("SUBMITTED");

        RegistrationRecordEntity saved = registrationRecordRepository.save(entity);
        return toDomain(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "registrationByRef", key = "#registrationRef")
    public RegistrationRecord findByReference(String registrationRef) {
        RegistrationRecordEntity entity = registrationRecordRepository.findById(registrationRef)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        return toDomain(entity);
    }

    private RegistrationRecord toDomain(RegistrationRecordEntity entity) {
        return new RegistrationRecord(
                entity.getRegistrationRef(),
                entity.getParcelId(),
                entity.getSellerName(),
                entity.getBuyerName(),
                entity.getDeedHash(),
                entity.getVerifiedIdentityToken(),
                entity.getCreatedAt(),
                entity.getStatus()
        );
    }
}
