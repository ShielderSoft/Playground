<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dashboard - TestVWA</title>
    <link rel="stylesheet" href="/resources/css/style.css">
</head>
<body>
    <header>
        <nav>
            <div class="container">
                <h1><a href="/">TestVWA</a></h1>
                <ul>
                    <li><a href="/dashboard">Dashboard</a></li>
                    <li><a href="/post/list">Posts</a></li>
                    <li><a href="/post/my-posts">My Posts</a></li>
                    <li><a href="/user/profile">Profile</a></li>
                    <li><a href="/file/upload">Upload</a></li>
                    <li><a href="/logout">Logout</a></li>
                </ul>
            </div>
        </nav>
    </header>

    <main class="container">
        <h2>Welcome, ${user.firstName} ${user.lastName}!</h2>
        
        <div class="dashboard-grid">
            <div class="dashboard-card">
                <h3>User Information</h3>
                <!-- VULNERABILITY: Exposing sensitive data -->
                <p><strong>Username:</strong> ${user.username}</p>
                <p><strong>Email:</strong> ${user.email}</p>
                <p><strong>Role:</strong> ${user.role}</p>
                <p><strong>API Key:</strong> ${user.apiKey}</p>
                <p><strong>SSN:</strong> ${user.ssn}</p>
                <p><strong>Credit Card:</strong> ${user.creditCard}</p>
                <p><strong>Member Since:</strong> ${user.createdDate}</p>
            </div>

            <div class="dashboard-card">
                <h3>Quick Actions</h3>
                <ul>
                    <li><a href="/post/create">Create New Post</a></li>
                    <li><a href="/post/my-posts">View My Posts</a></li>
                    <li><a href="/user/profile">Edit Profile</a></li>
                    <li><a href="/file/upload">Upload File</a></li>
                </ul>
            </div>

            <div class="dashboard-card">
                <h3>Administrative Functions</h3>
                <!-- VULNERABILITY: No role-based access control in UI -->
                <ul>
                    <li><a href="/user/admin">User Management</a></li>
                    <li><a href="/user/search">Search Users</a></li>
                    <li><a href="/api/users">API Users</a></li>
                    <li><a href="/user/exec?cmd=whoami">System Commands</a></li>
                </ul>
            </div>

            <div class="dashboard-card">
                <h3>System Information</h3>
                <!-- VULNERABILITY: Information disclosure -->
                <p><strong>Server Time:</strong> <%= new java.util.Date() %></p>
                <p><strong>Session ID:</strong> <%= session.getId() %></p>
                <p><strong>Java Version:</strong> <%= System.getProperty("java.version") %></p>
                <p><strong>OS:</strong> <%= System.getProperty("os.name") %></p>
            </div>
        </div>

        <!-- VULNERABILITY: XSS via unescaped user data -->
        <div class="welcome-message">
            <h3>Welcome back, ${user.firstName}!</h3>
            <p>Your last login was from: <%= request.getRemoteAddr() %></p>
        </div>
    </main>

    <!-- VULNERABILITY: Inline JavaScript with potential issues -->
    <script>
        // VULNERABILITY: No CSRF protection on AJAX calls
        function executeCommand() {
            var cmd = prompt("Enter command to execute:");
            if (cmd) {
                fetch('/user/exec?cmd=' + encodeURIComponent(cmd))
                    .then(response => response.text())
                    .then(data => alert(data));
            }
        }

        // VULNERABILITY: Exposing API key in JavaScript
        var userApiKey = '${user.apiKey}';
        console.log('User API Key:', userApiKey);
    </script>
</body>
</html>
