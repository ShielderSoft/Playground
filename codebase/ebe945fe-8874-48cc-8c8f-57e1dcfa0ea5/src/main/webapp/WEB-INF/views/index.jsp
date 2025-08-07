<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>TestVWA - Vulnerable Web Application</title>
    <link rel="stylesheet" href="/resources/css/style.css">
    <!-- VULNERABILITY: Mixed content - loading resources over HTTP -->
    <script src="http://code.jquery.com/jquery-1.9.1.min.js"></script>
</head>
<body>
    <header>
        <nav>
            <div class="container">
                <h1><a href="/">TestVWA</a></h1>
                <ul>
                    <li><a href="/">Home</a></li>
                    <li><a href="/post/list">Posts</a></li>
                    <li><a href="/login">Login</a></li>
                    <li><a href="/register">Register</a></li>
                </ul>
            </div>
        </nav>
    </header>

    <main class="container">
        <section class="hero">
            <h2>Welcome to TestVWA</h2>
            <p>A deliberately vulnerable web application for security testing</p>
            <div class="buttons">
                <a href="/login" class="btn btn-primary">Login</a>
                <a href="/register" class="btn btn-secondary">Sign Up</a>
            </div>
        </section>

        <section class="features">
            <h3>Features</h3>
            <div class="feature-grid">
                <div class="feature">
                    <h4>User Management</h4>
                    <p>Create and manage user accounts</p>
                </div>
                <div class="feature">
                    <h4>Blog Posts</h4>
                    <p>Create and share blog posts</p>
                </div>
                <div class="feature">
                    <h4>File Upload</h4>
                    <p>Upload and share files</p>
                </div>
                <div class="feature">
                    <h4>API Access</h4>
                    <p>RESTful API for integration</p>
                </div>
            </div>
        </section>

        <!-- VULNERABILITY: Information disclosure -->
        <section class="debug-info">
            <h3>Debug Information</h3>
            <p>Server: <%= request.getServerName() %>:<%= request.getServerPort() %></p>
            <p>Context Path: <%= request.getContextPath() %></p>
            <p>Session ID: <%= session.getId() %></p>
            <p>User Agent: <%= request.getHeader("User-Agent") %></p>
        </section>
    </main>

    <footer>
        <div class="container">
            <p>&copy; 2025 TestVWA - Vulnerable Web Application for Testing</p>
        </div>
    </footer>
</body>
</html>
