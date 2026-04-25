package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.citizen.MarketValueResponse;
import in.gov.landrevenue.clean.entity.MarketValue;
import in.gov.landrevenue.clean.repository.MarketValueRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MarketValueService {
    private final MarketValueRepository marketValueRepository;

    public MarketValueService(MarketValueRepository marketValueRepository) {
        this.marketValueRepository = marketValueRepository;
    }

    public List<MarketValueResponse> getAllCurrentRates() {
        return marketValueRepository.findAllCurrent().stream().map(this::toResponse).toList();
    }

    public List<MarketValueResponse> getRatesByDistrict(String district) {
        return marketValueRepository.findByDistrictIgnoreCaseOrderByVillageAsc(district)
                .stream().map(this::toResponse).toList();
    }

    public Optional<MarketValueResponse> getCurrentRate(String district, String village) {
        return marketValueRepository.findCurrentRate(district, village).map(this::toResponse);
    }

    private MarketValueResponse toResponse(MarketValue mv) {
        return new MarketValueResponse(mv.getId(), mv.getDistrict(), mv.getVillage(),
                mv.getRatePerAcre(), mv.getEffectiveFrom());
    }
}
