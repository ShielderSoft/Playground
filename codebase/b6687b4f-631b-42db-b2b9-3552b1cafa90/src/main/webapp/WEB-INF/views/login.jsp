<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login - TestVWA</title>
    <link rel="stylesheet" href="/resources/css/style.css">
</head>
<body>
    <div class="container">
        <div class="login-form">
            <h2>Login</h2>
            
            <!-- VULNERABILITY: XSS in error messages -->
            <c:if test="${not empty error}">
                <div class="error">
                    ${error}
                </div>
            </c:if>
            
            <c:if test="${not empty success}">
                <div class="success">
                    ${success}
                </div>
            </c:if>

            <form action="/login" method="post">
                <div class="form-group">
                    <label for="username">Username:</label>
                    <input type="text" id="username" name="username" required>
                </div>
                
                <div class="form-group">
                    <label for="password">Password:</label>
                    <input type="password" id="password" name="password" required>
                </div>
                
                <button type="submit" class="btn btn-primary">Login</button>
            </form>

            <div class="links">
                <a href="/register">Don't have an account? Sign up</a>
                <br>
                <a href="/">Back to Home</a>
            </div>

            <!-- VULNERABILITY: Information disclosure -->
            <div class="debug-info">
                <h4>Test Credentials:</h4>
                <p>Username: admin, Password: admin123 (Admin)</p>
                <p>Username: user, Password: password (User)</p>
                <p>Username: test, Password: test (User)</p>
            </div>
        </div>
    </div>

    <!-- VULNERABILITY: Inline JavaScript with potential XSS -->
    <script>
        // VULNERABILITY: No CSRF protection
        function quickLogin(username, password) {
            document.getElementById('username').value = username;
            document.getElementById('password').value = password;
        }
    </script>
</body>
</html>
