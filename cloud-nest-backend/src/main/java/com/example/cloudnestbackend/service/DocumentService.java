package com.example.cloudnestbackend.service;

import com.example.cloudnestbackend.entity.Document;
import com.example.cloudnestbackend.entity.DocumentAccess;
import com.example.cloudnestbackend.entity.DocumentVersion;
import com.example.cloudnestbackend.entity.User;
import com.example.cloudnestbackend.repository.DocumentAccessRepository;
import com.example.cloudnestbackend.repository.DocumentRepository;
import com.example.cloudnestbackend.repository.DocumentVersionRepository;
import com.example.cloudnestbackend.repository.UserRepository;
import com.example.cloudnestbackend.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;
    private final DocumentAccessRepository accessRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional
    public Document uploadDocument(MultipartFile file, String ownerEmail) throws IOException {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UUID documentId = UUID.randomUUID();
        String s3Key = "documents/" + documentId + "/v1-" + file.getOriginalFilename();

        s3Service.uploadFile(s3Key, file);

        Document doc = new Document();
        doc.setId(documentId);
        doc.setName(file.getOriginalFilename());
        doc.setOwnerEmail(ownerEmail);
        doc.setS3Key(s3Key);
        doc.setOwner(owner);
        doc.setCurrentVersion(1);

        Document savedDoc = documentRepository.save(doc);

        DocumentVersion version = new DocumentVersion(savedDoc, 1, s3Key);
        versionRepository.save(version);

        return savedDoc;
    }

    @Transactional
    public void uploadNewVersion(UUID documentId, MultipartFile file, String email) throws IOException {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!doc.getOwnerEmail().equals(email)) {
            throw new RuntimeException("Only the owner can upload new versions");
        }

        int nextVersion = doc.getCurrentVersion() + 1;
        String s3Key = "documents/" + documentId + "/v" + nextVersion + "-" + file.getOriginalFilename();

        s3Service.uploadFile(s3Key, file);

        DocumentVersion version = new DocumentVersion(doc, nextVersion, s3Key);
        versionRepository.save(version);

        doc.setCurrentVersion(nextVersion);
        doc.setS3Key(s3Key);
        documentRepository.save(doc);
    }

    public List<Document> getDocumentsForUser(String email) {
        // Documents owned by the user
        List<Document> owned = documentRepository.findByOwnerEmail(email);

        // Documents shared with the user
        List<DocumentAccess> accesses = accessRepository.findByUserEmailAndRevokedFalse(email);
        List<Document> shared = accesses.stream()
                .map(DocumentAccess::getDocument)
                .collect(Collectors.toList());

        List<Document> all = new ArrayList<>(owned);
        all.addAll(shared);
        return all;
    }

    @Transactional
    public String grantAccess(UUID documentId, String reviewerEmail, String ownerEmail) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!doc.getOwnerEmail().equals(ownerEmail)) {
            throw new RuntimeException("Only the owner can grant access");
        }

        String token = TokenUtil.generate();
        DocumentAccess access = new DocumentAccess(
                doc,
                reviewerEmail,
                token,
                Instant.now().plus(3, ChronoUnit.DAYS));

        accessRepository.save(access);
        return token;
    }

    @Transactional
    public void revokeAccess(String token, String email) {
        DocumentAccess access = accessRepository.findByAccessToken(token)
                .orElseThrow(() -> new RuntimeException("Access token invalid"));

        // Allow either the owner of the document or the reviewer themselves to
        // revoke/remove
        Document doc = access.getDocument();
        if (!doc.getOwnerEmail().equals(email) && !access.getUserEmail().equals(email)) {
            throw new RuntimeException("Not authorized to revoke this access");
        }

        access.setRevoked(true);
        accessRepository.save(access);
    }

    @Transactional
    public void deleteDocument(UUID documentId, String email) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!doc.getOwnerEmail().equals(email)) {
            throw new RuntimeException("Only the owner can delete a document permanently");
        }

        // Delete all S3 objects for all versions
        List<DocumentVersion> versions = versionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId);
        for (DocumentVersion v : versions) {
            s3Service.deleteFile(v.getS3Key());
        }

        // Repositories handle cascade or manual deletion
        accessRepository.deleteByDocumentId(documentId);
        versionRepository.deleteByDocumentId(documentId);
        documentRepository.delete(doc);
    }

    public String generateReviewUrl(String token) {
        DocumentAccess access = accessRepository.findByAccessToken(token)
                .orElseThrow(() -> new RuntimeException("Access invalid"));

        if (access.isRevoked() || access.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Access is revoked or expired");
        }

        return s3Service.generatePresignedDownloadUrl(access.getDocument().getS3Key());
    }
}
