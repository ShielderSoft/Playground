# Comprehensive API Documentation

This document provides a detailed overview of the Playground API, including endpoints, request formats, and example responses.

## Base URL

All API endpoints are relative to the following base URL:

```
http://localhost:8000
```

---

## 1. Health Check

This endpoint provides a health check of the API, returning system metrics and status.

- **Endpoint:** `GET /health`
- **Method:** `GET`
- **Description:** Checks the health of the API and returns system metrics like memory and CPU usage.

### Request

Use the following `curl.exe` command to test the endpoint:

```sh
curl.exe http://localhost:8000/health
```

### Response

A successful request returns a `200 OK` status code and a JSON object with the following structure:

```json
{
  "status": "healthy",
  "timestamp": "2025-07-26T10:00:00Z",
  "uptime": 1234.56,
  "version": "1.0.0",
  "memory_usage": {
    "total": 16384000000,
    "available": 8192000000,
    "percent": 50.0,
    "used": 8192000000
  },
  "cpu_usage": 15.5
}
```

---

## 2. Clone Repository

This endpoint clones a public GitHub repository to the server's local filesystem for further processing.

- **Endpoint:** `POST /clone`
- **Method:** `POST`
- **Description:** Clones a GitHub repository. You can provide the URL or the `owner/repo` slug.

### Request Body

The request body must be a JSON object with the following properties:

- `repo_url` (string, required): The URL of the GitHub repository or the `owner/repo` slug (e.g., `octocat/Hello-World`).
- `branch` (string, optional): The branch to clone. Defaults to `main`.

### Request

Use the following `curl.exe` command to test the endpoint. This example clones the `octocat/Hello-World` repository.

```sh
curl.exe -X POST -H "Content-Type: application/json" -d '{\"repo_url\": \"octocat/Hello-World\"}' http://localhost:8000/clone
```

### Response

A successful request returns a `200 OK` status code and a JSON object containing a unique `repo_id` for the cloned repository.

```json
{
  "repo_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
}
```
**Note:** You will need to use this `repo_id` in subsequent requests to analyze the repository or access its files.

---

## 3. Generate Repository Analysis

This endpoint analyzes a previously cloned repository and returns comprehensive metadata about it.

- **Endpoint:** `GET /generate`
- **Method:** `GET`
- **Description:** Provides a detailed analysis of a cloned repository, including line counts, language statistics, and file structure.

### Query Parameters

- `repo_id` (string, required): The unique ID of the repository returned by the `/clone` endpoint.

### Request

Use the following `curl.exe` command to test the endpoint. Replace `YOUR_REPO_ID` with the `repo_id` you received from the `/clone` endpoint.

```sh
curl.exe "http://localhost:8000/generate?repo_id=YOUR_REPO_ID"
```

### Response

A successful request returns a `200 OK` status code and a JSON object with the repository analysis.

```json
{
  "total_lines": 2,
  "file_types": {
    ".md": 1,
    "": 1
  },
  "languages": {},
  "structure": {
    "README": {
      "type": "file",
      "size": 31,
      "extension": ""
    },
    "README.md": {
      "type": "file",
      "size": 31,
      "extension": ".md"
    }
  }
}
```

---

## 4. Stream File Contents

This endpoint streams the contents of a specific file from a cloned repository.

- **Endpoint:** `GET /file`
- **Method:** `GET`
- **Description:** Retrieves and streams the raw content of a file from a cloned repository. This is efficient for large files.

### Query Parameters

- `repo_id` (string, required): The unique ID of the repository.
- `path` (string, required): The relative path to the file within the repository.

### Request

Use the following `curl.exe` command to test the endpoint. Replace `YOUR_REPO_ID` with your repository's ID and `README` with the desired file path.

```sh
curl.exe "http://localhost:8000/file?repo_id=YOUR_REPO_ID&path=README"
```

### Response

A successful request returns a `200 OK` status code and the raw content of the file as a stream. For a file like `README` in `octocat/Hello-World`, the response body would be:

```
Hello World!
```
