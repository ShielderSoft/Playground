"""
Production-grade utility functions for GitHub repository operations.
Handles cloning, analysis, and file streaming with security and performance focus.
"""

import os
import re
import uuid
import shutil
import subprocess
import asyncio
import stat
from pathlib import Path
from typing import Dict, Any, AsyncGenerator, Optional
import logging
from collections import defaultdict
import mimetypes

import git
from git import Repo

logger = logging.getLogger(__name__)

# File extension to language mapping for better detection
LANGUAGE_EXTENSIONS = {
    '.py': 'Python',
    '.js': 'JavaScript',
    '.ts': 'TypeScript',
    '.java': 'Java',
    '.cpp': 'C++',
    '.cc': 'C++',
    '.cxx': 'C++',
    '.c': 'C',
    '.h': 'C/C++',
    '.hpp': 'C++',
    '.cs': 'C#',
    '.php': 'PHP',
    '.rb': 'Ruby',
    '.go': 'Go',
    '.rs': 'Rust',
    '.swift': 'Swift',
    '.kt': 'Kotlin',
    '.scala': 'Scala',
    '.sh': 'Shell',
    '.bash': 'Bash',
    '.zsh': 'Zsh',
    '.ps1': 'PowerShell',
    '.sql': 'SQL',
    '.html': 'HTML',
    '.htm': 'HTML',
    '.css': 'CSS',
    '.scss': 'SCSS',
    '.sass': 'Sass',
    '.less': 'Less',
    '.xml': 'XML',
    '.json': 'JSON',
    '.yaml': 'YAML',
    '.yml': 'YAML',
    '.toml': 'TOML',
    '.ini': 'INI',
    '.cfg': 'Config',
    '.conf': 'Config',
    '.dockerfile': 'Docker',
    '.r': 'R',
    '.R': 'R',
    '.m': 'MATLAB/Objective-C',
    '.pl': 'Perl',
    '.lua': 'Lua',
    '.dart': 'Dart',
    '.vue': 'Vue',
    '.jsx': 'JSX',
    '.tsx': 'TSX',
}

# Binary file extensions to exclude from line counting
BINARY_EXTENSIONS = {
    '.exe', '.dll', '.so', '.dylib', '.bin', '.obj', '.o', '.a', '.lib',
    '.jpg', '.jpeg', '.png', '.gif', '.bmp', '.svg', '.ico', '.webp',
    '.mp3', '.wav', '.ogg', '.mp4', '.avi', '.mov', '.wmv', '.flv',
    '.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx',
    '.zip', '.rar', '.7z', '.tar', '.gz', '.bz2', '.xz',
    '.ttf', '.otf', '.woff', '.woff2', '.eot',
    '.class', '.jar', '.war', '.ear',
    '.pyc', '.pyo', '.pyd',
    '.node', '.wasm'
}

def safe_rmtree(path: Path) -> None:
    """
    Safely remove a directory tree, handling Windows permission issues.
    
    Args:
        path: Path to directory to remove
    """
    def handle_remove_readonly(func, path, exc):
        """Handle removal of read-only files on Windows."""
        if os.path.exists(path):
            os.chmod(path, stat.S_IWRITE)
            func(path)
    
    try:
        if path.exists():
            shutil.rmtree(str(path), onerror=handle_remove_readonly)
    except Exception as e:
        logger.warning(f"Failed to clean up directory {path}: {str(e)}")
        # Try alternative cleanup method for Windows
        try:
            if os.name == 'nt':  # Windows
                subprocess.run(['rmdir', '/s', '/q', str(path)], 
                             shell=True, check=False, capture_output=True)
        except Exception as e2:
            logger.error(f"Alternative cleanup also failed: {str(e2)}")

def validate_repo_id(repo_id: str) -> bool:
    """
    Validate that repo_id is a valid UUID format.
    
    Args:
        repo_id: The repository ID to validate
        
    Returns:
        bool: True if valid UUID format, False otherwise
    """
    try:
        uuid.UUID(repo_id)
        return True
    except ValueError:
        return False

def validate_file_path(file_path: str) -> bool:
    """
    Validate file path to prevent path traversal attacks.
    
    Args:
        file_path: The file path to validate
        
    Returns:
        bool: True if path is safe, False otherwise
    """
    if not file_path:
        return False
    
    # Check for path traversal patterns
    dangerous_patterns = ['../', '..\\', '/../', '\\..\\']
    if any(pattern in file_path for pattern in dangerous_patterns):
        return False
    
    # Check for absolute paths
    if file_path.startswith(('/', '\\')):
        return False
    
    # Check for drive letters on Windows
    if re.match(r'^[a-zA-Z]:', file_path):
        return False
    
    return True

def normalize_repo_url(repo_url: str) -> str:
    """
    Normalize repository URL to HTTPS format for cloning.
    
    Args:
        repo_url: Repository URL in various formats
        
    Returns:
        str: Normalized HTTPS URL
    """
    if repo_url.startswith('http'):
        return repo_url
    elif '/' in repo_url and len(repo_url.split('/')) == 2:
        # Format: org/repo
        return f"https://github.com/{repo_url}.git"
    else:
        raise ValueError("Invalid repository URL format")

def clone_repository_to_filesystem(
    repo_url: str, 
    branch: str, 
    github_token: str, 
    base_path: str
) -> str:
    """
    Clone a GitHub repository to the local filesystem with authentication.
    
    Args:
        repo_url: GitHub repository URL
        branch: Git branch to clone
        github_token: GitHub authentication token
        base_path: Base directory for cloned repositories
        
    Returns:
        str: UUID of the cloned repository
        
    Raises:
        ValueError: For invalid input parameters
        RuntimeError: For git operation failures
    """
    repo_path = None
    try:
        # Generate unique identifier for this repository
        repo_id = str(uuid.uuid4())
        repo_path = Path(base_path) / repo_id
        
        # Ensure base path exists with proper permissions
        base_path_obj = Path(base_path)
        base_path_obj.mkdir(exist_ok=True)
        
        # Normalize repository URL
        normalized_url = normalize_repo_url(repo_url)
        
        # Add authentication to URL
        if normalized_url.startswith('https://github.com/'):
            auth_url = normalized_url.replace(
                'https://github.com/', 
                f'https://{github_token}@github.com/'
            )
        else:
            raise ValueError("Only GitHub repositories are supported")
        
        logger.info(f"Cloning repository to {repo_path}")
        
        # First, try to get the default branch if the specified branch fails
        def try_clone_with_branch(target_branch):
            """Helper function to try cloning with a specific branch."""
            try:
                cmd = [
                    'git', 'clone', 
                    '--depth', '1', 
                    '--single-branch', 
                    '--branch', target_branch,
                    auth_url, 
                    str(repo_path)
                ]
                
                result = subprocess.run(
                    cmd, 
                    capture_output=True, 
                    text=True, 
                    timeout=300,  # 5 minute timeout
                    check=True
                )
                
                logger.info(f"Clone completed using subprocess method with branch '{target_branch}'")
                return True
                
            except subprocess.CalledProcessError as e:
                if "not found in upstream origin" in e.stderr:
                    logger.warning(f"Branch '{target_branch}' not found in repository")
                    return False
                else:
                    # Re-raise for other git errors
                    raise e
        
        # Try the requested branch first
        clone_successful = False
        final_branch = branch
        
        try:
            clone_successful = try_clone_with_branch(branch)
        except subprocess.CalledProcessError:
            # If subprocess fails entirely, we'll try GitPython later
            pass
        
        # If the requested branch doesn't exist, try common default branches
        if not clone_successful:
            logger.info(f"Branch '{branch}' not found, trying default branches...")
            default_branches = ['master', 'main', 'develop', 'dev']
            
            # Remove the originally requested branch from defaults to avoid duplicate attempts
            if branch in default_branches:
                default_branches.remove(branch)
            
            for default_branch in default_branches:
                try:
                    if try_clone_with_branch(default_branch):
                        clone_successful = True
                        final_branch = default_branch
                        logger.info(f"Successfully cloned using default branch '{default_branch}'")
                        break
                except subprocess.CalledProcessError:
                    continue
        
        # If subprocess method failed entirely, try GitPython as fallback
        if not clone_successful:
            logger.warning("Subprocess git clone failed, trying GitPython...")
            try:
                repo = Repo.clone_from(
                    auth_url,
                    str(repo_path),
                    branch=branch,
                    depth=1,
                    single_branch=True
                )
                logger.info("Clone completed using GitPython method")
                clone_successful = True
            except git.exc.GitCommandError as git_error:
                # Try with default branches using GitPython
                if "not found in upstream origin" in str(git_error):
                    logger.info("Trying default branches with GitPython...")
                    default_branches = ['master', 'main', 'develop', 'dev']
                    if branch in default_branches:
                        default_branches.remove(branch)
                    
                    for default_branch in default_branches:
                        try:
                            if repo_path.exists():
                                safe_rmtree(repo_path)
                            repo = Repo.clone_from(
                                auth_url,
                                str(repo_path),
                                branch=default_branch,
                                depth=1,
                                single_branch=True
                            )
                            final_branch = default_branch
                            logger.info(f"Successfully cloned using GitPython with branch '{default_branch}'")
                            clone_successful = True
                            break
                        except git.exc.GitCommandError:
                            continue
                
                if not clone_successful:
                    raise git_error
        
        if not clone_successful:
            raise RuntimeError(f"Failed to clone repository: No valid branches found")
        
        # Remove git directory to save space and prevent accidental operations
        git_dir = repo_path / '.git'
        if git_dir.exists():
            safe_rmtree(git_dir)
        
        logger.info(f"Successfully cloned repository {repo_url} (branch: {final_branch}) to {repo_id}")
        return repo_id
        
    except Exception as e:
        logger.error(f"Unexpected error during clone: {str(e)}")
        # Clean up partial clone
        if repo_path and repo_path.exists():
            safe_rmtree(repo_path)
        raise RuntimeError(f"Clone operation failed: {str(e)}")

def count_lines_in_file(file_path: Path) -> int:
    """
    Count lines in a text file, handling various encodings gracefully.
    
    Args:
        file_path: Path to the file
        
    Returns:
        int: Number of lines in the file
    """
    try:
        # Skip binary files
        if file_path.suffix.lower() in BINARY_EXTENSIONS:
            return 0
        
        # Try different encodings
        encodings = ['utf-8', 'utf-16', 'latin1', 'cp1252']
        
        for encoding in encodings:
            try:
                with open(file_path, 'r', encoding=encoding) as f:
                    return sum(1 for _ in f)
            except (UnicodeDecodeError, UnicodeError):
                continue
        
        # If all encodings fail, assume binary file
        return 0
        
    except Exception as e:
        logger.warning(f"Failed to count lines in {file_path}: {str(e)}")
        return 0

def build_directory_structure(path: Path, max_depth: int = 10) -> Dict[str, Any]:
    """
    Build a nested JSON representation of directory structure.
    
    Args:
        path: Root path to analyze
        max_depth: Maximum depth to traverse
        
    Returns:
        Dict: Nested structure representation
    """
    def _build_structure(current_path: Path, current_depth: int) -> Dict[str, Any]:
        if current_depth > max_depth:
            return {"type": "directory", "truncated": True}
        
        structure = {}
        
        try:
            items = sorted(current_path.iterdir(), key=lambda x: (x.is_file(), x.name.lower()))
            
            for item in items:
                # Skip hidden files and common ignore patterns
                if item.name.startswith('.') and item.name not in {'.gitignore', '.env.example'}:
                    continue
                
                if item.name in {'node_modules', '__pycache__', '.git', 'venv', '.venv'}:
                    continue
                
                if item.is_file():
                    structure[item.name] = {
                        "type": "file",
                        "size": item.stat().st_size,
                        "extension": item.suffix.lower()
                    }
                elif item.is_dir():
                    structure[item.name] = {
                        "type": "directory",
                        "children": _build_structure(item, current_depth + 1)
                    }
                    
        except PermissionError:
            logger.warning(f"Permission denied accessing {current_path}")
        
        return structure
    
    return _build_structure(path, 0)

def analyze_repository(repo_path: str) -> Dict[str, Any]:
    """
    Perform comprehensive analysis of a cloned repository.
    
    Args:
        repo_path: Path to the repository directory
        
    Returns:
        Dict: Analysis results including line count, file types, languages, and structure
    """
    path = Path(repo_path)
    
    if not path.exists():
        raise ValueError(f"Repository path does not exist: {repo_path}")
    
    total_lines = 0
    file_types = defaultdict(int)
    language_lines = defaultdict(int)
    
    logger.info(f"Analyzing repository at {repo_path}")
    
    # Walk through all files
    for file_path in path.rglob('*'):
        if file_path.is_file():
            # Skip hidden files and common ignore patterns
            if any(part.startswith('.') for part in file_path.parts):
                if file_path.name not in {'.gitignore', '.env.example', '.dockerignore'}:
                    continue
            
            # Skip common ignore directories
            if any(ignore_dir in file_path.parts for ignore_dir in {
                'node_modules', '__pycache__', '.git', 'venv', '.venv', 
                'build', 'dist', 'target', 'bin', 'obj'
            }):
                continue
            
            extension = file_path.suffix.lower()
            file_types[extension or 'no_extension'] += 1
            
            # Count lines and determine language
            if extension not in BINARY_EXTENSIONS:
                lines = count_lines_in_file(file_path)
                total_lines += lines
                
                # Map to language
                if extension in LANGUAGE_EXTENSIONS:
                    language = LANGUAGE_EXTENSIONS[extension]
                    language_lines[language] += lines
                elif not extension:
                    # Check shebang for extensionless files
                    try:
                        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                            first_line = f.readline().strip()
                            if first_line.startswith('#!'):
                                if 'python' in first_line:
                                    language_lines['Python'] += lines
                                elif 'bash' in first_line or 'sh' in first_line:
                                    language_lines['Shell'] += lines
                                elif 'node' in first_line:
                                    language_lines['JavaScript'] += lines
                    except Exception:
                        pass
    
    # Calculate language percentages
    total_language_lines = sum(language_lines.values())
    languages = {}
    if total_language_lines > 0:
        for lang, lines in language_lines.items():
            languages[lang] = round((lines / total_language_lines) * 100, 2)
    
    # Build directory structure
    structure = build_directory_structure(path)
    
    logger.info(f"Analysis complete: {total_lines} lines, {len(file_types)} file types")
    
    return {
        "total_lines": total_lines,
        "file_types": dict(file_types),
        "languages": languages,
        "structure": structure
    }

async def stream_file_contents(file_path: str, chunk_size: int = 8192) -> AsyncGenerator[bytes, None]:
    """
    Asynchronously stream file contents in chunks for efficient transfer.
    
    Args:
        file_path: Path to the file to stream
        chunk_size: Size of each chunk in bytes
        
    Yields:
        bytes: File content chunks
    """
    try:
        path = Path(file_path)
        
        if not path.exists():
            raise FileNotFoundError(f"File not found: {file_path}")
        
        if not path.is_file():
            raise ValueError(f"Path is not a file: {file_path}")
        
        logger.info(f"Streaming file: {file_path} ({path.stat().st_size} bytes)")
        
        with open(path, 'rb') as file:
            while True:
                chunk = file.read(chunk_size)
                if not chunk:
                    break
                yield chunk
                
                # Allow other coroutines to run
                await asyncio.sleep(0)
                
    except Exception as e:
        logger.error(f"Error streaming file {file_path}: {str(e)}")
        raise

def cleanup_repository(repo_id: str, base_path: str) -> bool:
    """
    Clean up a cloned repository from the filesystem.
    
    Args:
        repo_id: Repository ID to clean up
        base_path: Base directory containing repositories
        
    Returns:
        bool: True if cleanup was successful
    """
    try:
        if not validate_repo_id(repo_id):
            raise ValueError("Invalid repository ID")
        
        repo_path = Path(base_path) / repo_id
        
        if repo_path.exists():
            safe_rmtree(repo_path)
            logger.info(f"Successfully cleaned up repository {repo_id}")
            return True
        else:
            logger.warning(f"Repository {repo_id} not found for cleanup")
            return False
            
    except Exception as e:
        logger.error(f"Failed to clean up repository {repo_id}: {str(e)}")
        return False

def get_file_info(file_path: str) -> Dict[str, Any]:
    """
    Get detailed information about a file.
    
    Args:
        file_path: Path to the file
        
    Returns:
        Dict: File information including size, type, and metadata
    """
    try:
        path = Path(file_path)
        
        if not path.exists():
            raise FileNotFoundError(f"File not found: {file_path}")
        
        stat = path.stat()
        mime_type, _ = mimetypes.guess_type(str(path))
        
        return {
            "name": path.name,
            "size": stat.st_size,
            "extension": path.suffix,
            "mime_type": mime_type or "application/octet-stream",
            "is_binary": path.suffix.lower() in BINARY_EXTENSIONS,
            "modified": stat.st_mtime,
            "language": LANGUAGE_EXTENSIONS.get(path.suffix.lower(), "Unknown")
        }
        
    except Exception as e:
        logger.error(f"Failed to get file info for {file_path}: {str(e)}")
        raise
