# Google Sign-In Setup Guide

This guide will help you set up Google OAuth2 Sign-In for your Spring Boot application.

## Table of Contents
1. [Dependencies](#dependencies)
2. [Google Cloud Console Setup](#google-cloud-console-setup)
3. [Application Configuration](#application-configuration)
4. [Security Configuration](#security-configuration)
5. [User Service Implementation](#user-service-implementation)
6. [API Endpoints](#api-endpoints)
7. [Frontend Integration](#frontend-integration)
8. [Testing](#testing)

---

## Dependencies

✅ **Already Added to `pom.xml`:**
- `spring-boot-starter-security` - Core Spring Security
- `spring-boot-starter-oauth2-client` - OAuth2 Client support for Google
- `spring-boot-starter-oauth2-resource-server` - For JWT token validation (optional, if using JWT)

---

## Google Cloud Console Setup

### Step 1: Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Note your **Project ID**

### Step 2: Enable Google+ API

1. Navigate to **APIs & Services** > **Library**
2. Search for "Google+ API" or "Google Identity Services"
3. Click **Enable**

### Step 3: Configure OAuth Consent Screen

1. Go to **APIs & Services** > **OAuth consent screen**
2. Choose **External** (unless you have Google Workspace)
3. Fill in required information:
   - **App name**: Tiling Roofing Services
   - **User support email**: Your email
   - **Developer contact information**: Your email
4. Add scopes:
   - `openid`
   - `profile`
   - `email`
5. Save and continue

### Step 4: Create OAuth 2.0 Credentials

1. Go to **APIs & Services** > **Credentials**
2. Click **Create Credentials** > **OAuth client ID**
3. Choose **Web application**
4. Configure:
   - **Name**: Tiling Roofing Backend
   - **Authorized JavaScript origins**:
     - `http://localhost:8082` (for development)
     - `https://yourdomain.com` (for production)
   - **Authorized redirect URIs**:
     - `http://localhost:8082/login/oauth2/code/google` (for development)
     - `https://yourdomain.com/login/oauth2/code/google` (for production)
5. Click **Create**
6. **IMPORTANT**: Copy the **Client ID** and **Client Secret**

---

## Application Configuration

### Add to `application.properties` or `application-dev.properties`:

```properties
# Google OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID:your-client-id-here}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:your-client-secret-here}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

# OAuth2 Provider Configuration
spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v2/userinfo
spring.security.oauth2.client.provider.google.user-name-attribute=email

# JWT Configuration (if using JWT tokens)
app.jwt.secret=${JWT_SECRET:your-jwt-secret-key-minimum-256-bits}
app.jwt.expiration=${JWT_EXPIRATION:86400000}
```

### Environment Variables (Production)

Set these in your production environment:

```bash
GOOGLE_CLIENT_ID=your-actual-client-id
GOOGLE_CLIENT_SECRET=your-actual-client-secret
JWT_SECRET=your-256-bit-secret-key
```

---

## Security Configuration

You'll need to create a Security Configuration class. Here's what you need:

### Required Components:

1. **SecurityConfig.java** - Main security configuration
   - Configure OAuth2 login
   - Set up JWT authentication (optional)
   - Configure CORS
   - Define public vs protected endpoints

2. **OAuth2UserService** - Custom user service
   - Handle OAuth2 user info
   - Create/update User entity in database
   - Map Google user info to your User entity

3. **JWT Utilities** (if using JWT)
   - Generate JWT tokens
   - Validate JWT tokens
   - Extract user info from tokens

---

## User Service Implementation

You'll need a service to handle OAuth2 user information:

### Key Responsibilities:

1. **Extract user info from OAuth2User**
   - Email (required)
   - Name (optional)
   - Picture (optional)
   - Locale (optional)

2. **Create or Update User**
   - Check if user exists by email
   - Create new user if not exists
   - Update existing user info if changed

3. **Return User entity**
   - For session management
   - For JWT token generation

---

## API Endpoints

You'll need these endpoints:

### Authentication Endpoints:

1. **GET `/api/auth/google`** - Initiate Google OAuth login
   - Redirects to Google login page

2. **GET `/api/auth/callback`** - OAuth callback (handled by Spring Security)
   - Automatically handled by Spring Security
   - Redirects to frontend with token/session

3. **GET `/api/auth/me`** - Get current authenticated user
   - Returns current user info

4. **POST `/api/auth/logout`** - Logout
   - Invalidates session/token

### Protected Endpoints:

- All booking endpoints should require authentication
- User can only see their own bookings (or admin sees all)

---

## Frontend Integration

### Option 1: Redirect Flow (Recommended for Web)

1. User clicks "Sign in with Google"
2. Frontend redirects to: `http://localhost:8082/api/auth/google`
3. User authenticates with Google
4. Google redirects back to: `http://localhost:8082/login/oauth2/code/google`
5. Backend processes OAuth, creates/updates user
6. Backend redirects to frontend with token: `http://localhost:3000/auth/callback?token=xxx`
7. Frontend stores token and uses it for API calls

### Option 2: Popup Flow (For SPAs)

1. Frontend opens popup to Google OAuth
2. After authentication, backend returns token via postMessage
3. Frontend receives token and stores it

---

## Testing

### Manual Testing:

1. Start your application
2. Navigate to: `http://localhost:8082/api/auth/google`
3. You should be redirected to Google login
4. After login, you should be redirected back
5. Check database for new user record

### Test Endpoints:

```bash
# Test authentication
curl http://localhost:8082/api/auth/google

# Test current user (requires authentication)
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8082/api/auth/me
```

---

## Next Steps

After setting up the basic OAuth flow, you'll want to:

1. ✅ **Add JWT token generation** - For stateless authentication
2. ✅ **Implement user profile endpoints** - Update user info
3. ✅ **Add role-based access control** - Admin vs regular user
4. ✅ **Secure booking endpoints** - Require authentication
5. ✅ **Add refresh token support** - For long-lived sessions
6. ✅ **Implement logout** - Invalidate tokens/sessions

---

## Security Best Practices

1. **Never commit secrets** - Use environment variables
2. **Use HTTPS in production** - OAuth requires HTTPS
3. **Validate redirect URIs** - Only allow trusted domains
4. **Set token expiration** - Don't use infinite tokens
5. **Implement CSRF protection** - Spring Security handles this
6. **Rate limiting** - Prevent abuse of auth endpoints
7. **Log security events** - Monitor authentication attempts

---

## Troubleshooting

### Common Issues:

1. **"Redirect URI mismatch"**
   - Check that redirect URI in Google Console matches exactly
   - Include protocol (http/https) and port

2. **"Invalid client"**
   - Verify Client ID and Secret are correct
   - Check they're set in environment variables

3. **"Access denied"**
   - Check OAuth consent screen is configured
   - Verify scopes are requested correctly

4. **User not created in database**
   - Check OAuth2UserService implementation
   - Verify database connection
   - Check logs for errors

---

## Additional Resources

- [Spring Security OAuth2 Client Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Spring Boot OAuth2 Tutorial](https://spring.io/guides/tutorials/spring-boot-oauth2/)

---

## Support

If you encounter issues:
1. Check application logs
2. Verify Google Cloud Console configuration
3. Test with Postman/curl
4. Review Spring Security documentation

