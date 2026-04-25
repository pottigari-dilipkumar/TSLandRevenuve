package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.user.UserCreateRequest;
import in.gov.landrevenue.clean.dto.user.UserResponse;
import in.gov.landrevenue.clean.entity.User;
import in.gov.landrevenue.clean.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.username());
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setFullName(request.fullName());
        user.setMobile(request.mobile());
        user.setEmail(request.email());
        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getRole().name(),
                u.getFullName(), u.getMobile(), u.getEmail());
    }
}
