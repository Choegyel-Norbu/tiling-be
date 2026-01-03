# Multi-stage Dockerfile for Java + Node.js application
# Stage 1: Build the Java application
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build the application (skip tests for faster builds)
RUN apt-get update && apt-get install -y maven \
    && mvn clean package -DskipTests \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Stage 2: Runtime image with Java + Node.js
FROM eclipse-temurin:21-jre

# Install Node.js 20.x
RUN apt-get update \
    && apt-get install -y curl gnupg \
    && mkdir -p /etc/apt/keyrings \
    && curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg \
    && echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_20.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list \
    && apt-get update \
    && apt-get install -y nodejs \
    && apt-get clean && rm -rf /var/lib/apt/lists/* \
    && node --version \
    && npm --version

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Copy Node.js scripts and package files
COPY package.json .
COPY package-lock.json* .
COPY uploadthing-upload.js .
COPY uploadthing-delete.js .

# Install Node.js dependencies
RUN npm install --production

# Create uploads directory
RUN mkdir -p /app/uploads

# Expose the application port
EXPOSE 8082

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

