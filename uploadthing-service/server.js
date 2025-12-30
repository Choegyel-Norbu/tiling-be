import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import multer from 'multer';
import { createUploadthing, createRouteHandler } from 'uploadthing/express';
import { UTApi } from 'uploadthing/server';

// Load environment variables
dotenv.config();

const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(cors({
  origin: process.env.JAVA_BACKEND_URL || 'http://localhost:8082',
  credentials: true
}));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Configure multer for file uploads from Java backend
const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 10 * 1024 * 1024 // 10MB
  }
});

// Initialize UploadThing
const f = createUploadthing();

// Define file router with UploadThing SDK
const fileRouter = {
  /**
   * Image uploader route
   * Accepts: JPEG, PNG, GIF, WebP
   * Max size: 10MB
   */
  imageUploader: f({
    image: {
      maxFileSize: '10MB',
      maxFileCount: 1
    }
  })
    .middleware(async ({ req }) => {
      // Optional: Add authentication/authorization here
      // For now, we'll allow all requests from the Java backend
      return {
        userId: req.headers['x-user-id'] || 'anonymous',
        bookingRef: req.headers['x-booking-ref'] || null
      };
    })
    .onUploadComplete(async ({ metadata, file }) => {
      console.log('Upload complete:', {
        key: file.key,
        name: file.name,
        url: file.url,
        size: file.size,
        metadata
      });
    }),

  /**
   * PDF uploader route
   * Accepts: PDF files
   * Max size: 10MB
   */
  pdfUploader: f({
    pdf: {
      maxFileSize: '10MB',
      maxFileCount: 1
    }
  })
    .middleware(async ({ req }) => {
      return {
        userId: req.headers['x-user-id'] || 'anonymous',
        bookingRef: req.headers['x-booking-ref'] || null
      };
    })
    .onUploadComplete(async ({ metadata, file }) => {
      console.log('PDF upload complete:', {
        key: file.key,
        name: file.name,
        url: file.url,
        size: file.size,
        metadata
      });
    }),

  /**
   * General file uploader (images + PDFs)
   * Accepts: Images and PDFs
   * Max size: 10MB
   */
  fileUploader: f({
    image: { maxFileSize: '10MB', maxFileCount: 1 },
    pdf: { maxFileSize: '10MB', maxFileCount: 1 }
  })
    .middleware(async ({ req }) => {
      return {
        userId: req.headers['x-user-id'] || 'anonymous',
        bookingRef: req.headers['x-booking-ref'] || null
      };
    })
    .onUploadComplete(async ({ metadata, file }) => {
      console.log('File upload complete:', {
        key: file.key,
        name: file.name,
        url: file.url,
        size: file.size,
        metadata
      });
    })
};

// Create UploadThing route handler
app.use(
  '/api/uploadthing',
  createRouteHandler({
    router: fileRouter,
    config: {
      // Optional: Add custom configuration
    }
  })
);

// Initialize UTApi for server-side operations
const utapi = new UTApi({
  secret: process.env.UPLOADTHING_SECRET
});

/**
 * REST API endpoints for Java backend to call
 */

/**
 * Health check endpoint
 */
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'uploadthing-service',
    uploadthingEnabled: !!process.env.UPLOADTHING_SECRET
  });
});

/**
 * Server-side file upload endpoint
 * Java backend sends file as multipart/form-data
 * This endpoint uploads the file to UploadThing and returns the file info
 */
app.post('/api/upload', upload.single('file'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: 'No file provided' });
    }

    const file = req.file;
    const customId = req.body.customId || req.body.bookingRef || null;

    // Create File object from buffer for UploadThing
    // Note: File constructor is available in Node.js 18+
    const fileObj = new File(
      [file.buffer],
      file.originalname,
      { type: file.mimetype }
    );

    // Upload to UploadThing using UTApi
    const uploadResult = await utapi.uploadFiles([fileObj], {
      metadata: {
        originalName: file.originalname,
        customId: customId,
        uploadedBy: req.headers['x-user-id'] || 'backend'
      }
    });

    if (!uploadResult || uploadResult.length === 0) {
      return res.status(500).json({ error: 'Upload failed' });
    }

    const uploadedFile = uploadResult[0];

    res.json({
      success: true,
      data: {
        key: uploadedFile.key,
        url: uploadedFile.url,
        name: uploadedFile.name,
        size: file.size,
        type: file.mimetype
      }
    });

  } catch (error) {
    console.error('Upload error:', error);
    res.status(500).json({
      error: 'Upload failed',
      message: error.message
    });
  }
});

/**
 * Delete file endpoint
 */
app.delete('/api/files/:fileKey', async (req, res) => {
  try {
    const { fileKey } = req.params;

    const result = await utapi.deleteFiles(fileKey);

    res.json({
      success: true,
      message: 'File deleted successfully',
      data: result
    });

  } catch (error) {
    console.error('Delete error:', error);
    res.status(500).json({
      error: 'Delete failed',
      message: error.message
    });
  }
});

/**
 * List files endpoint (optional)
 */
app.get('/api/files', async (req, res) => {
  try {
    // Note: UploadThing doesn't have a direct list endpoint
    // This would need to be implemented based on your requirements
    res.json({
      success: true,
      message: 'List endpoint - implement based on your needs',
      data: []
    });
  } catch (error) {
    console.error('List error:', error);
    res.status(500).json({
      error: 'List failed',
      message: error.message
    });
  }
});

/**
 * Get file info endpoint
 */
app.get('/api/files/:fileKey/info', async (req, res) => {
  try {
    const { fileKey } = req.params;

    // UploadThing file URL format: https://<APP_ID>.ufs.sh/f/<FILE_KEY>
    const fileUrl = `https://${process.env.UPLOADTHING_APP_ID || 'utfs'}.ufs.sh/f/${fileKey}`;

    res.json({
      success: true,
      data: {
        key: fileKey,
        url: fileUrl
      }
    });

  } catch (error) {
    console.error('Get file info error:', error);
    res.status(500).json({
      error: 'Get file info failed',
      message: error.message
    });
  }
});

// Start server
app.listen(PORT, () => {
  console.log(`UploadThing service running on port ${PORT}`);
  console.log(`UploadThing routes available at http://localhost:${PORT}/api/uploadthing`);
  console.log(`REST API available at http://localhost:${PORT}/api`);
});

