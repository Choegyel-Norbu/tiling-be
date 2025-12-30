package com.tilingroofing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for UploadThing integration.
 * Loads settings from application.properties with prefix 'app.uploadthing'
 */
@Component
@ConfigurationProperties(prefix = "app.uploadthing")
public class UploadThingProperties {

    /**
     * UploadThing API secret key.
     * This should be kept secure and never exposed to clients.
     */
    private String apiKey;

    /**
     * Node.js UploadThing service URL.
     * This is the URL of the Node.js service that wraps UploadThing SDK.
     * Defaults to localhost:3001
     */
    private String serviceUrl = "http://localhost:3001";

    /**
     * Whether UploadThing is enabled.
     * If false, falls back to local file storage.
     */
    private boolean enabled = true;

    /**
     * Maximum file size in bytes.
     * Default: 10MB (10485760 bytes)
     */
    private long maxFileSize = 10485760L;

    /**
     * Allowed file types (MIME types).
     * Comma-separated list of allowed MIME types.
     */
    private String allowedTypes = "image/jpeg,image/png,image/gif,image/webp,application/pdf";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(String allowedTypes) {
        this.allowedTypes = allowedTypes;
    }
}

