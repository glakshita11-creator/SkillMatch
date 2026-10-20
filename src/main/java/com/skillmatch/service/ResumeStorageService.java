package com.skillmatch.service;

import com.skillmatch.exception.BadRequestException;
import com.skillmatch.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ResumeStorageService {

    @Value("${skillmatch.upload.dir:uploads/resumes}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize resume storage directory at " + uploadDir, e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded resume file is empty.");
        }

        // Validate max 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds 5MB limit.");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new BadRequestException("Only PDF documents are supported for resume uploads.");
        }

        // Validate PDF magic bytes (%PDF)
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            if (read < 4 || header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46) {
                throw new BadRequestException("Invalid PDF file header. The uploaded file is not a valid PDF document.");
            }
        } catch (IOException e) {
            throw new BadRequestException("Failed to read uploaded file: " + e.getMessage());
        }

        // Sanitize original filename and prevent directory traversal
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "resume.pdf";
        }
        String cleanName = Paths.get(originalFilename).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!cleanName.toLowerCase().endsWith(".pdf")) {
            cleanName += ".pdf";
        }

        String safeStoredFilename = UUID.randomUUID().toString() + "_" + cleanName;

        try {
            Path destinationFile = this.rootLocation.resolve(safeStoredFilename).normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation)) {
                throw new BadRequestException("Cannot store file outside current directory (path traversal detected).");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return safeStoredFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store resume file.", e);
        }
    }

    public Resource loadAsResource(String filename) {
        try {
            if (filename == null || filename.isBlank()) {
                throw new ResourceNotFoundException("Resume file not found.");
            }
            Path file = rootLocation.resolve(filename).normalize().toAbsolutePath();
            if (!file.startsWith(rootLocation)) {
                throw new BadRequestException("Invalid file access path.");
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Resume file could not be read or does not exist: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Could not read file: " + filename);
        }
    }

    public void deleteFile(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Path file = rootLocation.resolve(filename).normalize().toAbsolutePath();
            if (file.startsWith(rootLocation)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException ignored) {
        }
    }
}
