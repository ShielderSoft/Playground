# Playground Docker Security Guide

This document outlines the security measures implemented in the Playground Docker container.

## Security Features Implemented

### 1. Container Security
- **Non-root user**: Container runs as user `playground` (UID 1001)
- **Read-only filesystem**: Container filesystem is mounted read-only
- **Dropped capabilities**: All Linux capabilities dropped, only essential ones added back
- **No new privileges**: Prevents privilege escalation
- **Resource limits**: CPU and memory limits enforced

### 2. Image Security
- **Minimal base image**: Uses Alpine Linux (python:3.11-alpine3.19)
- **Security updates**: Automatic security updates applied
- **Layer optimization**: Minimized attack surface through efficient layering
- **No sensitive data**: Secrets and sensitive files excluded via `.dockerignore`

### 3. Network Security
- **Rate limiting**: API endpoints protected with FastAPI rate limiting
- **CORS protection**: Cross-origin requests properly configured
- **Security headers**: Security headers via Secure middleware
- **Input validation**: Pydantic models for request validation

### 4. Application Security
- **FastAPI security**: Built-in security features and middleware
- **Pydantic validation**: Strong input validation and serialization
- **Rate limiting**: Per-endpoint rate limiting with slowapi
- **Security headers**: Comprehensive security headers

## Building and Running

### Build the container:
```bash
docker build -t playground .
```

### Run with Docker Compose (recommended):
```bash
docker-compose up -d
```

### Run manually:
```bash
docker run -d \
  --name playground-container \
  --security-opt no-new-privileges:true \
  --cap-drop ALL \
  --cap-add CHOWN \
  --cap-add SETGID \
  --cap-add SETUID \
  --read-only \
  --tmpfs /tmp:rw,noexec,nosuid,size=100m \
  -p 8000:8000 \
  playground
```

## Security Checklist

- [ ] Use `.env` file for environment variables (not committed to repo)
- [ ] Enable firewall rules for the host
- [ ] Use HTTPS in production with proper certificates
- [ ] Implement authentication and authorization
- [ ] Regular security audits with `npm audit`
- [ ] Monitor container logs and metrics
- [ ] Keep base images updated
- [ ] Use container registry scanning

## Health Monitoring

The container includes a health check endpoint at `/health` that monitors:
- Application responsiveness
- System uptime
- Memory usage
- Version information

## Logging

Logs are configured with:
- JSON format for structured logging
- Log rotation (max 3 files, 10MB each)
- Appropriate log levels based on environment

## Environment Variables

Copy `.env.example` to `.env` and configure:
- `ALLOWED_ORIGINS`: Comma-separated list of allowed origins
- `LOG_LEVEL`: Logging verbosity (debug, info, warn, error)
- Add application-specific variables as needed

## Production Considerations

1. **Secrets Management**: Use Docker secrets or external secret management
2. **Reverse Proxy**: Deploy behind nginx or similar with SSL termination
3. **Container Orchestration**: Consider Kubernetes for production deployments
4. **Monitoring**: Implement comprehensive monitoring and alerting
5. **Backup**: Ensure proper backup strategies for persistent data
