package com.tilingroofing.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service for verifying Google ID tokens.
 * Validates tokens received from the frontend Google Sign-In.
 */
@Service
public class GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${app.google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    /**
     * Verifies a Google ID token and extracts user information.
     *
     * @param idTokenString The Google ID token string from frontend
     * @return GoogleIdToken if valid, null otherwise
     */
    public GoogleIdToken verifyToken(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                log.debug("Google token verified successfully for user: {}", 
                        idToken.getPayload().getEmail());
                return idToken;
            } else {
                log.warn("Google token verification failed: token is null");
                return null;
            }
        } catch (Exception e) {
            log.error("Error verifying Google token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extracts user information from a verified Google ID token.
     *
     * @param idToken Verified Google ID token
     * @return UserInfo containing email, name, picture, and locale
     */
    public UserInfo extractUserInfo(GoogleIdToken idToken) {
        if (idToken == null) {
            return null;
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        
        return UserInfo.builder()
                .email(payload.getEmail())
                .name((String) payload.get("name"))
                .picture((String) payload.get("picture"))
                .locale((String) payload.get("locale"))
                .build();
    }

    /**
     * DTO for user information extracted from Google token.
     */
    @lombok.Data
    @lombok.Builder
    public static class UserInfo {
        private String email;
        private String name;
        private String picture;
        private String locale;
    }
}

