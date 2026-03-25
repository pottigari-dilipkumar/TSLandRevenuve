package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.auth.AuthRequest;
import in.gov.landrevenue.clean.dto.auth.AuthResponse;
import in.gov.landrevenue.clean.entity.User;
import in.gov.landrevenue.clean.exception.ResourceNotFoundException;
import in.gov.landrevenue.clean.repository.UserRepository;
import in.gov.landrevenue.clean.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        log.info("JWT token generated for user: {}", user.getUsername());
        return new AuthResponse(token, user.getRole().name());
    }
}
