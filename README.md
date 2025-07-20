# Playground
codeshuriken playground for repo cloning, scanning and cleansing

## 🔒 Secure Docker Container

This project includes a security-hardened Docker container setup with the following features:

### Security Features
- **Non-root execution** - Container runs as unprivileged user
- **Read-only filesystem** - Prevents runtime modifications
- **Capability dropping** - Minimal Linux capabilities
- **Resource limits** - CPU and memory constraints
- **Network security** - Rate limiting and CORS protection
- **Security headers** - Comprehensive HTTP security headers
- **Python FastAPI** - Modern, fast web framework with built-in security

### Quick Start

1. **Build and run the container:**
   ```bash
   docker-compose up -d
   ```

2. **Check container health:**
   ```bash
   curl http://localhost:8000/health
   ```

3. **View API documentation:**
   ```bash
   curl http://localhost:8000/docs
   ```

4. **View logs:**
   ```bash
   docker-compose logs -f
   ```

### Security Audit

Run the security audit script to verify your setup:
```bash
bash security-audit.sh
```

### Configuration

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Update the `.env` file with your specific configuration

### API Endpoints

- `GET /` - Welcome message and API information
- `GET /health` - Health check endpoint with system metrics
- `GET /api` - API documentation and features
- `GET /docs` - Interactive API documentation (Swagger UI)
- `POST /api/clone` - Repository cloning endpoint
- `POST /api/scan` - Security scanning endpoint
- `POST /api/cleanse` - Code cleansing endpoint

### Documentation

See `DOCKER_SECURITY.md` for detailed security information and best practices.

## Development

### Prerequisites
- Docker and Docker Compose
- Python 3.11+ (for local development)

### Local Development
```bash
pip install -r requirements.txt
python -m uvicorn main:app --reload
```

### Production Deployment
```bash
docker-compose up -d
```
