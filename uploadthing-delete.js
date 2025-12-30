#!/usr/bin/env node

const { UTApi } = require('uploadthing/server');

// Get file keys from command line arguments
const fileKeys = process.argv.slice(2);

// Get credentials from environment
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
  if (!appId) {
    console.error(JSON.stringify({
      success: false,
      error: 'UPLOADTHING_APP_ID is required when using a raw API secret (sk_live_...). Get your App ID from the UploadThing dashboard.'
    }));
    process.exit(1);
  }
  tokenToUse = createToken(secret, appId);
}

if (!fileKeys || fileKeys.length === 0) {
  console.error(JSON.stringify({
    success: false,
    error: 'No file keys provided'
  }));
  process.exit(1);
}

async function deleteFiles() {
  try {
    // Initialize UTApi with the token
    process.env.UPLOADTHING_TOKEN = tokenToUse;
    const utapi = new UTApi();

    // Log what we're about to delete (to stderr so it doesn't interfere with JSON output)
    console.error(`Attempting to delete ${fileKeys.length} file(s): ${fileKeys.join(', ')}`);

    // Delete files - the SDK returns { success: boolean, deletedCount: number }
    const result = await utapi.deleteFiles(fileKeys);

    console.error(`UploadThing deleteFiles result: ${JSON.stringify(result)}`);

    // Check the result structure - UploadThing SDK returns:
    // { success: boolean, deletedCount: number } for successful operations
    // or an array of results for batch operations
    const deletedFiles = [];
    const failedFiles = [];

    if (result) {
      // Handle object response with success property
      if (typeof result === 'object' && 'success' in result) {
        if (result.success) {
          // Check deletedCount to verify files were actually deleted
          const deletedCount = result.deletedCount || 0;
          if (deletedCount > 0) {
            deletedFiles.push(...fileKeys);
            console.error(`Successfully deleted ${deletedCount} files from UploadThing`);
          } else {
            // Success but deletedCount is 0 - files may not exist or already deleted
            console.error(`UploadThing returned success but deletedCount is ${deletedCount}. Files may not exist or were already deleted.`);
            // Still mark as "deleted" since the operation was successful (files don't exist in UploadThing)
            deletedFiles.push(...fileKeys);
          }
        } else {
          // Deletion failed
          failedFiles.push(...fileKeys);
          console.error(`Delete operation returned success: false`);
        }
      }
      // Handle array response (each file has its own result)
      else if (Array.isArray(result)) {
        result.forEach((res, index) => {
          if (res.success || res === true) {
            deletedFiles.push(fileKeys[index]);
          } else {
            failedFiles.push(fileKeys[index]);
          }
        });
      }
      // Handle boolean response
      else if (typeof result === 'boolean') {
        if (result) {
          deletedFiles.push(...fileKeys);
        } else {
          failedFiles.push(...fileKeys);
        }
      }
      // Unknown response format - assume success if we got here without error
      else {
        console.error(`Unknown result format: ${typeof result}, assuming success`);
        deletedFiles.push(...fileKeys);
      }
    } else {
      // No result returned - this might indicate an issue
      console.error('No result returned from deleteFiles');
      failedFiles.push(...fileKeys);
    }

    // Determine overall success
    const overallSuccess = deletedFiles.length > 0 && failedFiles.length === 0;

    // Output JSON to stdout
    console.log(JSON.stringify({
      success: overallSuccess,
      message: overallSuccess 
        ? `Successfully deleted ${deletedFiles.length} file(s)` 
        : `Failed to delete ${failedFiles.length} file(s)`,
      deletedFiles: deletedFiles.length > 0 ? deletedFiles : null,
      failedFiles: failedFiles.length > 0 ? failedFiles : null
    }));

    if (!overallSuccess) {
      process.exit(1);
    }

  } catch (error) {
    // Log detailed error to stderr
    console.error(`Delete error: ${error.message}`);
    console.error(`Stack: ${error.stack}`);
    
    // Output JSON to stdout for Java to parse
    console.log(JSON.stringify({
      success: false,
      error: error.message || 'Unknown error occurred'
    }));
    process.exit(1);
  }
}

deleteFiles();

