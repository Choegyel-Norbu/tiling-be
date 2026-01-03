# Fixing 502 Bad Gateway on OPTIONS Requests

## Issue
Getting 502 Bad Gateway on OPTIONS (preflight) requests to `/api/auth/google-signin` even though service shows as "Online" in Railway.

## Root Cause Analysis

A 502 Bad Gateway means Railway's proxy can't get a valid response from your application. This can happen even if the service shows "Online" if:
1. The application crashes when handling OPTIONS requests
2. Code changes haven't been deployed yet
3. There's a configuration issue causing runtime errors

## Solution Steps

### Step 1: Deploy the Latest Code Changes

**CRITICAL**: The code changes we made (removing CorsFilter) need to be deployed!

1. **Commit the changes**:
   ```bash
   git add .
   git commit -m "Fix CORS configuration - remove redundant CorsFilter"
   git push
   ```

2. **Or trigger a redeploy in Railway**:
   - Railway should auto-deploy if connected to Git
   - If not, manually trigger a redeploy in Railway dashboard

3. **Wait for deployment to complete** (check Railway → Deployments)

### Step 2: Verify CORS Environment Variable (Optional but Recommended)

In Railway Dashboard → Variables tab, verify:

**Option A**: Set `CORS_ORIGINS` explicitly (recommended for production)
```
CORS_ORIGINS=https://tiling-fe.vercel.app
```

**Option B**: Use default from `application.properties` (should work if not set)
- Default includes: `http://localhost:3000,http://localhost:5173,https://tiling-fe.vercel.app`

### Step 3: Check Railway Logs for Errors

1. Go to Railway Dashboard
2. Click on your service
3. Go to "Deployments" or "Logs" tab
4. Look for:
   - Application startup errors
   - Errors when OPTIONS requests come in
   - Stack traces or exceptions
   - CORS-related errors

### Step 4: Test if Application is Actually Responding

Try accessing these endpoints to verify the application is responding:

```bash
# Health check (should work)
curl https://tiling-be-production.up.railway.app/actuator/health

# API docs (should work)
curl https://tiling-be-production.up.railway.app/api-docs

# Test OPTIONS directly
curl -X OPTIONS https://tiling-be-production.up.railway.app/api/auth/google-signin \
  -H "Origin: https://tiling-fe.vercel.app" \
  -H "Access-Control-Request-Method: POST" \
  -v
```

If these also return 502, the application might not be running properly despite showing "Online".

### Step 5: Verify Port Configuration

In Railway Settings → Networking, verify:
- Public port is `8082` (or matches your `SERVER_PORT`)
- Application is listening on the correct port

## What We Fixed in Code

1. ✅ Removed redundant `CorsFilter` bean that was conflicting with Spring Security's CORS
2. ✅ CORS is now handled solely by `SecurityConfig.corsConfigurationSource()`
3. ✅ Configuration includes `https://tiling-fe.vercel.app` in allowed origins
4. ✅ OPTIONS method is explicitly allowed

## Expected Behavior After Fix

After deploying, OPTIONS requests should:
- Return `200 OK` (not 502)
- Include CORS headers:
  ```
  Access-Control-Allow-Origin: https://tiling-fe.vercel.app
  Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
  Access-Control-Allow-Credentials: true
  ```

## If Still Getting 502 After Deployment

1. **Check Railway logs** for startup errors
2. **Verify all required environment variables** are set (especially `GOOGLE_CLIENT_ID`, `JWT_SECRET`)
3. **Check if the application actually started** - look for "Started TilingBeApplication" in logs
4. **Try restarting the service** in Railway
5. **Check database connectivity** - if database is down, application might not start

## Quick Test Command

After deployment, test with:

```bash
curl -X OPTIONS https://tiling-be-production.up.railway.app/api/auth/google-signin \
  -H "Origin: https://tiling-fe.vercel.app" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v
```

Expected response:
- Status: `200 OK` or `204 No Content`
- Headers include `Access-Control-Allow-Origin: https://tiling-fe.vercel.app`

