package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.user.UserCreateRequest;
import in.gov.landrevenue.clean.dto.user.UserResponse;
import in.gov.landrevenue.clean.entity.User;
import in.gov.landrevenue.clean.enums.Role;
import in.gov.landrevenue.clean.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private static final List<Role> STAFF_ROLES = List.of(
            Role.ADMIN, Role.REVENUE_OFFICER, Role.DATA_ENTRY, Role.SRO, Role.SRO_ASSISTANT);
    private static final List<Role> CITIZEN_ROLES = List.of(Role.CITIZEN);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public Map<String, Object> listUsers(
            @RequestParam(defaultValue = "staff") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Role> roles = "citizen".equalsIgnoreCase(type) ? CITIZEN_ROLES : STAFF_ROLES;
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<UserResponse> result = userRepository.findByRoleIn(roles, pageable)
                .map(this::toResponse);
        return Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "page", result.getNumber(),
                "size", result.getSize()
        );
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
