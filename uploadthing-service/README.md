# UploadThing Node.js Service

This is a Node.js microservice that wraps UploadThing's SDK for use with the Java Spring Boot backend.

## Architecture

The Java backend communicates with this Node.js service via REST API. This service uses UploadThing's official SDK to handle file uploads, providing a clean separation between the Java backend and UploadThing's Node.js-based infrastructure.

## Setup

### Prerequisites

- Node.js 18+ (for File API support)
- npm or yarn

### Installation

1. Navigate to the service directory:
```bash
cd uploadthing-service
```

2. Install dependencies:
```bash
npm install
```

3. Create `.env` file from `.env.example`:
```bash
cp .env.example .env
```

4. Configure your `.env` file:
```env
UPLOADTHING_SECRET=your-uploadthing-secret-key-here
UPLOADTHING_APP_ID=your-app-id
PORT=3001
NODE_ENV=development
JAVA_BACKEND_URL=http://localhost:8082
```

### Running the Service

**Development:**
```bash
npm run dev
```

**Production:**
```bash
npm start
```

The service will start on port 3001 (or the port specified in `.env`).

## API Endpoints

### For Java Backend

#### POST `/api/upload`
Server-side file upload endpoint. Java backend sends files here.

**Request:**
- Content-Type: `multipart/form-data`
- Body: `file` (multipart file)
- Optional: `customId` or `bookingRef` (string)

**Response:**
```json
{
  "success": true,
  "data": {
    "key": "file-key-123",
    "url": "https://app-id.ufs.sh/f/file-key-123",
    "name": "example.jpg",
    "size": 1024000,
    "type": "image/jpeg"
  }
}
```

#### DELETE `/api/files/:fileKey`
Delete a file from UploadThing.

**Response:**
```json
{
  "success": true,
  "message": "File deleted successfully"
}
```

#### GET `/api/files/:fileKey/info`
Get file information.

**Response:**
```json
{
  "success": true,
  "data": {
    "key": "file-key-123",
    "url": "https://app-id.ufs.sh/f/file-key-123"
  }
}
```

#### GET `/health`
Health check endpoint.

**Response:**
```json
{
  "status": "ok",
  "service": "uploadthing-service",
  "uploadthingEnabled": true
}
```

### For Client-Side Uploads

The service exposes UploadThing file router endpoints that clients can use directly:

- `POST /api/uploadthing/imageUploader` - For images (JPEG, PNG, GIF, WebP)
- `POST /api/uploadthing/pdfUploader` - For PDF files
- `POST /api/uploadthing/fileUploader` - For both images and PDFs

Clients should use UploadThing's client SDK to upload to these endpoints.

## Configuration

### Environment Variables

- `UPLOADTHING_SECRET` - Your UploadThing secret key (required)
- `UPLOADTHING_APP_ID` - Your UploadThing app ID (optional, for file URLs)
- `PORT` - Port to run the service on (default: 3001)
- `NODE_ENV` - Environment (development/production)
- `JAVA_BACKEND_URL` - Java backend URL for CORS (default: http://localhost:8082)

## File Router Configuration

The service defines three file routes:

1. **imageUploader**: Accepts images up to 10MB
2. **pdfUploader**: Accepts PDF files up to 10MB
3. **fileUploader**: Accepts both images and PDFs up to 10MB

You can modify these in `server.js` to match your requirements.

## Integration with Java Backend

The Java backend is configured to call this service at `http://localhost:3001` by default. You can change this in the Java application properties:

```properties
app.uploadthing.service-url=http://localhost:3001
```

## Troubleshooting

### Service won't start
- Check that Node.js 18+ is installed: `node --version`
- Verify `.env` file exists and has `UPLOADTHING_SECRET` set
- Check port 3001 is not already in use

### Uploads failing
- Verify `UPLOADTHING_SECRET` is correct
- Check file size is within limits (10MB default)
- Verify file type is allowed (images/PDFs)
- Check service logs for detailed error messages

### Java backend can't connect
- Verify Node.js service is running: `curl http://localhost:3001/health`
- Check `app.uploadthing.service-url` in Java properties matches service URL
- Verify CORS is configured correctly in `server.js`

## Production Deployment

For production:

1. Set `NODE_ENV=production`
2. Use a process manager like PM2:
```bash
npm install -g pm2
pm2 start server.js --name uploadthing-service
pm2 save
pm2 startup
```

3. Configure reverse proxy (nginx/Apache) if needed
4. Set up monitoring and logging
5. Use environment variables for all sensitive configuration

## Security Notes

- Never commit `.env` file to version control
- Keep `UPLOADTHING_SECRET` secure
- Configure CORS appropriately for production
- Consider adding authentication middleware for production use

