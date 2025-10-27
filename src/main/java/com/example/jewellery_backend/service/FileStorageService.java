package com.example.jewellery_backend.service;

import com.example.jewellery_backend.config.FileStorageProperties;
import com.example.jewellery_backend.exception.FileStorageException;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(FileStorageProperties properties) {
        this.fileStorageLocation = Paths.get(properties.getUploadDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create upload directory", ex);
        }
    }

    /**
     * Store a file under a given sub-directory (e.g., "orders/order123").
     * Returns the **relative path** (with subdirectory) to save in DB.
     */
    public String storeFile(MultipartFile file, String subDir) {
        String original = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (original.contains("..")) {
            throw new FileStorageException("Invalid path sequence in filename " + original);
        }

        // Extract extension and generate unique filename
        String ext = "";
        int i = original.lastIndexOf('.');
        if (i >= 0) ext = original.substring(i);
        String filename = UUID.randomUUID() + ext;

        try {
            Path targetDir = this.fileStorageLocation.resolve(subDir).normalize();
            Files.createDirectories(targetDir);

            Path target = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path from upload-dir
            Path rel = this.fileStorageLocation.relativize(target);
            return rel.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file " + original, e);
        }
    }

    /**
     * Load a file as Spring Resource using its relative path.
     */
    public Resource loadAsResource(String relativePath) {
        try {
            Path file = this.fileStorageLocation.resolve(relativePath).normalize();
            if (!Files.exists(file)) {
                throw new FileStorageException("File not found: " + relativePath);
            }
            return new PathResource(file);
        } catch (Exception ex) {
            throw new FileStorageException("Could not read file: " + relativePath, ex);
        }
    }

    /**
     * Delete a file by its relative path.
     */
    public void delete(String relativePath) {
        try {
            Path file = this.fileStorageLocation.resolve(relativePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Optional: log error
        }
    }

    /**
     * Get the absolute Path of a stored file.
     */
    public Path getFilePath(String relativePath) {
        return this.fileStorageLocation.resolve(relativePath).normalize();
    }
}
