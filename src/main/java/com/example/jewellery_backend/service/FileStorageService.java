package com.example.jewellery_backend.service;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.example.jewellery_backend.config.FileStorageProperties;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;

    public FileStorageService(FileStorageProperties properties) {
        this.fileStorageLocation = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    /**
     * Store the file under subDir (e.g., "order123"). Returns the **relative** path that can be saved in DB:
     * e.g., order123/receipt.png
     */
    public String storeFile(MultipartFile file, String subDir) {
        String original = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        try {
            if (original.contains("..")) {
                throw new RuntimeException("Invalid path sequence in filename " + original);
            }
            Path targetDir = this.fileStorageLocation.resolve(subDir).normalize();
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(original);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            // return relative path from upload-dir
            Path rel = this.fileStorageLocation.relativize(target);
            return rel.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + original, e);
        }
    }

    public Resource loadAsResource(String relativePath) {
        try {
            Path file = this.fileStorageLocation.resolve(relativePath).normalize();
            if (!Files.exists(file)) throw new RuntimeException("File not found: " + relativePath);
            return new PathResource(file);
        } catch (Exception ex) {
            throw new RuntimeException("Could not read file: " + relativePath, ex);
        }
    }

    public void delete(String relativePath) {
        try {
            Path path = this.fileStorageLocation.resolve(relativePath).normalize();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // swallow or log
        }
    }
}
