package in.gov.landrevenue.model.platform;

import java.util.List;

public record LandParcel(
        String landId,
        String village,
        String surveyNumber,
        String seller,
        String buyer,
        List<GeoPoint> polygon,
        List<LandEvent> history,
        List<LandDocument> documents
) {
}
