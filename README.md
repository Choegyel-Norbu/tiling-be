# Tiling Roofing Backend

A Spring Boot 3.x RESTful API for managing service bookings, file uploads, and administrative operations.

## Features

- ✅ Create and manage service bookings
- ✅ File upload support (images and PDFs, max 10MB each)
- ✅ Email notifications (customer confirmation, admin notification, status updates)
- ✅ Admin endpoints for listing and managing bookings
- ✅ Date blocking functionality
- ✅ Australian phone and postcode validation
- ✅ Pagination support for list endpoints
- ✅ OpenAPI/Swagger documentation

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **MySQL 8+**
- **Flyway** for database migrations
- **MapStruct** for DTO mapping
- **Thymeleaf** for email templates
- **SpringDoc OpenAPI** for API documentation

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- MySQL 8+ installed and running

## Quick Start

### Development Mode

1. **Create the MySQL database**
   ```sql
   CREATE DATABASE tiling CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Configure database connection** (already set in `application-dev.properties`)
   - Database: `tiling`
   - Username: `root`
   - Password: `ChogyalWp`
   - Host: `localhost:3306`

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```
   Or explicitly with dev profile:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. **Access the application**
   - API: http://localhost:8082/api
   - Swagger UI: http://localhost:8082/swagger-ui.html

### Production Mode

1. **Create the database**
   ```sql
   CREATE DATABASE tiling CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Set environment variables** (optional - defaults are in properties file)
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   export DATABASE_URL=jdbc:mysql://localhost:3306/tiling
   export DATABASE_USERNAME=root
   export DATABASE_PASSWORD=ChogyalWp
   export MAIL_HOST=smtp.gmail.com
   export MAIL_PORT=587
   export MAIL_USERNAME=your_email@gmail.com
   export MAIL_PASSWORD=your_app_password
   export ADMIN_EMAIL=admin@tilingroofing.com.au
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
   ```

## API Endpoints

### Bookings

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bookings` | Create a new booking (multipart/form-data) |
| GET | `/api/bookings/{bookingRef}` | Get booking by reference (e.g., TR-12345) |
| GET | `/api/bookings` | List bookings with pagination and filters |
| PATCH | `/api/bookings/{id}/status` | Update booking status |

### Blocked Dates

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bookings/block-dates` | Block dates from booking |
| GET | `/api/bookings/block-dates` | Get blocked dates |
| GET | `/api/bookings/block-dates/check?date=YYYY-MM-DD` | Check if date is blocked |
| DELETE | `/api/bookings/block-dates/{id}` | Unblock a date |

### Files

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/files/{fileId}` | Download uploaded file |

## API Examples

### Create Booking

```bash
curl -X POST http://localhost:8082/api/bookings \
  -F "serviceId=roof-tiling" \
  -F "jobSize=medium" \
  -F "suburb=Sydney" \
  -F "postcode=2000" \
  -F "description=Full roof tiling required" \
  -F "date=2024-12-25" \
  -F "timeSlot=morning" \
  -F "name=John Doe" \
  -F "email=john@example.com" \
  -F "phone=0412345678" \
  -F "files=@/path/to/image.jpg"
```

### Get Booking

```bash
curl http://localhost:8082/api/bookings/TR-12345
```

### List Bookings (Admin)

```bash
curl "http://localhost:8082/api/bookings?status=pending&page=0&limit=20"
```

### Update Status (Admin)

```bash
curl -X PATCH http://localhost:8082/api/bookings/{id}/status \
  -H "Content-Type: application/json" \
  -d '{"status": "confirmed"}'
```

### Block Dates

```bash
curl -X POST http://localhost:8082/api/bookings/block-dates \
  -H "Content-Type: application/json" \
  -d '{"dates": ["2024-12-25", "2024-12-26"], "reason": "Christmas holidays"}'
```

## Response Format

### Success Response

```json
{
  "success": true,
  "data": { ... },
  "message": "Optional success message"
}
```

### Error Response

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input",
    "details": {
      "email": "Invalid email format"
    }
  }
}
```

## Validation Rules

| Field | Rules |
|-------|-------|
| `serviceId` | Required, max 50 characters |
| `jobSize` | Required, one of: `small`, `medium`, `large` |
| `suburb` | Required, max 100 characters |
| `postcode` | Required, exactly 4 digits (Australian format) |
| `date` | Required, must be a future date (ISO format: YYYY-MM-DD) |
| `timeSlot` | Required, one of: `morning`, `afternoon`, `flexible` |
| `name` | Required, max 200 characters |
| `email` | Required, valid email format |
| `phone` | Required, Australian format (10 digits, starting with 0) |
| `files` | Optional, max 10MB each, types: images (JPEG, PNG, GIF, WebP), PDF |

## Booking Status Flow

```
PENDING → CONFIRMED → IN_PROGRESS → COMPLETED
    ↓         ↓           ↓
 CANCELLED  CANCELLED  CANCELLED
```

## Configuration

Configuration is managed via `application.properties` files with profile-specific overrides:
- `application.properties` - Base configuration
- `application-dev.properties` - Development profile (H2 database)
- `application-prod.properties` - Production profile (MySQL)

All configuration can be customized via environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Application port | 8082 |
| `DATABASE_URL` | Database JDBC URL | jdbc:mysql://localhost:3306/tiling |
| `DATABASE_USERNAME` | Database username | root |
| `DATABASE_PASSWORD` | Database password | ChogyalWp |
| `MAIL_HOST` | SMTP server host | smtp.gmail.com |
| `MAIL_PORT` | SMTP server port | 587 |
| `MAIL_USERNAME` | SMTP username | (required) |
| `MAIL_PASSWORD` | SMTP password | (required) |
| `ADMIN_EMAIL` | Admin notification email | admin@tilingroofing.com.au |
| `FILE_UPLOAD_DIR` | File storage directory | ./uploads |
| `CORS_ORIGINS` | Allowed CORS origins | http://localhost:3000,http://localhost:5173 |

## Project Structure

```
src/main/java/com/tilingroofing/
├── TilingBeApplication.java      # Main application entry
├── api/
│   ├── controller/               # REST controllers (thin)
│   ├── dto/                      # Request/Response DTOs
│   │   ├── request/
│   │   └── response/
│   ├── exception/                # Global exception handler
│   └── mapper/                   # MapStruct mappers
├── common/
│   ├── exception/                # Custom exceptions
│   └── validation/               # Custom validators
├── config/                       # Configuration classes
├── domain/
│   ├── entity/                   # JPA entities
│   ├── enums/                    # Enums
│   └── repository/               # Spring Data repositories
└── service/                      # Business logic services
```

## Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

## Building for Production

```bash
# Build JAR
./mvnw clean package -DskipTests

# Run JAR
java -jar target/tiling-be-1.0.0.jar --spring.profiles.active=prod
```

## Docker (Optional)

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/tiling-be-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t tiling-be .
docker run -p 8082:8082 -e SPRING_PROFILES_ACTIVE=prod tiling-be
```

## License

Proprietary - All rights reserved.

