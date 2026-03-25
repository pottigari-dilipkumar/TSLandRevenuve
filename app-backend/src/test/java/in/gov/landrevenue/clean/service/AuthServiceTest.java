package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.auth.AuthRequest;
import in.gov.landrevenue.clean.dto.auth.AuthResponse;
import in.gov.landrevenue.clean.entity.User;
import in.gov.landrevenue.clean.enums.Role;
import in.gov.landrevenue.clean.exception.ResourceNotFoundException;
import in.gov.landrevenue.clean.repository.UserRepository;
import in.gov.landrevenue.clean.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticate_shouldReturnToken_whenCredentialsValid() {
        AuthRequest request = new AuthRequest("admin", "plain-password");
        User user = new User();
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setRole(Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken("admin", "ADMIN")).thenReturn("jwt-token");

        AuthResponse response = authService.authenticate(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    void authenticate_shouldThrow_whenPasswordInvalid() {
        AuthRequest request = new AuthRequest("officer", "wrong-password");
        User user = new User();
        user.setUsername("officer");
        user.setPassword("encoded-password");
        user.setRole(Role.REVENUE_OFFICER);

        when(userRepository.findByUsername("officer")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invalid username or password");

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void authenticate_shouldThrow_whenUserMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(new AuthRequest("ghost", "pwd")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invalid username or password");
    }
}
