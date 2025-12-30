# Google Sign-In Implementation Checklist

Use this checklist to track your progress implementing Google Sign-In.

## ✅ Phase 1: Dependencies & Configuration

- [x] **Dependencies Added**
  - [x] `spring-boot-starter-security` added to `pom.xml`
  - [x] `spring-boot-starter-oauth2-client` added to `pom.xml`
  - [x] `spring-boot-starter-oauth2-resource-server` added to `pom.xml`
  - [ ] Run `mvn clean install` to download dependencies

- [ ] **Google Cloud Console Setup**
  - [ ] Create/select Google Cloud project
  - [ ] Enable Google+ API or Google Identity Services
  - [ ] Configure OAuth consent screen
    - [ ] App name: Tiling Roofing Services
    - [ ] User support email
    - [ ] Developer contact email
    - [ ] Add scopes: `openid`, `profile`, `email`
  - [ ] Create OAuth 2.0 Client ID
    - [ ] Application type: Web application
    - [ ] Authorized JavaScript origins:
      - [ ] `http://localhost:8082` (dev)
      - [ ] `https://yourdomain.com` (prod)
    - [ ] Authorized redirect URIs:
      - [ ] `http://localhost:8082/login/oauth2/code/google` (dev)
      - [ ] `https://yourdomain.com/login/oauth2/code/google` (prod)
  - [ ] Copy Client ID and Client Secret

- [ ] **Application Properties**
  - [x] OAuth2 configuration added to `application-dev.properties`
  - [x] OAuth2 configuration added to `application-prod.properties`
  - [ ] Set `GOOGLE_CLIENT_ID` in environment or properties file
  - [ ] Set `GOOGLE_CLIENT_SECRET` in environment or properties file
  - [ ] Set `JWT_SECRET` (256-bit minimum) for production

## 🔨 Phase 2: Code Implementation

- [ ] **Security Configuration**
  - [ ] Create `SecurityConfig.java` in `config` package
    - [ ] Configure OAuth2 login
    - [ ] Set up public endpoints (e.g., `/api/auth/**`, `/api/public/**`)
    - [ ] Set up protected endpoints (e.g., `/api/bookings/**`)
    - [ ] Configure CORS integration
    - [ ] Configure CSRF (if needed)
    - [ ] Set up session management or JWT

- [ ] **OAuth2 User Service**
  - [ ] Create `CustomOAuth2UserService.java` in `service` package
    - [ ] Implement `OAuth2UserService<OAuth2UserRequest, OAuth2User>`
    - [ ] Extract user info from Google (email, name, picture, locale)
    - [ ] Check if user exists by email
    - [ ] Create new User entity if not exists
    - [ ] Update existing User if info changed
    - [ ] Return OAuth2User with user info

- [ ] **User Service Enhancement**
  - [ ] Add method to `UserService` (or create if doesn't exist):
    - [ ] `findOrCreateUser(String email, String name, String picture, String locale)`
    - [ ] `updateUserInfo(User user, String name, String picture, String locale)`

- [ ] **Authentication Controller**
  - [ ] Create `AuthController.java` in `api/controller` package
    - [ ] `GET /api/auth/google` - Initiate login
    - [ ] `GET /api/auth/me` - Get current user
    - [ ] `POST /api/auth/logout` - Logout
    - [ ] `GET /api/auth/status` - Check auth status

- [ ] **JWT Utilities** (Optional - for stateless auth)
  - [ ] Create `JwtTokenProvider.java` in `config` or `common` package
    - [ ] Generate JWT token from User
    - [ ] Validate JWT token
    - [ ] Extract user info from token
    - [ ] Get expiration time

- [ ] **JWT Filter** (If using JWT)
  - [ ] Create `JwtAuthenticationFilter.java`
    - [ ] Extract token from Authorization header
    - [ ] Validate token
    - [ ] Set authentication in SecurityContext

## 🔒 Phase 3: Security & Authorization

- [ ] **Protect Booking Endpoints**
  - [ ] Update `BookingController` to require authentication
  - [ ] Add user context to booking creation
  - [ ] Implement user-based filtering (users see only their bookings)
  - [ ] Add admin role check for admin endpoints

- [ ] **User Roles** (Optional)
  - [ ] Add `Role` enum or entity
  - [ ] Add `roles` field to User entity
  - [ ] Create migration for roles
  - [ ] Implement role-based access control

- [ ] **Security Headers**
  - [ ] Configure security headers in SecurityConfig
  - [ ] Add Content Security Policy
  - [ ] Configure HTTPS redirect (production)

## 🧪 Phase 4: Testing

- [ ] **Manual Testing**
  - [ ] Test Google OAuth login flow
  - [ ] Verify user creation in database
  - [ ] Test protected endpoints with authentication
  - [ ] Test logout functionality
  - [ ] Test token expiration (if using JWT)

- [ ] **Integration Tests**
  - [ ] Test OAuth2 authentication endpoint
  - [ ] Test user creation/update logic
  - [ ] Test protected endpoints
  - [ ] Test unauthorized access

- [ ] **Security Testing**
  - [ ] Test with invalid tokens
  - [ ] Test with expired tokens
  - [ ] Test CSRF protection
  - [ ] Test CORS configuration

## 📱 Phase 5: Frontend Integration

- [ ] **Frontend Setup**
  - [ ] Create login page with "Sign in with Google" button
  - [ ] Implement redirect flow or popup flow
  - [ ] Handle OAuth callback
  - [ ] Store authentication token/session
  - [ ] Add token to API requests
  - [ ] Implement logout functionality
  - [ ] Handle token expiration/refresh

## 🚀 Phase 6: Production Deployment

- [ ] **Environment Variables**
  - [ ] Set `GOOGLE_CLIENT_ID` in production
  - [ ] Set `GOOGLE_CLIENT_SECRET` in production
  - [ ] Set `JWT_SECRET` (strong, random, 256+ bits)
  - [ ] Verify HTTPS is enabled
  - [ ] Update Google OAuth redirect URIs for production domain

- [ ] **Security Review**
  - [ ] Review all security configurations
  - [ ] Verify no secrets in code
  - [ ] Check CORS settings
  - [ ] Verify HTTPS redirect
  - [ ] Test production OAuth flow

- [ ] **Monitoring**
  - [ ] Add logging for authentication events
  - [ ] Monitor failed login attempts
  - [ ] Set up alerts for security issues

## 📚 Documentation

- [x] Setup guide created (`GOOGLE_SIGNIN_SETUP.md`)
- [x] Checklist created (this file)
- [ ] API documentation updated with auth endpoints
- [ ] Frontend integration guide created
- [ ] Deployment guide updated

## 🎯 Next Steps After Basic Implementation

1. **Add Refresh Tokens** - For long-lived sessions
2. **Implement Remember Me** - Optional persistent login
3. **Add Social Login Options** - Facebook, GitHub, etc.
4. **User Profile Management** - Update profile, change email
5. **Account Linking** - Link multiple OAuth providers
6. **Two-Factor Authentication** - Additional security layer
7. **Session Management** - View/revoke active sessions

---

## Quick Reference

### Required Files to Create:
1. `SecurityConfig.java` - Security configuration
2. `CustomOAuth2UserService.java` - OAuth2 user handling
3. `AuthController.java` - Authentication endpoints
4. `JwtTokenProvider.java` - JWT utilities (optional)
5. `JwtAuthenticationFilter.java` - JWT filter (optional)

### Key Configuration Values:
- **Client ID**: From Google Cloud Console
- **Client Secret**: From Google Cloud Console
- **Redirect URI**: `/login/oauth2/code/google`
- **Scopes**: `openid`, `profile`, `email`

### Testing URLs:
- **Initiate Login**: `http://localhost:8082/api/auth/google`
- **OAuth Callback**: `http://localhost:8082/login/oauth2/code/google`
- **Current User**: `http://localhost:8082/api/auth/me`

---

**Status**: Dependencies added ✅ | Configuration added ✅ | Code implementation pending ⏳

