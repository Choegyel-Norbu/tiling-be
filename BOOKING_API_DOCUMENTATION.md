# Booking API Documentation

## Base URL
```
http://localhost:8082/api/bookings
```

## Response Format

All endpoints return responses wrapped in an `ApiResponse` object:

```json
{
  "success": true,
  "data": { /* response data */ },
  "message": "Optional success message",
  "error": null
}
```

Error responses:
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

### 1. Create Booking

Creates a new booking with optional file attachments.

**Endpoint:** `POST /api/bookings`  
**Content-Type:** `multipart/form-data`

#### Request Parameters

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `serviceId` | string | Yes | Max 50 chars | Service identifier |
| `jobSize` | string | Yes | `small`, `medium`, `large` | Size of the job |
| `suburb` | string | Yes | Max 100 chars | Service suburb/location |
| `postcode` | string | Yes | Exactly 4 digits | Australian postcode |
| `description` | string | No | Max 2000 chars | Job description/details |
| `date` | string | Yes | Future date (YYYY-MM-DD) | Preferred service date |
| `timeSlot` | string | Yes | `morning`, `afternoon`, `flexible` | Preferred time slot |
| `name` | string | Yes | Max 200 chars | Customer name |
| `email` | string | Yes | Valid email, max 200 chars | Customer email |
| `phone` | string | Yes | Australian format | Customer phone number |
| `files` | File[] | No | Max 10MB per file, 50MB total | Optional file attachments |

**Phone Format:** Australian phone numbers only  
- Valid formats: `+61234567890`, `0234567890`, `234567890`
- Must start with `+61`, `0`, or a digit 2-9

**Allowed File Types:**
- Images: `image/jpeg`, `image/png`, `image/gif`, `image/webp`
- Documents: `application/pdf`
- Max file size: 10MB per file
- Max total request size: 50MB

#### Response

**Status Code:** `201 Created`

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
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "customerPhone": "+61412345678",
    "files": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440001",
        "filename": "roof_photo_12345.jpg",
        "originalFilename": "roof_photo.jpg",
        "url": "/api/files/660e8400-e29b-41d4-a716-446655440001",
        "fileSize": 245678,
        "mimeType": "image/jpeg",
        "uploadedAt": "2024-02-15T10:30:00"
      }
    ],
    "createdAt": "2024-02-15T10:30:00",
    "updatedAt": "2024-02-15T10:30:00"
  },
  "message": "Booking created successfully"
}
```

#### Example (JavaScript/Fetch)

```javascript
const formData = new FormData();
formData.append('serviceId', 'roofing-service');
formData.append('jobSize', 'large');
formData.append('suburb', 'Sydney');
formData.append('postcode', '2000');
formData.append('description', 'Roof repair needed');
formData.append('date', '2024-03-15');
formData.append('timeSlot', 'morning');
formData.append('name', 'John Doe');
formData.append('email', 'john.doe@example.com');
formData.append('phone', '+61412345678');

// Add files if any
const fileInput = document.querySelector('#fileInput');
if (fileInput.files.length > 0) {
  Array.from(fileInput.files).forEach(file => {
    formData.append('files', file);
  });
}

const response = await fetch('http://localhost:8082/api/bookings', {
  method: 'POST',
  body: formData
});

const result = await response.json();
```

#### Error Responses

| Status Code | Error Code | Description |
|-------------|------------|-------------|
| `400 Bad Request` | `VALIDATION_ERROR` | Validation failed (check error details) |
| `400 Bad Request` | `INVALID_DATE` | Date is not in the future |
| `400 Bad Request` | `FILE_TOO_LARGE` | File exceeds size limit |
| `400 Bad Request` | `INVALID_FILE_TYPE` | File type not allowed |

---

### 2. Get Booking by Reference

Retrieves a booking by its reference number (e.g., TR-12345).

**Endpoint:** `GET /api/bookings/{bookingRef}`

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `bookingRef` | string | Yes | Booking reference (e.g., "TR-12345") |

#### Response

**Status Code:** `200 OK`

```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "bookingRef": "TR-12345",
    "status": "confirmed",
    "serviceId": "roofing-service",
    "jobSize": "large",
    "suburb": "Sydney",
    "postcode": "2000",
    "description": "Roof repair needed",
    "preferredDate": "2024-03-15",
    "timeSlot": "morning",
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "customerPhone": "+61412345678",
    "files": [],
    "createdAt": "2024-02-15T10:30:00",
    "updatedAt": "2024-02-15T11:45:00"
  }
}
```

#### Example

```javascript
const bookingRef = 'TR-12345';
const response = await fetch(`http://localhost:8082/api/bookings/${bookingRef}`);
const result = await response.json();

if (result.success) {
  console.log('Booking:', result.data);
}
```

#### Error Responses

| Status Code | Error Code | Description |
|-------------|------------|-------------|
| `404 Not Found` | `BOOKING_NOT_FOUND` | Booking reference not found |

---

### 3. List Bookings (Admin)

Lists all bookings with optional filtering and pagination.

**Endpoint:** `GET /api/bookings`

#### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `status` | string | No | - | Filter by status: `pending`, `confirmed`, `in_progress`, `completed`, `cancelled` |
| `search` | string | No | - | Search by booking ref, customer name, or email |
| `page` | integer | No | `0` | Page number (0-based) |
| `limit` | integer | No | `20` | Page size (max 100) |

#### Response

**Status Code:** `200 OK`

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "bookingRef": "TR-12345",
        "status": "pending",
        "serviceId": "roofing-service",
        "jobSize": "large",
        "suburb": "Sydney",
        "postcode": "2000",
        "preferredDate": "2024-03-15",
        "timeSlot": "morning",
        "customerName": "John Doe",
        "customerEmail": "john.doe@example.com",
        "customerPhone": "+61412345678",
        "createdAt": "2024-02-15T10:30:00",
        "updatedAt": "2024-02-15T10:30:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3,
    "first": true,
    "last": false
  }
}
```

#### Example

```javascript
// Get first page of pending bookings
const params = new URLSearchParams({
  status: 'pending',
  page: '0',
  limit: '20'
});

const response = await fetch(`http://localhost:8082/api/bookings?${params}`);
const result = await response.json();

if (result.success) {
  const { content, totalElements, totalPages } = result.data;
  console.log(`Showing ${content.length} of ${totalElements} bookings`);
  console.log(`Page ${result.data.page + 1} of ${totalPages}`);
}

// Search bookings
const searchParams = new URLSearchParams({
  search: 'john.doe@example.com',
  page: '0',
  limit: '20'
});

const searchResponse = await fetch(`http://localhost:8082/api/bookings?${searchParams}`);
```

---

### 4. Update Booking Status (Admin)

Updates the status of a booking.

**Endpoint:** `PATCH /api/bookings/{id}/status`  
**Content-Type:** `application/json`

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | UUID | Yes | Booking UUID |

#### Request Body

```json
{
  "status": "confirmed"
}
```

| Field | Type | Required | Valid Values |
|-------|------|----------|--------------|
| `status` | string | Yes | `pending`, `confirmed`, `in_progress`, `completed`, `cancelled` |

#### Response

**Status Code:** `200 OK`

```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "bookingRef": "TR-12345",
    "status": "confirmed",
    "serviceId": "roofing-service",
    "jobSize": "large",
    "suburb": "Sydney",
    "postcode": "2000",
    "preferredDate": "2024-03-15",
    "timeSlot": "morning",
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "customerPhone": "+61412345678",
    "createdAt": "2024-02-15T10:30:00",
    "updatedAt": "2024-02-15T11:45:00"
  },
  "message": "Status updated successfully"
}
```

#### Example

```javascript
const bookingId = '550e8400-e29b-41d4-a716-446655440000';

const response = await fetch(`http://localhost:8082/api/bookings/${bookingId}/status`, {
  method: 'PATCH',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    status: 'confirmed'
  })
});

const result = await response.json();
```

#### Error Responses

| Status Code | Error Code | Description |
|-------------|------------|-------------|
| `400 Bad Request` | `VALIDATION_ERROR` | Invalid status value |
| `404 Not Found` | `BOOKING_NOT_FOUND` | Booking ID not found |

---

## Data Models

### BookingResponse

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Unique booking identifier |
| `bookingRef` | string | Human-readable reference (e.g., "TR-12345") |
| `status` | string | Booking status (see enum values below) |
| `serviceId` | string | Service identifier |
| `jobSize` | string | Job size (see enum values below) |
| `suburb` | string | Service location suburb |
| `postcode` | string | Postcode |
| `description` | string | Job description (optional) |
| `preferredDate` | date (YYYY-MM-DD) | Preferred service date |
| `timeSlot` | string | Preferred time slot (see enum values below) |
| `customerName` | string | Customer name |
| `customerEmail` | string | Customer email |
| `customerPhone` | string | Customer phone |
| `files` | FileResponse[] | Array of uploaded files |
| `createdAt` | datetime (ISO 8601) | Creation timestamp |
| `updatedAt` | datetime (ISO 8601) | Last update timestamp |

### FileResponse

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | File identifier |
| `filename` | string | Stored filename |
| `originalFilename` | string | Original filename |
| `url` | string | Download URL path |
| `fileSize` | long | File size in bytes |
| `mimeType` | string | MIME type |
| `uploadedAt` | datetime (ISO 8601) | Upload timestamp |

### PagedResponse<T>

| Field | Type | Description |
|-------|------|-------------|
| `content` | T[] | Array of items for current page |
| `page` | integer | Current page number (0-based) |
| `size` | integer | Page size |
| `totalElements` | long | Total number of elements |
| `totalPages` | integer | Total number of pages |
| `first` | boolean | Is this the first page? |
| `last` | boolean | Is this the last page? |

---

## Enums

### BookingStatus

| Value | Description |
|-------|-------------|
| `pending` | Initial status when booking is created |
| `confirmed` | Booking has been confirmed |
| `in_progress` | Service is in progress |
| `completed` | Service has been completed |
| `cancelled` | Booking has been cancelled |

### JobSize

| Value | Description |
|-------|-------------|
| `small` | Small job |
| `medium` | Medium job |
| `large` | Large job |

### TimeSlot

| Value | Description |
|-------|-------------|
| `morning` | Morning time slot |
| `afternoon` | Afternoon time slot |
| `flexible` | Flexible timing |

---

## Common HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| `200 OK` | Request succeeded |
| `201 Created` | Resource created successfully |
| `400 Bad Request` | Invalid request (validation errors) |
| `404 Not Found` | Resource not found |
| `500 Internal Server Error` | Server error |

---

## Validation Rules

### CreateBookingRequest

- **serviceId**: Required, max 50 characters
- **jobSize**: Required, must be one of: `small`, `medium`, `large`
- **suburb**: Required, max 100 characters
- **postcode**: Required, exactly 4 digits (Australian format)
- **description**: Optional, max 2000 characters
- **date**: Required, must be a future date (YYYY-MM-DD format)
- **timeSlot**: Required, must be one of: `morning`, `afternoon`, `flexible`
- **name**: Required, max 200 characters
- **email**: Required, valid email format, max 200 characters
- **phone**: Required, valid Australian phone number format

### Phone Number Validation

Australian phone numbers must match: `^(\\+61|0)?[2-9]\\d{8}$`

Valid examples:
- `+61412345678`
- `0412345678`
- `234567890`
- `0234567890`

---

## Error Handling Best Practices

1. **Always check the `success` field** in the response before accessing `data`
2. **Handle validation errors** by checking the `error.details` field for field-specific errors
3. **Check HTTP status codes** for proper error handling
4. **Display user-friendly messages** from `error.message` field

Example error handling:

```javascript
try {
  const response = await fetch(url, options);
  const result = await response.json();
  
  if (!result.success) {
    if (result.error) {
      console.error(`Error ${result.error.code}: ${result.error.message}`);
      // Handle specific error codes
      if (result.error.code === 'VALIDATION_ERROR') {
        // Display validation errors to user
      }
    }
    return;
  }
  
  // Use result.data
} catch (error) {
  console.error('Network error:', error);
}
```

---

## Swagger UI

Interactive API documentation is available at:
```
http://localhost:8082/swagger-ui.html
```

OpenAPI JSON specification:
```
http://localhost:8082/api-docs
```

---

## Notes for Frontend Developers

1. **Date Format**: Always use `YYYY-MM-DD` format for dates (ISO 8601 date)
2. **DateTime Format**: All timestamps are in ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`
3. **File Uploads**: Use `multipart/form-data` for file uploads, not JSON
4. **Pagination**: Page numbers are 0-based (first page is 0)
5. **Maximum Page Size**: Limit is capped at 100 items per page
6. **CORS**: CORS is enabled for `http://localhost:3000` and `http://localhost:5173`
7. **Booking Reference**: Format is `TR-{number}` (e.g., `TR-12345`)

---

## Testing

### cURL Examples

**Create Booking:**
```bash
curl -X POST http://localhost:8082/api/bookings \
  -F "serviceId=roofing-service" \
  -F "jobSize=large" \
  -F "suburb=Sydney" \
  -F "postcode=2000" \
  -F "date=2024-03-15" \
  -F "timeSlot=morning" \
  -F "name=John Doe" \
  -F "email=john.doe@example.com" \
  -F "phone=+61412345678" \
  -F "files=@/path/to/image.jpg"
```

**Get Booking:**
```bash
curl http://localhost:8082/api/bookings/TR-12345
```

**List Bookings:**
```bash
curl "http://localhost:8082/api/bookings?status=pending&page=0&limit=20"
```

**Update Status:**
```bash
curl -X PATCH http://localhost:8082/api/bookings/550e8400-e29b-41d4-a716-446655440000/status \
  -H "Content-Type: application/json" \
  -d '{"status":"confirmed"}'
```

