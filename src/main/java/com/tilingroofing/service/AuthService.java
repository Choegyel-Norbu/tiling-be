package com.tilingroofing.service;

import com.tilingroofing.domain.entity.User;

/**
 * Service interface for handling authentication operations.
 * Defines the contract for authentication operations.
 */
public interface AuthService {

    /**
     * Authenticates a user with Google ID token.
     * 
     * @param idTokenString Google ID token from frontend
     * @return AuthenticationResult containing JWT token and user info
     */
    AuthenticationResult authenticateWithGoogle(String idTokenString);

    /**
     * Gets user by ID.
     * 
     * @param userId The user ID
     * @return User entity
     */
    User getUserById(Long userId);

    /**
     * Result of authentication operation.
     */
    class AuthenticationResult {
        private String token;
        private User user;

        public AuthenticationResult() {
        }

        public AuthenticationResult(String token, User user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String token;
            private User user;

            public Builder token(String token) {
                this.token = token;
                return this;
            }

            public Builder user(User user) {
                this.user = user;
                return this;
            }

            public AuthenticationResult build() {
                return new AuthenticationResult(token, user);
            }
        }
    }
}
