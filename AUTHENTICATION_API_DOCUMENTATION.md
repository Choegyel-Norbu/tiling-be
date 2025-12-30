# Authentication API Documentation

## Base URL
```
http://localhost:8082/api/auth
```

## Response Format

All endpoints return responses wrapped in an `ApiResponse` object:

**Success Response:**
```json
{
  "success": true,
  "data": { /* response data */ },
  "message": "Optional success message",
  "error": null
}
```

**Error Response:**
```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "Error message",
    "details": { /* optional error details */ }
  }
}
```

---

## Endpoints

### 1. Sign In with Google

Authenticates a user using Google ID token, creates/updates user in database, and returns JWT token.

**Endpoint:** `POST /api/auth/google-signin`  
**Content-Type:** `application/json`  
**Authentication:** Not required (public endpoint)

#### Request Body

```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6Ij..."
}
```

**Request Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `idToken` | string | Yes | Google ID token obtained from frontend Google Sign-In |

**Validation Rules:**
- `idToken` must not be blank
- `idToken` must be a valid Google ID token

#### Success Response

**Status Code:** `200 OK`

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "name": "John Doe",
      "picture": "https://lh3.googleusercontent.com/a/..."
    }
  },
  "message": "Authentication successful"
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `token` | string | JWT token to be used for authenticated requests |
| `user.id` | string | User's unique identifier (UUID) |
| `user.email` | string | User's email address |
| `user.name` | string | User's display name (may be null) |
| `user.picture` | string | URL to user's profile picture (may be null) |

#### Error Responses

**Status Code:** `400 Bad Request`

**Invalid Token:**
```json
{
  "success": false,
  "error": {
    "code": "AUTHENTICATION_FAILED",
    "message": "Invalid Google ID token"
  }
}
```

**Missing Token:**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {
      "idToken": "Google ID token is required"
    }
  }
}
```

#### Example Request (cURL)

```bash
curl -X POST http://localhost:8082/api/auth/google-signin \
  -H "Content-Type: application/json" \
  -d '{
    "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6Ij..."
  }'
```

#### Example Request (JavaScript)

```javascript
const response = await fetch('http://localhost:8082/api/auth/google-signin', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    idToken: googleIdToken // From Google Sign-In
  })
});

const result = await response.json();

if (result.success) {
  // Store token for future requests
  localStorage.setItem('token', result.data.token);
  localStorage.setItem('user', JSON.stringify(result.data.user));
}
```

---

### 2. Get Current User

Retrieves information about the currently authenticated user.

**Endpoint:** `GET /api/auth/me`  
**Content-Type:** `application/json`  
**Authentication:** Required (JWT token)

#### Request Headers

```
Authorization: Bearer <jwt-token>
```

**Header Fields:**

| Header | Type | Required | Description |
|--------|------|----------|-------------|
| `Authorization` | string | Yes | Bearer token format: `Bearer <jwt-token>` |

#### Success Response

**Status Code:** `200 OK`

```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "name": "John Doe",
    "picture": "https://lh3.googleusercontent.com/a/...",
    "locale": "en"
  }
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | User's unique identifier (UUID) |
| `email` | string | User's email address |
| `name` | string | User's display name (may be null) |
| `picture` | string | URL to user's profile picture (may be null) |
| `locale` | string | User's locale preference (may be null) |

#### Error Responses

**Status Code:** `401 Unauthorized`

**Missing Token:**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "User is not authenticated"
  }
}
```

**Invalid/Expired Token:**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired token"
  }
}
```

**Status Code:** `404 Not Found`

**User Not Found:**
```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "User not found with id: 550e8400-e29b-41d4-a716-446655440000"
  }
}
```

#### Example Request (cURL)

```bash
curl -X GET http://localhost:8082/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### Example Request (JavaScript)

```javascript
const token = localStorage.getItem('token');

const response = await fetch('http://localhost:8082/api/auth/me', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const result = await response.json();

if (result.success) {
  console.log('Current user:', result.data);
} else {
  console.error('Error:', result.error);
}
```

---

## Authentication Flow

### Complete Flow Example

```javascript
// 1. User signs in with Google (frontend)
import { GoogleLogin } from '@react-oauth/google';

const handleGoogleSignIn = async (credentialResponse) => {
  const idToken = credentialResponse.credential;
  
  // 2. Send Google ID token to backend
  const signInResponse = await fetch('http://localhost:8082/api/auth/google-signin', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ idToken })
  });
  
  const signInResult = await signInResponse.json();
  
  if (signInResult.success) {
    // 3. Store JWT token
    const jwtToken = signInResult.data.token;
    localStorage.setItem('token', jwtToken);
    localStorage.setItem('user', JSON.stringify(signInResult.data.user));
    
    // 4. Use JWT token for subsequent requests
    const meResponse = await fetch('http://localhost:8082/api/auth/me', {
      headers: {
        'Authorization': `Bearer ${jwtToken}`
      }
    });
    
    const meResult = await meResponse.json();
    console.log('User info:', meResult.data);
  }
};
```

---

## JWT Token Usage

### Token Format

JWT tokens are in the format:
```
Bearer <token>
```

### Token Structure

The JWT token contains:
- **Header**: Algorithm and token type
- **Payload**: User information (ID, email, name)
- **Signature**: HMAC-SHA256 signature

**Token Payload Example:**
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "name": "John Doe",
  "iat": 1703500800,
  "exp": 1703504400
}
```

### Token Expiration

- **Default expiration**: 1 hour (3600000 milliseconds)
- **Configurable**: Set via `app.jwt.expiration` property
- **Expired tokens**: Return 401 Unauthorized

### Using Token in Requests

All protected endpoints require the JWT token in the Authorization header:

```javascript
// JavaScript example
const token = localStorage.getItem('token');

fetch('http://localhost:8082/api/bookings', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

```bash
# cURL example
curl -X GET http://localhost:8082/api/bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `AUTHENTICATION_FAILED` | 400 | Google ID token is invalid or verification failed |
| `VALIDATION_ERROR` | 400 | Request validation failed (missing/invalid fields) |
| `UNAUTHORIZED` | 401 | User is not authenticated or token is invalid/expired |
| `NOT_FOUND` | 404 | User not found |

---

## Security Notes

1. **Token Storage**: 
   - ⚠️ **Not recommended**: localStorage (XSS vulnerability)
   - ✅ **Recommended**: httpOnly cookies or secure memory storage

2. **HTTPS**: 
   - Required in production
   - OAuth requires HTTPS for security

3. **Token Expiration**: 
   - Tokens expire after 1 hour (default)
   - Implement refresh token mechanism for long-lived sessions

4. **Token Validation**: 
   - Tokens are validated on every request
   - Expired tokens are automatically rejected

---

## Testing

### Test with cURL

```bash
# 1. Sign in (replace with actual Google ID token)
TOKEN=$(curl -X POST http://localhost:8082/api/auth/google-signin \
  -H "Content-Type: application/json" \
  -d '{"idToken":"YOUR_GOOGLE_ID_TOKEN"}' \
  | jq -r '.data.token')

# 2. Get current user
curl -X GET http://localhost:8082/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

### Test with Postman

1. **Sign In Request:**
   - Method: `POST`
   - URL: `http://localhost:8082/api/auth/google-signin`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "idToken": "your-google-id-token"
     }
     ```

2. **Get Current User:**
   - Method: `GET`
   - URL: `http://localhost:8082/api/auth/me`
   - Headers: 
     - `Authorization: Bearer <token-from-signin-response>`

---

## Swagger/OpenAPI

The authentication endpoints are also documented in Swagger UI:

- **Swagger UI**: `http://localhost:8082/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8082/api-docs`

Look for the **Authentication** tag in Swagger UI to see interactive documentation.

---

## Related Endpoints

After authentication, you can use the JWT token to access:

- `POST /api/bookings` - Create booking (requires auth)
- `GET /api/bookings` - List bookings (requires auth)
- `GET /api/bookings/{id}` - Get booking (requires auth)
- `PATCH /api/bookings/{id}/status` - Update status (requires auth)

All these endpoints require the `Authorization: Bearer <token>` header.

