# Railway Environment Variables Configuration

This document lists all environment variables you need to configure in Railway for your Spring Boot application.

## 🔴 Required Variables (Must Set)

### Database Configuration (Auto-provided by Railway MySQL Service)
Railway automatically provides these when you add a MySQL service. **You don't need to set these manually** - Railway injects them automatically:

| Variable | Description | Example Value |
|----------|-------------|---------------|
| `MYSQLHOST` | MySQL hostname | `containers-us-west-xxx.railway.app` |
| `MYSQLPORT` | MySQL port | `3306` |
| `MYSQLUSER` | MySQL username | `root` |
| `MYSQLPASSWORD` | MySQL password | `[auto-generated]` |
| `MYSQLDATABASE` | MySQL database name | `railway` |

**Note:** Railway automatically creates these when you provision a MySQL database. Just add a MySQL service to your project and Railway will inject these variables.

---

### Application Profile
| Variable | Value | Description |
|----------|-------|-------------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Sets the application to production profile |

---

### JWT Security (CRITICAL - Must Set)
| Variable | Value | Description |
|----------|-------|-------------|
| `JWT_SECRET` | `[Generate a strong 256-bit secret]` | Secret key for JWT token signing. **MUST be at least 256 bits (32 characters minimum)**. Generate with: `openssl rand -base64 32` |

**Example:**
```bash
# Generate a secure JWT secret
openssl rand -base64 32
```

---

### Email Configuration (Required for Email Notifications)
| Variable | Value | Description |
|----------|-------|-------------|
| `MAIL_HOST` | `smtp.gmail.com` | SMTP server hostname |
| `MAIL_PORT` | `587` | SMTP server port (587 for TLS) |
| `MAIL_USERNAME` | `your-email@gmail.com` | Your Gmail address |
| `MAIL_PASSWORD` | `[Gmail App Password]` | Gmail App Password (not your regular password). Generate at: https://myaccount.google.com/apppasswords |

**Note:** If using Gmail, you need to:
1. Enable 2-Factor Authentication
2. Generate an App Password at https://myaccount.google.com/apppasswords
3. Use the App Password (16 characters) as `MAIL_PASSWORD`

---

### UploadThing Configuration (Required for File Uploads)
| Variable | Value | Description |
|----------|-------|-------------|
| `UPLOADTHING_API_SECRET` | `sk_live_...` or `sk_test_...` | UploadThing API secret key from https://uploadthing.com/dashboard |
| `UPLOADTHING_APP_ID` | `mj2duw9tdo` | Your UploadThing App ID (found in dashboard URL) |

**How to get:**
1. Go to https://uploadthing.com/dashboard
2. Select your app
3. Copy the API Key (starts with `sk_live_` or `sk_test_`)
4. Copy the App ID from the URL: `https://uploadthing.com/dashboard/{APP_ID}/...`

---

### Google OAuth (Required for Authentication)
| Variable | Value | Description |
|----------|-------|-------------|
| `GOOGLE_CLIENT_ID` | `[Your Google OAuth Client ID]` | Google OAuth 2.0 Client ID from Google Cloud Console |

**How to get:**
1. Go to https://console.cloud.google.com/
2. Create/Select a project
3. Enable Google+ API
4. Create OAuth 2.0 credentials
5. Copy the Client ID

---

## 🟡 Optional Variables (Recommended to Set)

### Server Configuration
| Variable | Value | Description |
|----------|-------|-------------|
| `SERVER_PORT` | `8082` | Application server port (Railway may override with `PORT` env var) |

**Note:** Railway typically provides a `PORT` environment variable. If you need a specific port, check Railway's port configuration.

---

### Admin Email
| Variable | Value | Description |
|----------|-------|-------------|
| `ADMIN_EMAIL` | `admin@tilingroofing.com.au` | Email address for admin notifications |

---

### CORS Configuration
| Variable | Value | Description |
|----------|-------|-------------|
| `CORS_ORIGINS` | `https://your-frontend-domain.com` | Comma-separated list of allowed CORS origins for your frontend |

**Example:**
```
CORS_ORIGINS=https://tilingroofing.com.au,https://www.tilingroofing.com.au
```

---

### JWT Expiration (Optional)
| Variable | Value | Description |
|----------|-------|-------------|
| `JWT_EXPIRATION` | `3600000` | JWT token expiration in milliseconds (default: 1 hour = 3600000ms) |

---

### File Storage (Optional)
| Variable | Value | Description |
|----------|-------|-------------|
| `FILE_UPLOAD_DIR` | `./uploads` | Local file storage directory (if not using UploadThing) |

---

## 📋 Quick Setup Checklist

### Step 1: Add MySQL Service in Railway
- Railway will automatically provide: `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE`
- ✅ No manual configuration needed

### Step 2: Set Required Environment Variables in Railway Dashboard

1. **Application Profile**
   ```
   SPRING_PROFILES_ACTIVE=prod
   ```

2. **JWT Secret** (Generate first!)
   ```bash
   openssl rand -base64 32
   ```
   Then set:
   ```
   JWT_SECRET=[generated-secret]
   ```

3. **Email Configuration**
   ```
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=[gmail-app-password]
   ```

4. **UploadThing**
   ```
   UPLOADTHING_API_SECRET=sk_live_...
   UPLOADTHING_APP_ID=mj2duw9tdo
   ```

5. **Google OAuth**
   ```
   GOOGLE_CLIENT_ID=[your-google-client-id]
   ```

6. **Admin Email**
   ```
   ADMIN_EMAIL=admin@tilingroofing.com.au
   ```

7. **CORS Origins** (Your frontend URL)
   ```
   CORS_ORIGINS=https://your-frontend-domain.com
   ```

---

## 🔒 Security Best Practices

1. **Never commit secrets to Git** - All sensitive values should be in Railway environment variables
2. **Use strong JWT secrets** - Minimum 256 bits (32 characters)
3. **Use Gmail App Passwords** - Never use your regular Gmail password
4. **Rotate secrets regularly** - Especially in production
5. **Use production UploadThing keys** - `sk_live_...` instead of `sk_test_...`

---

## 🧪 Testing Your Configuration

After setting all variables, verify:

1. **Database Connection** - Check application logs for successful database connection
2. **Email Sending** - Test booking creation to verify email notifications
3. **File Uploads** - Test file upload functionality
4. **Authentication** - Test Google Sign-In flow
5. **CORS** - Verify frontend can make API requests

---

## 📝 Notes

- Railway automatically provides database variables when you add a MySQL service
- The `PORT` variable is typically set by Railway automatically
- All environment variables are case-sensitive
- Changes to environment variables require a service restart
- Use Railway's environment variable reference feature to link variables between services

