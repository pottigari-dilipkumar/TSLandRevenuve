package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.registration.DocumentResponse;
import in.gov.landrevenue.clean.entity.LandRegistration;
import in.gov.landrevenue.clean.entity.RegistrationDocument;
import in.gov.landrevenue.clean.enums.DocumentType;
import in.gov.landrevenue.clean.repository.LandRegistrationRepository;
import in.gov.landrevenue.clean.repository.RegistrationDocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

@Service
public class DocumentStorageService {

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final LandRegistrationRepository registrationRepository;
    private final RegistrationDocumentRepository documentRepository;

    public DocumentStorageService(LandRegistrationRepository registrationRepository,
                                   RegistrationDocumentRepository documentRepository) {
        this.registrationRepository = registrationRepository;
        this.documentRepository = documentRepository;
    }

    public DocumentResponse upload(String registrationRef, MultipartFile file,
                                    DocumentType documentType, String description,
                                    Long uploadedByUserId) throws IOException {
        LandRegistration registration = registrationRepository.findByRegistrationRef(registrationRef)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found: " + registrationRef));

        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path dir = Paths.get(uploadDir, registrationRef);
        Files.createDirectories(dir);
        Path target = dir.resolve(storedName);
        file.transferTo(target);

        RegistrationDocument doc = new RegistrationDocument();
        doc.setLandRegistration(registration);
        doc.setFileName(storedName);
        doc.setOriginalName(file.getOriginalFilename());
        doc.setContentType(file.getContentType());
        doc.setFilePath(target.toString());
        doc.setDocumentType(documentType);
        doc.setDescription(description);
        doc.setUploadedAt(Instant.now());
        doc.setUploadedByUserId(uploadedByUserId);
        RegistrationDocument saved = documentRepository.save(doc);

        return toResponse(saved);
    }

    public Resource download(Long documentId) throws MalformedURLException {
        RegistrationDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        Path path = Paths.get(doc.getFilePath());
        return new UrlResource(path.toUri());
    }

    public String getContentType(Long documentId) {
        return documentRepository.findById(documentId)
                .map(RegistrationDocument::getContentType)
                .orElse("application/octet-stream");
    }

    private DocumentResponse toResponse(RegistrationDocument doc) {
        String downloadUrl = baseUrl + "/api/documents/" + doc.getId() + "/download";
        return new DocumentResponse(doc.getId(), doc.getOriginalName(),
                doc.getDocumentType().name(), doc.getDescription(), downloadUrl, doc.getUploadedAt());
    }
}
