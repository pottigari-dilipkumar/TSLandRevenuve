package in.gov.landrevenue.model.platform;

import java.time.Instant;

public record LandEvent(Instant at, String actor, String action, String remarks) {
}
