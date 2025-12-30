# UploadThing Integration Documentation

## Overview

This document describes the UploadThing integration implemented in the Tiling Roofing Backend. UploadThing is a cloud-based file storage service that provides secure, scalable file uploads.

## Architecture

### Design Principles

1. **Microservice Architecture**: Uses a Node.js microservice that wraps UploadThing's official SDK
2. **Hybrid Storage Strategy**: The system supports both UploadThing cloud storage and local filesystem storage
3. **Backward Compatibility**: Existing local storage functionality is preserved as a fallback
4. **Automatic Fallback**: If UploadThing is disabled or fails, the system automatically falls back to local storage
5. **Transparent Integration**: The `FileStorageService` automatically routes to UploadThing when enabled

### Components

**Java Backend:**
1. **UploadThingService**: Communicates with Node.js service via REST API
2. **UploadThingController**: REST endpoints for presigned URL generation
3. **FileStorageService**: Updated to use UploadThing when enabled
4. **UploadThingProperties**: Configuration properties for UploadThing

**Node.js Service:**
1. **Express Server**: REST API server that wraps UploadThing SDK
2. **UploadThing File Router**: Defines file upload routes using UploadThing SDK
3. **UTApi Integration**: Server-side file operations using UploadThing's UTApi

## Setup

### Step 1: Start Node.js Service

1. Navigate to the Node.js service directory:
```bash
cd uploadthing-service
```

2. Install dependencies:
```bash
npm install
```

3. Create `.env` file:
```bash
cp .env.example .env
```

4. Configure `.env` with your UploadThing credentials:
```env
UPLOADTHING_SECRET=your-uploadthing-secret-key-here
UPLOADTHING_APP_ID=your-app-id
PORT=3001
JAVA_BACKEND_URL=http://localhost:8082
```

5. Start the service:
```bash
npm start
# or for development with auto-reload:
npm run dev
```

The Node.js service will run on port 3001 by default.

### Step 2: Configure Java Backend

Add the following to your `application.properties` or environment variables:

```properties
# UploadThing Configuration
# Points to the Node.js service URL
app.uploadthing.service-url=http://localhost:3001
app.uploadthing.enabled=true
app.uploadthing.max-file-size=10485760  # 10MB
app.uploadthing.allowed-types=image/jpeg,image/png,image/gif,image/webp,application/pdf
```

### Environment Variables

For production, use environment variables:

```bash
export UPLOADTHING_SERVICE_URL=http://localhost:3001
export UPLOADTHING_ENABLED=true
```

## API Endpoints

### 1. Generate Presigned URL (Client-Side Uploads)

**Endpoint**: `POST /api/uploadthing/presigned`

**Authentication**: Required (Bearer token)

**Request Body**:
```json
{
  "filename": "example.jpg",
  "fileSize": 1024000,
  "contentType": "image/jpeg",
  "customId": "optional-tracking-id"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Presigned URL generated successfully",
  "data": {
    "uploadUrl": "https://uploadthing.com/upload/...",
    "fileKey": "file-key-123",
    "fileUrl": "https://app-id.ufs.sh/f/file-key-123"
  }
}
```

**Usage**: Clients use the `uploadUrl` to upload files directly to UploadThing, then send the `fileKey` to the backend.

### 2. Health Check

**Endpoint**: `GET /api/uploadthing/health`

**Response**:
```json
{
  "success": true,
  "message": "UploadThing is enabled and configured",
  "data": true
}
```

## File Upload Flow

### Server-Side Upload (Current Implementation)

The existing booking creation endpoint automatically uses UploadThing when enabled:

1. Client sends `POST /api/bookings` with `MultipartFile[]`
2. `FileStorageService.storeFile()` is called
3. If UploadThing is enabled:
   - File is uploaded to UploadThing via `UploadThingService`
   - File metadata (including UploadThing URL) is stored in database
4. If UploadThing is disabled or fails:
   - File is stored locally (fallback)
   - File path is stored in database

### Client-Side Upload (Recommended for Production)

For better performance and reduced server bandwidth:

1. Client calls `POST /api/uploadthing/presigned` with file metadata
2. Backend returns presigned URL
3. Client uploads file directly to UploadThing using presigned URL
4. Client sends file key to backend (e.g., when creating booking)
5. Backend stores file metadata in database

## Database Schema

The `BookingFile` entity stores file metadata:

- `filePath`: 
  - For UploadThing: Contains the UploadThing URL (starts with `http://` or `https://`)
  - For local storage: Contains the local filesystem path
- `filename`: 
  - For UploadThing: Contains the UploadThing file key
  - For local storage: Contains the stored filename

## How It Works

### Flow Diagram

```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│ Java Backend│  HTTP   │ Node.js      │  SDK    │ UploadThing  │
│             │────────>│ Service      │────────>│              │
│             │         │              │         │              │
└─────────────┘         └──────────────┘         └──────────────┘
```

1. **Java Backend** receives file upload request
2. **Java Service** calls Node.js service `POST /api/upload`
3. **Node.js Service** uses UploadThing SDK to upload file
4. **Node.js Service** returns file URL and metadata
5. **Java Service** stores metadata in database

### Benefits of This Architecture

1. **Uses Official SDK**: Node.js service uses UploadThing's official SDK
2. **Separation of Concerns**: Java backend doesn't need to know UploadThing internals
3. **Easy Updates**: Update UploadThing SDK version in Node.js service only
4. **Type Safety**: UploadThing SDK provides type-safe operations
5. **Future-Proof**: Easy to add new UploadThing features as SDK evolves

## Testing

### Enable UploadThing

```properties
app.uploadthing.enabled=true
app.uploadthing.api-key=your-api-key
```

### Disable UploadThing (Fallback to Local)

```properties
app.uploadthing.enabled=false
```

### Test Presigned URL Generation

```bash
curl -X POST http://localhost:8082/api/uploadthing/presigned \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "filename": "test.jpg",
    "fileSize": 1024000,
    "contentType": "image/jpeg"
  }'
```

## Security Considerations

1. **API Key Protection**: 
   - Never expose the API key in client-side code
   - Store in environment variables or secure configuration
   - Use different keys for dev/staging/production

2. **File Validation**:
   - File type validation is enforced
   - File size limits are enforced
   - All uploads require authentication

3. **Access Control**:
   - Presigned URLs are time-limited
   - File access can be restricted via UploadThing settings

## Troubleshooting

### UploadThing Not Working

1. Check if UploadThing is enabled: `GET /api/uploadthing/health`
2. Verify API key is correct
3. Check network connectivity to UploadThing API
4. Review application logs for error messages
5. System will automatically fall back to local storage

### Files Not Accessible

1. Check if file URL is valid (starts with `http://` or `https://`)
2. Verify UploadThing file permissions
3. Check if file was successfully uploaded (check logs)

## Future Enhancements

1. **File Router Pattern**: Implement UploadThing's file router pattern for better organization
2. **Webhook Support**: Add webhook endpoints for upload completion notifications
3. **Batch Operations**: Support batch file uploads/deletions
4. **CDN Integration**: Configure CDN for faster file delivery
5. **Image Processing**: Add image resizing/optimization before upload

## References

- [UploadThing Documentation](https://docs.uploadthing.com)
- [UploadThing API Reference](https://docs.uploadthing.com/api-reference)
- [UploadThing Working with Files](https://docs.uploadthing.com/working-with-files)

