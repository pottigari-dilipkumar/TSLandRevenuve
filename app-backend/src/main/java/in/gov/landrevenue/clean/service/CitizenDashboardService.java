package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.citizen.CitizenLandResponse;
import in.gov.landrevenue.clean.dto.registration.RegistrationResponse;
import in.gov.landrevenue.clean.entity.LandRecord;
import in.gov.landrevenue.clean.repository.LandRecordRepository;
import in.gov.landrevenue.clean.repository.MarketValueRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CitizenDashboardService {

    private final LandRecordRepository landRecordRepository;
    private final MarketValueRepository marketValueRepository;
    private final LandRegistrationService landRegistrationService;

    public CitizenDashboardService(LandRecordRepository landRecordRepository,
                                    MarketValueRepository marketValueRepository,
                                    LandRegistrationService landRegistrationService) {
        this.landRecordRepository = landRecordRepository;
        this.marketValueRepository = marketValueRepository;
        this.landRegistrationService = landRegistrationService;
    }

    public List<CitizenLandResponse> getMyLands(String aadhaarNumber) {
        return landRecordRepository.findByOwnerNationalId(aadhaarNumber)
                .stream().map(this::toLandResponse).toList();
    }

    public List<RegistrationResponse> getMyRegistrations(String aadhaarNumber) {
        return landRegistrationService.getForCitizen(aadhaarNumber);
    }

    private CitizenLandResponse toLandResponse(LandRecord land) {
        BigDecimal ratePerAcre = marketValueRepository
                .findCurrentRate(land.getDistrict(), land.getVillage())
                .map(mv -> mv.getRatePerAcre())
                .orElse(BigDecimal.ZERO);

        BigDecimal estimatedValue = ratePerAcre.multiply(land.getAreaInAcres())
                .setScale(2, RoundingMode.HALF_UP);

        return new CitizenLandResponse(land.getId(), land.getSurveyNumber(),
                land.getDistrict(), land.getVillage(), land.getAreaInAcres(),
                ratePerAcre, estimatedValue);
    }
}
