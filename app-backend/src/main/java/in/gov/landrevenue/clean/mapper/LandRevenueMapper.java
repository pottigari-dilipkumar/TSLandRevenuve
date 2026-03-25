package in.gov.landrevenue.clean.mapper;

import in.gov.landrevenue.clean.dto.land.LandRecordResponse;
import in.gov.landrevenue.clean.dto.land.OwnerResponse;
import in.gov.landrevenue.clean.dto.revenue.RevenueRecordResponse;
import in.gov.landrevenue.clean.entity.LandRecord;
import in.gov.landrevenue.clean.entity.Owner;
import in.gov.landrevenue.clean.entity.RevenueRecord;
import org.springframework.stereotype.Component;

@Component
public class LandRevenueMapper {
    public OwnerResponse toOwnerResponse(Owner owner) {
        return new OwnerResponse(owner.getId(), owner.getName(), owner.getNationalId());
    }

    public LandRecordResponse toLandResponse(LandRecord landRecord) {
        return new LandRecordResponse(
                landRecord.getId(),
                landRecord.getSurveyNumber(),
                landRecord.getDistrict(),
                landRecord.getVillage(),
                landRecord.getAreaInAcres(),
                landRecord.getOwner().getId(),
                landRecord.getOwner().getName()
        );
    }

    public RevenueRecordResponse toRevenueResponse(RevenueRecord revenueRecord) {
        return new RevenueRecordResponse(
                revenueRecord.getId(),
                revenueRecord.getAmount(),
                revenueRecord.getPaymentDate(),
                revenueRecord.getPaymentReference(),
                revenueRecord.getLandRecord().getId()
        );
    }
}
