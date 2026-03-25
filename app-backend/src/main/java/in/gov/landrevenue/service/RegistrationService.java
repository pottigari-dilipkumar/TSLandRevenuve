package in.gov.landrevenue.service;

import in.gov.landrevenue.blockchain.BlockchainAnchorResult;
import in.gov.landrevenue.blockchain.BlockchainRegistrationGateway;
import in.gov.landrevenue.dto.RegistrationCreateRequest;
import in.gov.landrevenue.model.RegistrationRecord;
import in.gov.landrevenue.model.RegistrationRecordEntity;
import in.gov.landrevenue.repository.RegistrationRecordRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
public class RegistrationService {
    private final RegistrationRecordRepository registrationRecordRepository;
    private final BlockchainRegistrationGateway blockchainRegistrationGateway;

    public RegistrationService(RegistrationRecordRepository registrationRecordRepository,
                               BlockchainRegistrationGateway blockchainRegistrationGateway) {
        this.registrationRecordRepository = registrationRecordRepository;
        this.blockchainRegistrationGateway = blockchainRegistrationGateway;
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
        entity.setOwnerWalletAddress(request.ownerWalletAddress());
        entity.setCreatedAt(Instant.now());
        entity.setStatus("SUBMITTED");
        entity.setBlockchainSyncStatus("PENDING");

        if (blockchainRegistrationGateway.isEnabled() && !StringUtils.hasText(request.ownerWalletAddress())) {
            throw new IllegalArgumentException("ownerWalletAddress is required when blockchain integration is enabled");
        }

        RegistrationRecordEntity saved = registrationRecordRepository.saveAndFlush(entity);
        applyAnchorResult(saved, blockchainRegistrationGateway.anchorRegistration(saved));
        return toDomain(registrationRecordRepository.save(saved));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "registrationByRef", key = "#registrationRef")
    public RegistrationRecord findByReference(String registrationRef) {
        RegistrationRecordEntity entity = registrationRecordRepository.findById(registrationRef)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        return toDomain(entity);
    }

    @Transactional
    @CachePut(value = "registrationByRef", key = "#registrationRef")
    public RegistrationRecord retryBlockchainSync(String registrationRef) {
        RegistrationRecordEntity entity = registrationRecordRepository.findById(registrationRef)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        applyAnchorResult(entity, blockchainRegistrationGateway.anchorRegistration(entity));
        return toDomain(registrationRecordRepository.save(entity));
    }

    public boolean blockchainHealthy() {
        return blockchainRegistrationGateway.isHealthy();
    }

    private RegistrationRecord toDomain(RegistrationRecordEntity entity) {
        return new RegistrationRecord(
                entity.getRegistrationRef(),
                entity.getParcelId(),
                entity.getSellerName(),
                entity.getBuyerName(),
                entity.getDeedHash(),
                entity.getVerifiedIdentityToken(),
                entity.getOwnerWalletAddress(),
                entity.getCreatedAt(),
                entity.getStatus(),
                entity.getBlockchainSyncStatus(),
                entity.getBlockchainTxHash(),
                entity.getBlockchainBlockNumber(),
                entity.getBlockchainSyncedAt(),
                entity.getBlockchainErrorMessage()
        );
    }

    private void applyAnchorResult(RegistrationRecordEntity entity, BlockchainAnchorResult result) {
        entity.setBlockchainSyncStatus(result.syncStatus());
        entity.setBlockchainTxHash(result.transactionHash());
        entity.setBlockchainBlockNumber(result.blockNumber());
        entity.setBlockchainSyncedAt(result.syncedAt());
        entity.setBlockchainErrorMessage(result.errorMessage());
    }
}
