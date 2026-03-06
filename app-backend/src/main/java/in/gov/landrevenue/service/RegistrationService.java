package in.gov.landrevenue.service;

import in.gov.landrevenue.dto.RegistrationCreateRequest;
import in.gov.landrevenue.model.RegistrationRecord;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistrationService {
    private final Map<String, RegistrationRecord> recordsByRef = new ConcurrentHashMap<>();

    public RegistrationRecord create(RegistrationCreateRequest request) {
        if (recordsByRef.containsKey(request.registrationRef())) {
            throw new IllegalArgumentException("Registration reference already exists");
        }

        RegistrationRecord record = new RegistrationRecord(
                request.registrationRef(),
                request.parcelId(),
                request.sellerName(),
                request.buyerName(),
                request.deedHash(),
                request.verifiedIdentityToken(),
                Instant.now(),
                "SUBMITTED"
        );
        recordsByRef.put(request.registrationRef(), record);
        return record;
    }

    public RegistrationRecord findByReference(String registrationRef) {
        RegistrationRecord record = recordsByRef.get(registrationRef);
        if (record == null) {
            throw new IllegalArgumentException("Registration not found");
        }
        return record;
    }
}
