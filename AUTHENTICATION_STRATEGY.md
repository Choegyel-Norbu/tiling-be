# Authentication & Authorization Strategy

## Recommended Approach: JWT-Based Stateless Authentication

### Why This Approach?

✅ **Simple**: No session management, stateless architecture  
✅ **Secure**: Google token verification + signed JWT tokens  
✅ **Scalable**: Works across multiple servers, no shared session store  
✅ **Modern**: Industry standard for SPAs and REST APIs  
✅ **Flexible**: Easy to add refresh tokens, role-based access later  

---

## Architecture Overview

```
┌─────────────┐
│  Frontend   │
│  (React)    │
└──────┬──────┘
       │
       │ 1. User clicks "Sign in with Google"
       │ 2. Google OAuth popup/redirect
       │ 3. Google returns ID token
       │
       ▼
┌─────────────────────────────────┐
│  POST /api/auth/google-signin    │
│  Body: { idToken: "..." }        │
└──────────────┬──────────────────┘
               │
               │ 4. Verify Google ID token
               │ 5. Extract user info (email, name, picture)
               │ 6. Create/update User in database
               │ 7. Generate JWT token
               │
               ▼
┌─────────────────────────────────┐
│  Response: {                     │
│    token: "jwt-token",           │
│    user: { id, email, name }    │
│  }                               │
└─────────────────────────────────┘
       │
       │ 8. Store JWT in localStorage/memory
       │ 9. Send JWT in Authorization header
       │
       ▼
┌─────────────────────────────────┐
│  All API Requests               │
│  Header: Authorization: Bearer  │
│           <jwt-token>           │
└─────────────────────────────────┘
```

---

## Flow Details

### 1. Frontend Google Sign-In

```javascript
// Frontend code (example)
import { GoogleAuth } from '@react-oauth/google';

const handleGoogleSignIn = async (credentialResponse) => {
  const idToken = credentialResponse.credential;
  
  // Send to backend
  const response = await fetch('http://localhost:8082/api/auth/google-signin', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idToken })
  });
  
  const { data } = await response.json();
  
  // Store JWT token
  localStorage.setItem('token', data.token);
  localStorage.setItem('user', JSON.stringify(data.user));
};
```

### 2. Backend Token Verification

1. **Receive Google ID Token** from frontend
2. **Verify Token** with Google's public keys
3. **Extract User Info**: email, name, picture, locale
4. **Create/Update User** in database
5. **Generate JWT** with user ID, email, roles
6. **Return JWT + User Info** to frontend

### 3. Subsequent API Requests

```javascript
// Frontend sends JWT in header
fetch('http://localhost:8082/api/bookings', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  }
});
```

### 4. Backend JWT Validation

1. **Extract Token** from `Authorization: Bearer <token>` header
2. **Validate Signature** and expiration
3. **Extract User Info** from JWT claims
4. **Set Authentication** in SecurityContext
5. **Allow/Deny Request** based on authorization rules

---

## Security Features

### ✅ Google ID Token Verification
- Verifies token signature with Google's public keys
- Validates token expiration
- Ensures token is from Google
- Prevents token tampering

### ✅ JWT Token Security
- Signed with HMAC-SHA256 or RSA
- Contains user ID, email, roles
- Short expiration (15-60 minutes)
- Refresh token for long-lived sessions (optional)

### ✅ Authorization
- Role-based access control (RBAC)
- User can only access their own resources
- Admin can access all resources
- Protected endpoints require valid JWT

### ✅ Additional Security
- HTTPS required in production
- CORS configured properly
- Rate limiting on auth endpoints
- Token blacklisting (optional, for logout)

---

## Implementation Components

### 1. **JwtTokenProvider** (`config/JwtTokenProvider.java`)
- Generate JWT tokens
- Validate JWT tokens
- Extract claims from tokens

### 2. **JwtAuthenticationFilter** (`config/JwtAuthenticationFilter.java`)
- Intercept requests
- Extract JWT from Authorization header
- Validate and set authentication

### 3. **SecurityConfig** (`config/SecurityConfig.java`)
- Configure security rules
- Define public vs protected endpoints
- Set up JWT filter
- Configure CORS

### 4. **GoogleTokenVerifier** (`service/GoogleTokenVerifier.java`)
- Verify Google ID tokens
- Extract user information
- Handle verification errors

### 5. **AuthService** (`service/AuthService.java`)
- Handle Google sign-in logic
- Create/update users
- Generate JWT tokens

### 6. **AuthController** (`api/controller/AuthController.java`)
- `POST /api/auth/google-signin` - Sign in with Google
- `GET /api/auth/me` - Get current user
- `POST /api/auth/logout` - Logout (optional)
- `POST /api/auth/refresh` - Refresh token (optional)

---

## Token Structure

### JWT Payload Example:
```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "name": "John Doe",
  "roles": ["USER"],
  "iat": 1234567890,
  "exp": 1234571490
}
```

### Token Claims:
- `sub`: User ID (subject)
- `email`: User email
- `name`: User display name
- `roles`: User roles (e.g., ["USER", "ADMIN"])
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp

---

## API Endpoints

### POST `/api/auth/google-signin`
**Request:**
```json
{
  "idToken": "google-id-token-here"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "jwt-token-here",
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "name": "John Doe",
      "picture": "https://..."
    }
  }
}
```

### GET `/api/auth/me`
**Headers:** `Authorization: Bearer <jwt-token>`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "John Doe",
    "picture": "https://..."
  }
}
```

### POST `/api/auth/logout` (Optional)
**Headers:** `Authorization: Bearer <jwt-token>`

**Response:**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

## Authorization Rules

### Public Endpoints (No Auth Required):
- `POST /api/auth/google-signin`
- `GET /api/health`
- `GET /api-docs/**`
- `GET /swagger-ui/**`

### Protected Endpoints (Auth Required):
- `GET /api/auth/me` - Get current user
- `POST /api/bookings` - Create booking (user's own)
- `GET /api/bookings` - List bookings (user's own)
- `GET /api/bookings/{id}` - Get booking (user's own or admin)
- `PATCH /api/bookings/{id}/status` - Update status (admin only)

### Authorization Logic:
- **Regular Users**: Can only access their own bookings
- **Admin Users**: Can access all bookings and admin endpoints
- **Unauthenticated**: Can only access public endpoints

---

## Configuration

### Required Properties:
```properties
# JWT Configuration
app.jwt.secret=${JWT_SECRET:your-256-bit-secret-key}
app.jwt.expiration=${JWT_EXPIRATION:3600000}  # 1 hour
app.jwt.refresh-expiration=${JWT_REFRESH_EXPIRATION:604800000}  # 7 days

# Google OAuth (for token verification)
app.google.client-id=${GOOGLE_CLIENT_ID:your-google-client-id}
```

### Environment Variables:
```bash
# Production
JWT_SECRET=<strong-random-256-bit-secret>
GOOGLE_CLIENT_ID=<your-google-client-id>
```

---

## Advantages of This Approach

### ✅ Simplicity
- No session storage needed
- No database lookups for authentication
- Stateless - works across servers

### ✅ Security
- Google token verification ensures authenticity
- JWT signed and tamper-proof
- Short expiration reduces risk
- HTTPS required in production

### ✅ Scalability
- No shared session store
- Works with load balancers
- Easy to scale horizontally

### ✅ User Experience
- Fast authentication
- No page reloads needed
- Works with SPAs

---

## Optional Enhancements

### 1. Refresh Tokens
- Long-lived refresh tokens (7-30 days)
- Short-lived access tokens (15-60 minutes)
- Automatic token refresh on frontend

### 2. Token Blacklisting
- Store revoked tokens in Redis/cache
- Check blacklist on each request
- Required for proper logout

### 3. Role-Based Access Control
- Add roles to User entity
- Include roles in JWT
- Check roles in authorization

### 4. Rate Limiting
- Limit auth endpoint requests
- Prevent brute force attacks
- Use Spring Security rate limiting

---

## Migration Path

### Phase 1: Basic Implementation
1. ✅ JWT token generation/validation
2. ✅ Google token verification
3. ✅ User creation/update
4. ✅ Basic authentication endpoints

### Phase 2: Authorization
1. ✅ Protect booking endpoints
2. ✅ User-based resource access
3. ✅ Admin role support

### Phase 3: Enhancements
1. ⏳ Refresh tokens
2. ⏳ Token blacklisting
3. ⏳ Rate limiting
4. ⏳ Audit logging

---

## Testing Strategy

### Unit Tests:
- JWT token generation/validation
- Google token verification
- User creation/update logic

### Integration Tests:
- Authentication flow
- Protected endpoint access
- Authorization rules

### Security Tests:
- Invalid token handling
- Expired token handling
- Unauthorized access attempts

---

## Best Practices

1. **Never store JWT in localStorage** (XSS risk) - Use httpOnly cookies or memory
2. **Use HTTPS in production** - Required for OAuth
3. **Set short token expiration** - 15-60 minutes
4. **Validate tokens on every request** - Don't trust client
5. **Log authentication events** - For security monitoring
6. **Rate limit auth endpoints** - Prevent abuse
7. **Use strong JWT secret** - 256+ bits, random
8. **Rotate secrets periodically** - Security best practice

---

## Next Steps

1. ✅ Review this strategy
2. ⏳ Implement JWT utilities
3. ⏳ Implement Google token verifier
4. ⏳ Create security configuration
5. ⏳ Implement authentication endpoints
6. ⏳ Protect existing endpoints
7. ⏳ Test authentication flow
8. ⏳ Deploy and monitor

