package com.example.cloudnestbackend.controller;

import com.example.cloudnestbackend.entity.Document;
import com.example.cloudnestbackend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, Authentication authentication)
            throws IOException {
        String email = authentication.getName();
        Document doc = documentService.uploadDocument(file, email);
        return ResponseEntity.ok(Map.of(
                "message", "Document uploaded successfully",
                "documentId", doc.getId(),
                "name", doc.getName()));
    }

    @PostMapping("/{id}/version")
    public ResponseEntity<?> uploadNewVersion(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        String email = authentication.getName();
        documentService.uploadNewVersion(id, file, email);
        return ResponseEntity.ok("New version uploaded successfully");
    }

    @GetMapping("/my-files")
    public ResponseEntity<List<Document>> getMyFiles(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(documentService.getDocumentsForUser(email));
    }

    @PostMapping("/grant-access")
    public ResponseEntity<?> grantAccess(
            @RequestParam UUID documentId,
            @RequestParam String reviewerEmail,
            Authentication authentication) {
        String ownerEmail = authentication.getName();
        String token = documentService.grantAccess(documentId, reviewerEmail, ownerEmail);

        // Return the review link (frontend URL would be better, but we follow the core
        // logic)
        String reviewLink = "http://localhost:8080/review/" + token;
        return ResponseEntity.ok(Map.of(
                "message", "Access granted",
                "reviewLink", reviewLink,
                "token", token));
    }

    @PostMapping("/revoke/{token}")
    public ResponseEntity<?> revokeAccess(@PathVariable String token, Authentication authentication) {
        String email = authentication.getName();
        documentService.revokeAccess(token, email);
        return ResponseEntity.ok("Access revoked successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable UUID id, Authentication authentication) {
        String email = authentication.getName();
        documentService.deleteDocument(id, email);
        return ResponseEntity.ok("Document and all versions deleted successfully");
    }
}
