package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.land.OwnerRequest;
import in.gov.landrevenue.clean.dto.land.OwnerResponse;
import in.gov.landrevenue.clean.entity.Owner;
import in.gov.landrevenue.clean.mapper.LandRevenueMapper;
import in.gov.landrevenue.clean.repository.OwnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OwnerService {
    private static final Logger log = LoggerFactory.getLogger(OwnerService.class);
    private final OwnerRepository ownerRepository;
    private final LandRevenueMapper mapper;

    public OwnerService(OwnerRepository ownerRepository, LandRevenueMapper mapper) {
        this.ownerRepository = ownerRepository;
        this.mapper = mapper;
    }

    public OwnerResponse createOwner(OwnerRequest request) {
        Owner owner = new Owner();
        owner.setName(request.name());
        owner.setNationalId(request.nationalId());
        Owner saved = ownerRepository.save(owner);
        log.info("Owner created with ID {}", saved.getId());
        return mapper.toOwnerResponse(saved);
    }

    public Page<OwnerResponse> listOwners(Pageable pageable) {
        return ownerRepository.findAll(pageable).map(mapper::toOwnerResponse);
    }
}
