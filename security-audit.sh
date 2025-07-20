#!/bin/bash

# Playground Docker Security Audit Script
# Run this script to perform basic security checks on your Docker setup

echo "🔒 Playground Docker Security Audit"
echo "=================================="

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "❌ Docker is not running"
    exit 1
fi

echo "✅ Docker is running"

# Check for Docker security best practices
echo ""
echo "📋 Security Checks:"

# Check if image exists
if docker images playground >/dev/null 2>&1; then
    echo "✅ Playground image found"
else
    echo "❌ Playground image not found. Run: docker build -t playground ."
    exit 1
fi

# Check image vulnerabilities (if docker scan is available)
echo ""
echo "🔍 Scanning for vulnerabilities..."
if command -v docker-scout >/dev/null 2>&1; then
    docker scout quickview playground
elif command -v trivy >/dev/null 2>&1; then
    trivy image playground
else
    echo "⚠️  No vulnerability scanner found. Consider installing Docker Scout or Trivy"
fi

# Check running container security
if docker ps | grep -q playground-container; then
    echo ""
    echo "🔍 Checking running container security..."
    
    # Check if running as non-root
    USER_ID=$(docker exec playground-container id -u)
    if [ "$USER_ID" -ne 0 ]; then
        echo "✅ Container running as non-root user (UID: $USER_ID)"
    else
        echo "❌ Container running as root - security risk!"
    fi
    
    # Check read-only filesystem
    if docker inspect playground-container | grep -q '"ReadonlyRootfs": true'; then
        echo "✅ Read-only root filesystem enabled"
    else
        echo "⚠️  Read-only filesystem not enabled"
    fi
    
    # Check capabilities
    CAPS=$(docker inspect playground-container --format '{{.HostConfig.CapDrop}}')
    if echo "$CAPS" | grep -q "ALL"; then
        echo "✅ All capabilities dropped"
    else
        echo "⚠️  Not all capabilities dropped: $CAPS"
    fi
    
else
    echo "ℹ️  Container not currently running. Start with: docker-compose up -d"
fi

echo ""
echo "📊 Security Recommendations:"
echo "1. Regularly update base images"
echo "2. Scan for vulnerabilities before deployment"
echo "3. Use secrets management for sensitive data"
echo "4. Enable Docker Content Trust in production"
echo "5. Monitor container logs and metrics"
echo "6. Implement network segmentation"
echo "7. Use official images when possible"

echo ""
echo "🎯 Next Steps:"
echo "1. Run 'pip-audit' to check for Python vulnerabilities"
echo "2. Set up monitoring and alerting"
echo "3. Configure proper backup procedures"
echo "4. Implement CI/CD security scanning"
echo "5. Run 'python -m safety check' for additional security checks"

echo ""
echo "Security audit completed! 🎉"
