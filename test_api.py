#!/usr/bin/env python3
"""
API Testing Script for Playground API
Tests all endpoints and generates documentation based on actual responses.
"""

import requests
import json
import time
import sys

BASE_URL = "http://localhost:8000"

def test_health_endpoint():
    """Test the health check endpoint."""
    print("Testing /health endpoint...")
    try:
        response = requests.get(f"{BASE_URL}/health")
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        return response.status_code, response.json()
    except Exception as e:
        print(f"Error testing /health: {e}")
        return None, None

def test_clone_endpoint():
    """Test the clone endpoint with a sample repository."""
    print("\nTesting /clone endpoint...")
    try:
        # Test with a simple public repository
        data = {
            "repo_url": "octocat/Hello-World",
            "branch": "master"
        }
        response = requests.post(
            f"{BASE_URL}/clone",
            json=data,
            headers={"Content-Type": "application/json"}
        )
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        
        if response.status_code == 200:
            return response.status_code, response.json()
        else:
            # Try without GitHub token requirement by testing with owner/repo format
            print("Retrying with different format...")
            return response.status_code, response.json()
    except Exception as e:
        print(f"Error testing /clone: {e}")
        return None, None

def test_generate_endpoint(repo_id):
    """Test the generate analysis endpoint."""
    print(f"\nTesting /generate endpoint with repo_id: {repo_id}...")
    try:
        response = requests.get(f"{BASE_URL}/generate?repo_id={repo_id}")
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        return response.status_code, response.json()
    except Exception as e:
        print(f"Error testing /generate: {e}")
        return None, None

def test_file_endpoint(repo_id, file_path):
    """Test the file streaming endpoint."""
    print(f"\nTesting /file endpoint with repo_id: {repo_id}, path: {file_path}...")
    try:
        response = requests.get(f"{BASE_URL}/file?repo_id={repo_id}&path={file_path}")
        print(f"Status Code: {response.status_code}")
        if response.status_code == 200:
            content = response.text[:200]  # First 200 chars
            print(f"File Content (first 200 chars): {content}")
            return response.status_code, content
        else:
            print(f"Error Response: {response.text}")
            return response.status_code, response.text
    except Exception as e:
        print(f"Error testing /file: {e}")
        return None, None

def main():
    """Run all API tests."""
    print("Starting API endpoint testing...\n")
    
    # Test health endpoint
    health_status, health_response = test_health_endpoint()
    
    # Test clone endpoint
    clone_status, clone_response = test_clone_endpoint()
    
    repo_id = None
    if clone_status == 200 and clone_response:
        repo_id = clone_response.get("repo_id")
    
    # Test generate endpoint if we have a repo_id
    generate_status, generate_response = None, None
    if repo_id:
        # Wait a moment for clone to complete
        time.sleep(2)
        generate_status, generate_response = test_generate_endpoint(repo_id)
    
    # Test file endpoint if we have a repo_id
    file_status, file_response = None, None
    if repo_id:
        file_status, file_response = test_file_endpoint(repo_id, "README")
    
    print("\n" + "="*50)
    print("TEST RESULTS SUMMARY")
    print("="*50)
    print(f"Health endpoint: {health_status}")
    print(f"Clone endpoint: {clone_status}")
    print(f"Generate endpoint: {generate_status}")
    print(f"File endpoint: {file_status}")
    
    return {
        "health": (health_status, health_response),
        "clone": (clone_status, clone_response),
        "generate": (generate_status, generate_response),
        "file": (file_status, file_response),
        "repo_id": repo_id
    }

if __name__ == "__main__":
    results = main()
