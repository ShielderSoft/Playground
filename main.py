from fastapi import FastAPI, HTTPException, Depends, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.trustedhost import TrustedHostMiddleware
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
from slowapi.middleware import SlowAPIMiddleware
from secure import Secure
import time
import os
import psutil
import logging
from typing import Dict, Any
from pydantic import BaseModel

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize rate limiter
limiter = Limiter(key_func=get_remote_address)

# Initialize FastAPI app
app = FastAPI(
    title="Playground API",
    description="Secure API for repo cloning, scanning and cleansing",
    version="1.0.0",
    docs_url="/docs" if os.getenv("NODE_ENV") != "production" else None,
    redoc_url="/redoc" if os.getenv("NODE_ENV") != "production" else None
)

# Security middleware
secure_headers = Secure()

@app.middleware("http")
async def add_security_headers(request: Request, call_next):
    response = await call_next(request)
    secure_headers.framework.fastapi(response)
    return response

# Add rate limiting middleware
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)
app.add_middleware(SlowAPIMiddleware)

# CORS middleware
allowed_origins = os.getenv("ALLOWED_ORIGINS", "http://localhost:8000").split(",")
app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)

# Trusted host middleware
app.add_middleware(
    TrustedHostMiddleware, 
    allowed_hosts=["localhost", "127.0.0.1", "0.0.0.0"] + allowed_origins
)

# Security bearer token (optional)
security = HTTPBearer(auto_error=False)

class HealthResponse(BaseModel):
    status: str
    timestamp: str
    uptime: float
    version: str
    memory_usage: Dict[str, Any]
    cpu_usage: float

class APIResponse(BaseModel):
    message: str
    features: list
    endpoints: Dict[str, str]

class WelcomeResponse(BaseModel):
    message: str
    version: str
    endpoints: Dict[str, str]

# Store app start time for uptime calculation
app.state.start_time = time.time()

@app.get("/health", response_model=HealthResponse)
@limiter.limit("10/minute")
async def health_check(request: Request):
    """Health check endpoint with system metrics"""
    try:
        # Get system metrics
        memory_info = psutil.virtual_memory()
        cpu_percent = psutil.cpu_percent(interval=1)
        
        uptime = time.time() - app.state.start_time
        
        return HealthResponse(
            status="healthy",
            timestamp=time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
            uptime=uptime,
            version="1.0.0",
            memory_usage={
                "total": memory_info.total,
                "available": memory_info.available,
                "percent": memory_info.percent,
                "used": memory_info.used
            },
            cpu_usage=cpu_percent
        )
    except Exception as e:
        logger.error(f"Health check failed: {str(e)}")
        raise HTTPException(status_code=500, detail="Health check failed")

@app.get("/", response_model=WelcomeResponse)
@limiter.limit("30/minute")
async def root(request: Request):
    """Welcome endpoint with API information"""
    return WelcomeResponse(
        message="Welcome to Playground - A secure containerized environment for repo operations",
        version="1.0.0",
        endpoints={
            "health": "/health",
            "api": "/api",
            "docs": "/docs" if os.getenv("NODE_ENV") != "production" else "disabled"
        }
    )

@app.get("/api", response_model=APIResponse)
@limiter.limit("20/minute")
async def api_info(request: Request):
    """API information endpoint"""
    return APIResponse(
        message="Secure API for repository operations",
        features=[
            "Repository cloning with security validation",
            "Comprehensive security scanning",
            "Code cleansing and sanitization",
            "Vulnerability assessment and reporting",
            "Rate limiting and security headers",
            "Health monitoring and metrics"
        ],
        endpoints={
            "clone": "/api/clone",
            "scan": "/api/scan",
            "cleanse": "/api/cleanse",
            "status": "/api/status"
        }
    )

@app.post("/api/clone")
@limiter.limit("5/minute")
async def clone_repository(
    request: Request,
    credentials: HTTPAuthorizationCredentials = Depends(security)
):
    """Clone repository endpoint (placeholder)"""
    # TODO: Implement secure repository cloning
    return {
        "message": "Repository cloning endpoint",
        "status": "not_implemented",
        "security_note": "This endpoint will validate repository URLs and implement secure cloning"
    }

@app.post("/api/scan")
@limiter.limit("10/minute")
async def scan_repository(
    request: Request,
    credentials: HTTPAuthorizationCredentials = Depends(security)
):
    """Scan repository for vulnerabilities endpoint (placeholder)"""
    # TODO: Implement security scanning
    return {
        "message": "Repository scanning endpoint",
        "status": "not_implemented",
        "security_note": "This endpoint will perform comprehensive security scanning"
    }

@app.post("/api/cleanse")
@limiter.limit("5/minute")
async def cleanse_repository(
    request: Request,
    credentials: HTTPAuthorizationCredentials = Depends(security)
):
    """Cleanse repository endpoint (placeholder)"""
    # TODO: Implement code cleansing
    return {
        "message": "Repository cleansing endpoint",
        "status": "not_implemented",
        "security_note": "This endpoint will sanitize and clean repository code"
    }

@app.get("/api/status")
@limiter.limit("30/minute")
async def get_status(request: Request):
    """Get system and API status"""
    return {
        "api_status": "operational",
        "endpoints_available": 6,
        "security_features": [
            "Rate limiting active",
            "CORS protection enabled",
            "Security headers configured",
            "Health monitoring active"
        ],
        "uptime": time.time() - app.state.start_time
    }

# Error handlers
@app.exception_handler(404)
async def not_found_handler(request: Request, exc):
    return {
        "error": "Not Found",
        "message": "The requested resource was not found",
        "path": str(request.url.path)
    }

@app.exception_handler(500)
async def internal_error_handler(request: Request, exc):
    logger.error(f"Internal server error: {str(exc)}")
    return {
        "error": "Internal Server Error",
        "message": "An internal server error occurred"
    }

# Startup event
@app.on_event("startup")
async def startup_event():
    logger.info("Playground API starting up...")
    logger.info("Security features enabled: Rate limiting, CORS, Security headers")
    
# Shutdown event
@app.on_event("shutdown")
async def shutdown_event():
    logger.info("Playground API shutting down...")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT", 8000)),
        log_level=os.getenv("LOG_LEVEL", "info").lower()
    )
