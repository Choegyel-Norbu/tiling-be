#!/usr/bin/env node

const { UTApi } = require('uploadthing/server');
const fs = require('fs');
const path = require('path');

// Get arguments from command line
const args = process.argv.slice(2);
const filePath = args[0];
const field = args[1];
const fileType = args[2];
const originalFilename = args[3];

// Get credentials from environment
// UPLOADTHING_TOKEN should be a base64-encoded JSON: { apiKey, appId, regions }
// UPLOADTHING_SECRET should be the raw API key (sk_live_...)
const secret = process.env.UPLOADTHING_SECRET || process.env.UPLOADTHING_API_SECRET;
const appId = process.env.UPLOADTHING_APP_ID;

if (!secret) {
  console.error(JSON.stringify({
    success: false,
    error: 'UPLOADTHING_SECRET or UPLOADTHING_API_SECRET environment variable is required'
  }));
  process.exit(1);
}

// Check if the secret is already a base64-encoded token
function isBase64Token(str) {
  try {
    const decoded = Buffer.from(str, 'base64').toString('utf-8');
    const parsed = JSON.parse(decoded);
    return parsed.apiKey && parsed.appId && parsed.regions;
  } catch (e) {
    return false;
  }
}

// Create a token from apiKey, appId, and regions
// Common regions: sea1 (Seattle), iad1 (Virginia), fra1 (Frankfurt), syd1 (Sydney), sfo1 (San Francisco)
function createToken(apiKey, appId, regions = null) {
  // Get regions from environment or use default US regions
  const regionsToUse = regions || 
    (process.env.UPLOADTHING_REGIONS ? process.env.UPLOADTHING_REGIONS.split(',') : ['sea1', 'iad1']);
  
  const tokenData = {
    apiKey: apiKey,
    appId: appId,
    regions: regionsToUse
  };
  return Buffer.from(JSON.stringify(tokenData)).toString('base64');
}

// Determine the token to use
let tokenToUse = secret;
if (!isBase64Token(secret)) {
  // It's a raw API key, we need to construct a token
  if (!appId) {
    console.error(JSON.stringify({
      success: false,
      error: 'UPLOADTHING_APP_ID is required when using a raw API secret (sk_live_...). Get your App ID from the UploadThing dashboard.'
    }));
    process.exit(1);
  }
  tokenToUse = createToken(secret, appId);
  console.error('Constructed token from API key and App ID');
}

if (!filePath || !field || !fileType || !originalFilename) {
  console.error(JSON.stringify({
    success: false,
    error: 'Missing required arguments: filePath, field, fileType, originalFilename'
  }));
  process.exit(1);
}

async function uploadFile() {
  try {
    // Read file from disk
    const fileBuffer = fs.readFileSync(filePath);
    
    // Determine MIME type
    let mimeType = 'application/octet-stream';
    if (fileType === 'image') {
      // Try to determine image type from extension
      const ext = originalFilename.split('.').pop().toLowerCase();
      const imageTypes = {
        'jpg': 'image/jpeg',
        'jpeg': 'image/jpeg',
        'png': 'image/png',
        'gif': 'image/gif',
        'webp': 'image/webp'
      };
      mimeType = imageTypes[ext] || 'image/jpeg';
    } else if (fileType === 'pdf') {
      mimeType = 'application/pdf';
    }
    
    // Create File object (Node.js 18+)
    const file = new File([fileBuffer], originalFilename, {
      type: mimeType
    });

    // Initialize UTApi with the token
    // Set environment variable so SDK can read it
    process.env.UPLOADTHING_TOKEN = tokenToUse;
    
    const utapi = new UTApi();

    // Upload file
    const result = await utapi.uploadFiles([file]);

    if (!result || result.length === 0) {
      console.error(JSON.stringify({
        success: false,
        error: 'Upload failed: No result returned'
      }));
      process.exit(1);
    }

    const uploadResult = result[0];

    // Log the actual response structure for debugging (to stderr)
    console.error('UploadThing response:', JSON.stringify(uploadResult, null, 2));

    // UploadThing SDK returns: { data: { key, url, name, size, type }, error: null }
    // Or on error: { data: null, error: { code, message } }
    
    // Check for error in response
    if (uploadResult.error) {
      console.error(JSON.stringify({
        success: false,
        error: uploadResult.error.message || 'Upload failed',
        code: uploadResult.error.code
      }));
      process.exit(1);
    }

    // Get the file data
    const fileData = uploadResult.data;
    if (!fileData) {
      console.error(JSON.stringify({
        success: false,
        error: 'Upload failed: No data in response'
      }));
      process.exit(1);
    }

    // Construct URL if not provided
    let fileUrl = fileData.url;
    if (!fileUrl && fileData.key) {
      // Construct URL: https://utfs.io/f/{key}
      fileUrl = `https://utfs.io/f/${fileData.key}`;
    }

    const response = {
      success: true,
      message: 'File uploaded successfully',
      url: fileUrl,
      fileKey: fileData.key,
      field: field,
      fileName: originalFilename,
      fileSize: fileBuffer.length,
      fileType: fileType
    };

    // Output JSON to stdout (for Java to parse)
    console.log(JSON.stringify(response));

  } catch (error) {
    // Log error to stderr
    console.error(JSON.stringify({
      success: false,
      error: error.message || 'Unknown error occurred',
      stack: error.stack
    }));
    process.exit(1);
  }
}

uploadFile();
