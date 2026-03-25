package in.gov.landrevenue.controller;

import in.gov.landrevenue.dto.RegistrationCreateRequest;
import in.gov.landrevenue.model.RegistrationRecord;
import in.gov.landrevenue.service.AadhaarAuthService;
import in.gov.landrevenue.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RegistrationController {
    private final RegistrationService registrationService;
    private final AadhaarAuthService aadhaarAuthService;

    public RegistrationController(RegistrationService registrationService, AadhaarAuthService aadhaarAuthService) {
        this.registrationService = registrationService;
        this.aadhaarAuthService = aadhaarAuthService;
    }

    @PostMapping("/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationRecord createRegistration(@Valid @RequestBody RegistrationCreateRequest request) {
        if (!aadhaarAuthService.isVerifiedTokenValid(request.verifiedIdentityToken())) {
            throw new IllegalArgumentException("Verified identity token is invalid or expired");
        }
        return registrationService.create(request);
    }

    @GetMapping("/public/verify/{registrationRef}")
    public RegistrationRecord verifyRegistration(@PathVariable String registrationRef) {
        return registrationService.findByReference(registrationRef);
    }

    @PostMapping("/registrations/{registrationRef}/blockchain/sync")
    public RegistrationRecord retryBlockchainSync(@PathVariable String registrationRef) {
        return registrationService.retryBlockchainSync(registrationRef);
    }

    @GetMapping("/registrations/blockchain/health")
    public String blockchainHealth() {
        return registrationService.blockchainHealthy() ? "UP" : "DOWN";
    }
}
