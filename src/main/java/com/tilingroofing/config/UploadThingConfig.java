package com.tilingroofing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration class for UploadThing integration.
 * Loads configuration from application.properties.
 */
@Configuration
public class UploadThingConfig {

    @Value("${uploadthing.api.base-url:https://api.uploadthing.com/v5}")
    private String baseUrl;

    @Value("${uploadthing.api.secret:}")
    private String apiSecret;

    @Value("${uploadthing.api.app-id:}")
    private String appId;

    @Value("${uploadthing.api.timeout:30s}")
    private Duration timeout;

    @Value("${uploadthing.api.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${uploadthing.api.retry.backoff-delay:1s}")
    private Duration backoffDelay;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public String getAppId() {
        return appId;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public Duration getBackoffDelay() {
        return backoffDelay;
    }
}

