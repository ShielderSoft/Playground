from fastapi import FastAPI, HTTPException, Request, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.trustedhost import TrustedHostMiddleware
from fastapi.responses import StreamingResponse
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
from slowapi.middleware import SlowAPIMiddleware
from secure import Secure
import time
import os
import psutil
import logging
from typing import Dict, Any, Optional
from pydantic import BaseModel, validator
import asyncio
from pathlib import Path

from utils import (
    clone_repository_to_filesystem,
    analyze_repository,
    stream_file_contents,
    validate_repo_id,
    validate_file_path
)

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize rate limiter
limiter = Limiter(key_func=get_remote_address)

# Initialize FastAPI app
app = FastAPI(
    title="Playground API",
    description="Production-grade API for GitHub repository operations",
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
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)

# Trusted host middleware
app.add_middleware(
    TrustedHostMiddleware, 
    allowed_hosts=["localhost", "127.0.0.1", "0.0.0.0"] + allowed_origins
)

# Pydantic models
class CloneRequest(BaseModel):
    repo_url: str
    branch: Optional[str] = "main"
    
    @validator('repo_url')
    def validate_repo_url(cls, v):
        if not v:
            raise ValueError('Repository URL cannot be empty')
        
        # Basic validation for GitHub URLs
        if v.startswith('http'):
            if 'github.com' not in v:
                raise ValueError('Only GitHub repositories are supported')
        elif '/' in v and len(v.split('/')) == 2:
            # Format: org/repo
            org, repo = v.split('/')
            if not org or not repo:
                raise ValueError('Invalid org/repo format')
        else:
            raise ValueError('Invalid repository URL format')
        
        return v

class CloneResponse(BaseModel):
    repo_id: str

class AnalysisResponse(BaseModel):
    total_lines: int
    file_types: Dict[str, int]
    languages: Dict[str, float]
    structure: Dict[str, Any]

class HealthResponse(BaseModel):
    status: str
    timestamp: str
    uptime: float
    version: str
    memory_usage: Dict[str, Any]
    cpu_usage: float

# Store app start time for uptime calculation
app.state.start_time = time.time()

# Ensure codebase directory exists
CODEBASE_DIR = Path("/app/codebase")
CODEBASE_DIR.mkdir(exist_ok=True)

@app.get("/health", response_model=HealthResponse)
@limiter.limit("30/minute")
async def health_check(request: Request):
    """Health check endpoint with system metrics"""
    try:
        # Get system metrics
        memory_info = psutil.virtual_memory()
        cpu_percent = psutil.cpu_percent(interval=0.1)
        
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

@app.post("/clone", response_model=CloneResponse)
@limiter.limit("10/minute")
async def clone_repo(request: Request, clone_request: CloneRequest):
    """
    Clone a GitHub repository to the local filesystem.
    
    Accepts repo_url (GitHub URL or org/repo format) and optional branch.
    Returns a unique repo_id for accessing the cloned repository.
    """
    try:
        github_token = os.getenv("GITHUB_TOKEN")
        if not github_token:
            logger.error("GitHub token not configured")
            raise HTTPException(
                status_code=500, 
                detail="GitHub authentication not configured"
            )
        
        # Clone repository using background task to avoid blocking
        repo_id = await asyncio.get_event_loop().run_in_executor(
            None,
            clone_repository_to_filesystem,
            clone_request.repo_url,
            clone_request.branch,
            github_token,
            str(CODEBASE_DIR)
        )
        
        logger.info(f"Successfully cloned repository to {repo_id}")
        return CloneResponse(repo_id=repo_id)
        
    except ValueError as e:
        logger.warning(f"Invalid clone request: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Clone operation failed: {str(e)}")
        raise HTTPException(status_code=500, detail="Failed to clone repository")

@app.get("/generate", response_model=AnalysisResponse)
@limiter.limit("20/minute")
async def generate_analysis(
    request: Request,
    repo_id: str = Query(..., description="Repository ID returned from clone endpoint")
):
    """
    Analyze a cloned repository and return comprehensive metadata.
    
    Returns line count, file types, detected languages, and full directory structure.
    """
    try:
        # Validate repo_id format and existence
        if not validate_repo_id(repo_id):
            raise HTTPException(status_code=400, detail="Invalid repository ID format")
        
        repo_path = CODEBASE_DIR / repo_id
        if not repo_path.exists():
            raise HTTPException(status_code=404, detail="Repository not found")
        
        # Analyze repository using background task
        analysis = await asyncio.get_event_loop().run_in_executor(
            None,
            analyze_repository,
            str(repo_path)
        )
        
        return AnalysisResponse(**analysis)
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Analysis failed for repo {repo_id}: {str(e)}")
        raise HTTPException(status_code=500, detail="Failed to analyze repository")

@app.get("/file")
@limiter.limit("50/minute")
async def stream_file(
    request: Request,
    repo_id: str = Query(..., description="Repository ID"),
    path: str = Query(..., description="Relative path to file within repository")
):
    """
    Stream file contents from a cloned repository.
    
    Returns file contents as an efficient streaming response to handle large files.
    """
    try:
        # Validate inputs
        if not validate_repo_id(repo_id):
            raise HTTPException(status_code=400, detail="Invalid repository ID format")
        
        if not validate_file_path(path):
            raise HTTPException(status_code=400, detail="Invalid file path")
        
        repo_path = CODEBASE_DIR / repo_id
        if not repo_path.exists():
            raise HTTPException(status_code=404, detail="Repository not found")
        
        file_path = repo_path / path
        
        # Security check: ensure file is within repository bounds
        try:
            file_path.resolve().relative_to(repo_path.resolve())
        except ValueError:
            raise HTTPException(status_code=400, detail="Path traversal not allowed")
        
        if not file_path.exists():
            raise HTTPException(status_code=404, detail="File not found")
        
        if not file_path.is_file():
            raise HTTPException(status_code=400, detail="Path is not a file")
        
        # Stream file contents
        return StreamingResponse(
            stream_file_contents(str(file_path)),
            media_type="application/octet-stream",
            headers={"Content-Disposition": f"attachment; filename={file_path.name}"}
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"File streaming failed for {repo_id}/{path}: {str(e)}")
        raise HTTPException(status_code=500, detail="Failed to stream file")

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
    logger.info(f"Codebase directory: {CODEBASE_DIR}")
    
    # Verify GitHub token is available (without logging it)
    if not os.getenv("GITHUB_TOKEN"):
        logger.warning("GITHUB_TOKEN environment variable not set")
    else:
        logger.info("GitHub authentication configured")
    
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
