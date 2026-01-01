# Railway Deployment Guide - Hybrid Java + Node.js

This guide explains how to deploy your hybrid Java Spring Boot + Node.js application on Railway.

## Architecture Overview

Your application consists of **two separate services**:

1. **Java Spring Boot Backend** (Main API)
   - Port: 8082 (or Railway's `PORT` env var)
   - Handles: REST API, database, business logic
   - Build: Maven (`pom.xml`)

2. **Node.js UploadThing Service** (Microservice)
   - Port: 3001 (or Railway's `PORT` env var)
   - Handles: UploadThing SDK integration, file uploads
   - Build: npm (`package.json` in `uploadthing-service/`)

**Communication**: Java backend → HTTP REST API → Node.js service

---

## Deployment Strategy

Railway supports **multiple services in one project**. You'll deploy both services separately.

### Option 1: Two Separate Services (Recommended)

Deploy each service as a separate Railway service. This provides:
- ✅ Independent scaling
- ✅ Independent deployments
- ✅ Better resource isolation
- ✅ Easier debugging

### Option 2: Monorepo with Nixpacks

Railway can auto-detect and build both services from a monorepo, but separate services are cleaner.

---

## Step-by-Step Deployment

### Step 1: Create Railway Project

1. Go to https://railway.app
2. Create a new project
3. Add **MySQL** service (Railway will auto-inject database variables)

### Step 2: Deploy Java Spring Boot Service

1. **Add a new service** → "Deploy from GitHub repo"
2. **Select your repository** (`tiling-be`)
3. **Root Directory**: Leave as root (`/`)
4. **Build Command**: Railway will auto-detect Maven, but you can set:
   ```bash
   ./mvnw clean package -DskipTests
   ```
5. **Start Command**:
   ```bash
   java -jar target/tiling-be-*.jar
   ```
   Or Railway will auto-detect from `pom.xml`

6. **Environment Variables** (Set in Railway Dashboard):
   ```bash
   # Application Profile
   SPRING_PROFILES_ACTIVE=prod
   
   # Database (Auto-provided by Railway MySQL service)
   # MYSQLHOST, MYSQLPORT, MYSQLUSER, MYSQLPASSWORD, MYSQLDATABASE
   
   # JWT Security
   JWT_SECRET=[generate with: openssl rand -base64 32]
   
   # Email Configuration
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=[gmail-app-password]
   
   # UploadThing API (for direct API calls)
   UPLOADTHING_API_SECRET=sk_live_...
   UPLOADTHING_APP_ID=mj2duw9tdo
   
   # UploadThing Node.js Service URL (CRITICAL!)
   # This will be set after Node.js service is deployed
   APP_UPLOADTHING_SERVICE_URL=https://your-nodejs-service.railway.app
   
   # Google OAuth
   GOOGLE_CLIENT_ID=[your-google-client-id]
   
   # Admin Email
   ADMIN_EMAIL=admin@tilingroofing.com.au
   
   # CORS Origins
   CORS_ORIGINS=https://your-frontend-domain.com
   
   # Server Port (Railway provides PORT automatically)
   # SERVER_PORT will be overridden by Railway's PORT env var
   ```

### Step 3: Deploy Node.js UploadThing Service

1. **Add another new service** → "Deploy from GitHub repo"
2. **Select the same repository** (`tiling-be`)
3. **Root Directory**: Set to `uploadthing-service/`
   - This tells Railway to build from the Node.js service directory
4. **Build Command**: Railway will auto-detect npm, but you can set:
   ```bash
   npm install
   ```
5. **Start Command**:
   ```bash
   npm start
   ```
   Or Railway will auto-detect from `package.json`

6. **Environment Variables** (Set in Railway Dashboard):
   ```bash
   # UploadThing Credentials
   UPLOADTHING_SECRET=sk_live_...  # Same as Java service
   UPLOADTHING_APP_ID=mj2duw9tdo   # Same as Java service
   
   # Port (Railway provides PORT automatically)
   PORT=3001  # Optional, Railway will set this
   
   # Java Backend URL (for CORS)
   JAVA_BACKEND_URL=https://your-java-service.railway.app
   ```

### Step 4: Configure Service-to-Service Communication

After both services are deployed:

1. **Get Node.js Service URL**:
   - Go to Node.js service dashboard
   - Copy the public URL (e.g., `https://uploadthing-service-production.up.railway.app`)

2. **Update Java Service Environment Variable**:
   - Go to Java service dashboard
   - Add/Update: `APP_UPLOADTHING_SERVICE_URL`
   - Value: `https://uploadthing-service-production.up.railway.app`
   - **Important**: Use the public URL, not `localhost`

3. **Update Java Application Properties**:
   You need to add this to `application.properties` or set via env var:
   ```properties
   app.uploadthing.service-url=${APP_UPLOADTHING_SERVICE_URL:http://localhost:3001}
   ```

---

## Configuration Files

### Update `application.properties`

Add the UploadThing service URL configuration:

```properties
# UploadThing Node.js Service Configuration
app.uploadthing.service-url=${APP_UPLOADTHING_SERVICE_URL:http://localhost:3001}
app.uploadthing.enabled=true
```

### Railway Service Detection

Railway uses **Nixpacks** to auto-detect build systems:

- **Java Service**: Detects `pom.xml` → Uses Maven buildpack
- **Node.js Service**: Detects `package.json` in `uploadthing-service/` → Uses Node.js buildpack

---

## Environment Variables Summary

### Java Service Environment Variables

| Variable | Value | Source |
|----------|-------|--------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Manual |
| `MYSQLHOST` | Auto | Railway MySQL service |
| `MYSQLPORT` | Auto | Railway MySQL service |
| `MYSQLUSER` | Auto | Railway MySQL service |
| `MYSQLPASSWORD` | Auto | Railway MySQL service |
| `MYSQLDATABASE` | Auto | Railway MySQL service |
| `JWT_SECRET` | Generated | Manual |
| `MAIL_HOST` | `smtp.gmail.com` | Manual |
| `MAIL_PORT` | `587` | Manual |
| `MAIL_USERNAME` | Your email | Manual |
| `MAIL_PASSWORD` | App password | Manual |
| `UPLOADTHING_API_SECRET` | From dashboard | Manual |
| `UPLOADTHING_APP_ID` | From dashboard | Manual |
| `APP_UPLOADTHING_SERVICE_URL` | Node.js service URL | Manual (after Node.js deploy) |
| `GOOGLE_CLIENT_ID` | From Google Console | Manual |
| `ADMIN_EMAIL` | Your email | Manual |
| `CORS_ORIGINS` | Frontend URL | Manual |
| `PORT` | Auto | Railway (overrides SERVER_PORT) |

### Node.js Service Environment Variables

| Variable | Value | Source |
|----------|-------|--------|
| `UPLOADTHING_SECRET` | From dashboard | Manual |
| `UPLOADTHING_APP_ID` | From dashboard | Manual |
| `JAVA_BACKEND_URL` | Java service URL | Manual (for CORS) |
| `PORT` | Auto | Railway |

---

## Service URLs and Networking

### Railway Service URLs

Railway provides:
- **Public URL**: `https://your-service-name.up.railway.app`
- **Private URL**: Services in the same project can communicate via private networking

### Communication Flow

```
Client → Java Service (Public URL)
         ↓
Java Service → Node.js Service (Public URL or Private Network)
```

**Best Practice**: Use Railway's **private networking** for service-to-service communication when possible.

### Using Private Networking (Optional)

Railway services in the same project can communicate via:
- **Private Hostname**: `uploadthing-service.railway.internal`
- **Private Port**: Use the service's internal port

Update Java service env var:
```bash
APP_UPLOADTHING_SERVICE_URL=http://uploadthing-service.railway.internal:${PORT}
```

**Note**: Private networking may require Railway Pro plan. Check Railway documentation.

---

## Build Configuration

### Java Service Build

Railway will detect `pom.xml` and:
1. Install Java 17+ (from `pom.xml` or `java.version` property)
2. Run `mvnw clean package` (or your custom build command)
3. Execute `java -jar target/tiling-be-*.jar`

### Node.js Service Build

Railway will detect `package.json` in `uploadthing-service/` and:
1. Install Node.js (from `engines.node` or latest LTS)
2. Run `npm install`
3. Execute `npm start` (or your custom start command)

---

## Troubleshooting

### Issue: Java service can't connect to Node.js service

**Solution**:
1. Verify Node.js service is deployed and running
2. Check `APP_UPLOADTHING_SERVICE_URL` is set correctly
3. Use public URL: `https://uploadthing-service.up.railway.app`
4. Check Node.js service logs for errors

### Issue: CORS errors

**Solution**:
1. Set `JAVA_BACKEND_URL` in Node.js service to Java service URL
2. Verify CORS configuration in `uploadthing-service/server.js`

### Issue: Build fails

**Solution**:
1. Check build logs in Railway dashboard
2. Verify root directory is correct:
   - Java: `/` (root)
   - Node.js: `uploadthing-service/`
3. Ensure all dependencies are in `pom.xml` and `package.json`

### Issue: Port conflicts

**Solution**:
- Railway provides `PORT` environment variable automatically
- Don't hardcode ports in code
- Use `process.env.PORT` in Node.js
- Use `server.port=${PORT:8082}` in Spring Boot

---

## Deployment Checklist

- [ ] Create Railway project
- [ ] Add MySQL service (database variables auto-injected)
- [ ] Deploy Java Spring Boot service
  - [ ] Set root directory: `/`
  - [ ] Configure all environment variables
  - [ ] Verify build succeeds
- [ ] Deploy Node.js UploadThing service
  - [ ] Set root directory: `uploadthing-service/`
  - [ ] Configure environment variables
  - [ ] Verify build succeeds
- [ ] Get Node.js service public URL
- [ ] Update Java service: `APP_UPLOADTHING_SERVICE_URL`
- [ ] Update `application.properties` with service URL config
- [ ] Test service-to-service communication
- [ ] Verify file uploads work
- [ ] Test end-to-end booking flow

---

## Alternative: Single Service with Docker

If you prefer a single deployment, you can use Docker Compose:

```dockerfile
# Dockerfile for Java service
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8082
CMD ["java", "-jar", "app.jar"]
```

However, **separate services are recommended** for better scalability and maintainability.

---

## Next Steps

1. Deploy both services to Railway
2. Test the integration
3. Set up custom domains (optional)
4. Configure monitoring and alerts
5. Set up CI/CD for automatic deployments

---

## Additional Resources

- [Railway Documentation](https://docs.railway.app)
- [Railway Multi-Service Projects](https://docs.railway.app/develop/services)
- [Railway Environment Variables](https://docs.railway.app/develop/variables)
- [Railway Private Networking](https://docs.railway.app/develop/private-networking)

