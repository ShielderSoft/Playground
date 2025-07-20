# Use a minimal, security-focused base image
FROM python:3.11-alpine3.19

# Create a non-root user for security
RUN addgroup -g 1001 -S playground && \
    adduser -S playground -u 1001 -G playground

# Set the working directory
WORKDIR /app

# Install security updates and required packages
RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
    git \
    curl \
    ca-certificates \
    gcc \
    musl-dev \
    libffi-dev && \
    rm -rf /var/cache/apk/*

# Copy requirements file first for better layer caching
COPY requirements.txt ./

# Install Python dependencies as root, then change ownership
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY . .

# Change ownership of the app directory to the non-root user
RUN chown -R playground:playground /app

# Switch to non-root user
USER playground

# Create a healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8000/health || exit 1

# Expose port (use non-privileged port)
EXPOSE 8000

# Use exec form of CMD for proper signal handling
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
