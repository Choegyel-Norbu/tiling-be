package com.tilingroofing.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for UploadThing URL operations.
 * Provides methods for URL validation, file key extraction, and URL building.
 */
public class UploadThingUrlUtil {

    // UploadThing URL pattern: https://utfs.io/f/<fileKey> or https://<app-id>.ufs.sh/f/<fileKey>
    private static final Pattern UPLOADTHING_URL_PATTERN = Pattern.compile(
            "https?://(?:utfs\\.io|.*\\.ufs\\.sh)/f/([^/?]+)"
    );

    /**
     * Validates if a URL is a valid UploadThing URL.
     *
     * @param url The URL to validate
     * @return true if the URL is a valid UploadThing URL, false otherwise
     */
    public static boolean isValidUploadThingUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return UPLOADTHING_URL_PATTERN.matcher(url).matches();
    }

    /**
     * Extracts the file key from an UploadThing URL.
     *
     * @param url The UploadThing URL
     * @return The file key, or null if the URL is invalid
     */
    public static String extractFileKey(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        Matcher matcher = UPLOADTHING_URL_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Builds an UploadThing URL from a file key.
     *
     * @param fileKey The file key
     * @return The UploadThing URL, or null if fileKey is invalid
     */
    public static String buildUrl(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }
        return "https://utfs.io/f/" + fileKey;
    }

    /**
     * Builds an UploadThing URL from a file key and app ID.
     *
     * @param fileKey The file key
     * @param appId   The UploadThing app ID
     * @return The UploadThing URL, or null if fileKey is invalid
     */
    public static String buildUrl(String fileKey, String appId) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }
        if (appId != null && !appId.isBlank()) {
            return "https://" + appId + ".ufs.sh/f/" + fileKey;
        }
        return buildUrl(fileKey);
    }
}

