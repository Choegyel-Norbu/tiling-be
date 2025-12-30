# Create Booking API Documentation

## Endpoint

**POST** `/api/bookings`  
**Content-Type:** `multipart/form-data`  
**Authentication:** Required (Bearer Token)

---

## Request Body

### Form Data Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `serviceId` | string | Yes | Service identifier (max 50 characters) |
| `jobSize` | string | Yes | Job size: `small`, `medium`, or `large` |
| `suburb` | string | Yes | Service suburb/location (max 100 characters) |
| `postcode` | string | Yes | Australian postcode (exactly 4 digits) |
| `description` | string | No | Job description (max 2000 characters) |
| `date` | string | Yes | Preferred service date (YYYY-MM-DD, must be future date) |
| `timeSlot` | string | Yes | Time slot: `morning`, `afternoon`, or `flexible` |
| `phone` | string | Yes | Customer phone number (Australian format) |
| `files` | File[] | No | Optional file attachments (max 10MB per file, 50MB total) |

**Note:** User ID is automatically extracted from the JWT token. Do not include `userId` in the request.

**Allowed File Types:**
- Images: `image/jpeg`, `image/png`, `image/gif`, `image/webp`
- Documents: `application/pdf`

---

## Response Body

### Success Response (201 Created)

```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "bookingRef": "TR-12345",
    "status": "pending",
    "serviceId": "roofing-service",
    "jobSize": "large",
    "suburb": "Sydney",
    "postcode": "2000",
    "description": "Roof repair needed",
    "preferredDate": "2024-03-15",
    "timeSlot": "morning",
    "user": {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "email": "john.doe@example.com",
      "name": "John Doe",
      "picture": "https://example.com/photo.jpg",
      "locale": "en-AU"
    },
    "customerPhone": "+61412345678",
    "files": [
      {
        "id": "770e8400-e29b-41d4-a716-446655440002",
        "filename": "roof_photo_TR-12345.jpg",
        "originalFilename": "roof_photo.jpg",
        "url": "/api/files/770e8400-e29b-41d4-a716-446655440002",
        "fileSize": 245678,
        "mimeType": "image/jpeg",
        "uploadedAt": "2024-02-15T10:30:00"
      }
    ],
    "createdAt": "2024-02-15T10:30:00",
    "updatedAt": "2024-02-15T10:30:00"
  },
  "message": "Booking created successfully",
  "error": null
}
```

### Error Response (400 Bad Request)

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {
      "serviceId": "Service ID is required",
      "date": "Date must be in the future",
      "phone": "Invalid Australian phone number format"
    }
  }
}
```

### Error Response (401 Unauthorized)

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Authentication required"
  }
}
```

---

## Example Request

### JavaScript (Fetch)

```javascript
const formData = new FormData();
formData.append('serviceId', 'roofing-service');
formData.append('jobSize', 'large');
formData.append('suburb', 'Sydney');
formData.append('postcode', '2000');
formData.append('description', 'Roof repair needed');
formData.append('date', '2024-03-15');
formData.append('timeSlot', 'morning');
formData.append('phone', '+61412345678');

// Add files if any
files.forEach(file => {
  formData.append('files', file);
});

const response = await fetch('http://localhost:8082/api/bookings', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
});

const result = await response.json();
```

### cURL

```bash
curl -X POST http://localhost:8082/api/bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "serviceId=roofing-service" \
  -F "jobSize=large" \
  -F "suburb=Sydney" \
  -F "postcode=2000" \
  -F "description=Roof repair needed" \
  -F "date=2024-03-15" \
  -F "timeSlot=morning" \
  -F "phone=+61412345678" \
  -F "files=@/path/to/image.jpg"
```
