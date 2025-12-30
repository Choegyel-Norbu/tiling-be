package com.tilingroofing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for file storage.
 */
@Component
@ConfigurationProperties(prefix = "app.file-storage")
public class FileStorageProperties {

    private String uploadDir = "./uploads";
    private String allowedTypes = "image/jpeg,image/png,image/gif,image/webp,application/pdf";
    private long maxFileSize = 10485760L; // 10MB

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getAllowedTypesRaw() {
        return allowedTypes;
    }

    public List<String> getAllowedTypes() {
        return Arrays.asList(allowedTypes.split(","));
    }

    public void setAllowedTypes(String allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
}
