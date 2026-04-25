package in.gov.landrevenue.clean.controller;

import in.gov.landrevenue.clean.dto.registration.DocumentResponse;
import in.gov.landrevenue.clean.enums.DocumentType;
import in.gov.landrevenue.clean.repository.UserRepository;
import in.gov.landrevenue.clean.service.DocumentStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentStorageService documentStorageService;
    private final UserRepository userRepository;

    public DocumentController(DocumentStorageService documentStorageService, UserRepository userRepository) {
        this.documentStorageService = documentStorageService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('SRO', 'SRO_ASSISTANT', 'ADMIN', 'CITIZEN')")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam String registrationRef,
            @RequestParam MultipartFile file,
            @RequestParam DocumentType documentType,
            @RequestParam(required = false) String description,
            Principal principal) throws IOException {
        Long userId = resolveUserId(principal.getName());
        DocumentResponse response = documentStorageService.upload(registrationRef, file, documentType, description, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
        Resource resource = documentStorageService.download(id);
        String contentType = documentStorageService.getContentType(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private Long resolveUserId(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElse(null);
    }
}
