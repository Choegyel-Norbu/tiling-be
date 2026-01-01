# Railway MySQL Connection Troubleshooting

## Common Error: Communications Link Failure

If you see this error:
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
The last packet sent successfully to the server was 0 milliseconds ago.
```

## Root Causes & Solutions

### 1. **Environment Variables Not Set**

Railway automatically provides MySQL environment variables when you add a MySQL service, but you need to verify they're set:

**Check in Railway Dashboard:**
1. Go to your Java service
2. Click on "Variables" tab
3. Verify these variables exist:
   - `MYSQLHOST`
   - `MYSQLPORT`
   - `MYSQLUSER`
   - `MYSQLPASSWORD`
   - `MYSQLDATABASE`

**If missing:**
- Make sure you've added a MySQL service to your Railway project
- Railway should auto-inject these variables
- If not, check that the MySQL service is in the same project

### 2. **SSL Configuration Issue**

Railway MySQL may require SSL. The current configuration uses `useSSL=false` for local dev, but Railway might need SSL enabled.

**Solution:** Set environment variable in Railway:
```bash
MYSQL_USE_SSL=true
```

Or update the connection string to always use SSL for production.

### 3. **Database Not Ready**

The database service might not be fully initialized when your Java app starts.

**Solution:** Add a startup delay or retry logic. Spring Boot will retry, but you can increase the timeout:

```properties
spring.datasource.hikari.connection-timeout=60000  # 60 seconds
```

### 4. **Wrong Host/Port**

Railway MySQL might use a different host format or port.

**Check:**
1. Go to Railway MySQL service dashboard
2. Check the "Connect" tab
3. Verify the connection details match your environment variables

### 5. **Network Connectivity**

Services in the same Railway project should be able to communicate, but verify:

- Both services are in the same Railway project
- MySQL service is running (check status in dashboard)
- No firewall rules blocking the connection

## Quick Fixes

### Fix 1: Verify Environment Variables

Add this to your Java service startup logs (temporary debug):

```bash
# Add to Railway environment variables (temporary)
SPRING_PROFILES_ACTIVE=prod
```

Then check application logs to see what values are being used.

### Fix 2: Use Railway's MYSQL_URL (If Available)

Railway sometimes provides a full `MYSQL_URL` connection string. Check if this variable exists in your Railway dashboard.

If it exists, you can use it directly:

```properties
# In application.properties, add this as an alternative:
spring.datasource.url=${MYSQL_URL:jdbc:mysql://${MYSQLHOST:localhost}:${MYSQLPORT:3306}/${MYSQLDATABASE:tiling}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
```

### Fix 3: Enable SSL for Railway

Railway MySQL typically requires SSL. Update your environment variables:

```bash
# In Railway Dashboard → Variables
MYSQL_USE_SSL=true
```

### Fix 4: Check Connection String Format

Railway MySQL connection strings might need specific formatting. Try:

```properties
# Alternative connection string format
spring.datasource.url=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}?useSSL=true&requireSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8
```

## Step-by-Step Debugging

### Step 1: Check Railway MySQL Service

1. Go to Railway Dashboard
2. Click on your MySQL service
3. Check "Connect" tab
4. Note the connection details:
   - Host
   - Port
   - Database
   - Username
   - Password

### Step 2: Verify Environment Variables

1. Go to your Java service
2. Click "Variables" tab
3. Verify all MySQL variables match the MySQL service details

### Step 3: Check Application Logs

Look for these log messages:
- Database connection attempts
- HikariCP pool initialization
- Any connection errors

### Step 4: Test Connection Manually

If Railway provides a MySQL CLI or connection tool, test the connection manually with the same credentials.

## Updated Configuration

The application.properties has been updated with:
- Configurable SSL via `MYSQL_USE_SSL` environment variable
- Increased connection timeout
- Better connection parameters
- Leak detection for debugging

## Environment Variables Checklist

Make sure these are set in Railway:

```bash
# Database (Auto-provided by Railway MySQL service)
MYSQLHOST=containers-us-west-xxx.railway.app
MYSQLPORT=3306
MYSQLUSER=root
MYSQLPASSWORD=[auto-generated]
MYSQLDATABASE=railway

# Optional: Enable SSL for Railway
MYSQL_USE_SSL=true

# Application
SPRING_PROFILES_ACTIVE=prod
```

## Still Not Working?

1. **Check Railway Status**: Ensure MySQL service is running
2. **Review Logs**: Check both Java service and MySQL service logs
3. **Verify Service Linking**: Ensure services are in the same project
4. **Try Direct Connection**: Use Railway's MySQL connection tool to verify credentials
5. **Contact Support**: Railway support can help debug connection issues

## Alternative: Use Railway's Private Networking

If available (Railway Pro), you can use private networking:

```bash
# Use private hostname
MYSQLHOST=mysql.railway.internal
```

This bypasses public network issues but requires Railway Pro plan.

