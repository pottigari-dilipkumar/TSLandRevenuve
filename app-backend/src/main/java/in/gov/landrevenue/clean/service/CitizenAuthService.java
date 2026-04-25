package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.auth.CitizenAuthResponse;
import in.gov.landrevenue.clean.dto.auth.CitizenProfileRequest;
import in.gov.landrevenue.clean.entity.User;
import in.gov.landrevenue.clean.enums.Role;
import in.gov.landrevenue.clean.repository.UserRepository;
import in.gov.landrevenue.clean.security.JwtService;
import in.gov.landrevenue.service.AadhaarAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CitizenAuthService {
    private static final Logger log = LoggerFactory.getLogger(CitizenAuthService.class);

    private final AadhaarAuthService aadhaarAuthService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public CitizenAuthService(AadhaarAuthService aadhaarAuthService, UserRepository userRepository,
                               JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.aadhaarAuthService = aadhaarAuthService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public String sendOtp(String aadhaarNumber) {
        return aadhaarAuthService.sendOtp(aadhaarNumber);
    }

    @Transactional
    public CitizenAuthResponse verifyOtpAndLogin(String aadhaarNumber, String otp) {
        // Validate OTP (reuse existing AadhaarAuthService)
        aadhaarAuthService.verifyOtp(aadhaarNumber, otp);

        // Find or create citizen user
        User user = userRepository.findByAadhaarNumber(aadhaarNumber).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("citizen_" + aadhaarNumber);
            newUser.setAadhaarNumber(aadhaarNumber);
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setRole(Role.CITIZEN);
            return userRepository.save(newUser);
        });

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        boolean profileComplete = user.getFullName() != null && user.getMobile() != null;
        log.info("Citizen login: aadhaar=****{}", aadhaarNumber.substring(8));
        return new CitizenAuthResponse(token, user.getRole().name(), profileComplete,
                user.getFullName(), aadhaarNumber, user.getMobile(), user.getEmail());
    }

    @Transactional
    public CitizenAuthResponse updateProfile(String aadhaarNumber, CitizenProfileRequest request) {
        User user = userRepository.findByAadhaarNumber(aadhaarNumber)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found"));
        user.setFullName(request.fullName());
        user.setMobile(request.mobile());
        user.setEmail(request.email());
        user.setAddress(request.address());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new CitizenAuthResponse(token, user.getRole().name(), true,
                user.getFullName(), aadhaarNumber, user.getMobile(), user.getEmail());
    }

    public CitizenAuthResponse getProfile(String aadhaarNumber) {
        User user = userRepository.findByAadhaarNumber(aadhaarNumber)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found"));
        boolean profileComplete = user.getFullName() != null && user.getMobile() != null;
        return new CitizenAuthResponse(null, user.getRole().name(), profileComplete,
                user.getFullName(), aadhaarNumber, user.getMobile(), user.getEmail());
    }
}
