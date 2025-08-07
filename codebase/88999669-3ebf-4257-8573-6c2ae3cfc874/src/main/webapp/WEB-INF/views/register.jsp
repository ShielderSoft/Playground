<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Register - TestVWA</title>
    <link rel="stylesheet" href="/resources/css/style.css">
</head>
<body>
    <div class="container">
        <div class="register-form">
            <h2>Register</h2>
            
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

            <form action="/register" method="post">
                <div class="form-group">
                    <label for="username">Username:</label>
                    <input type="text" id="username" name="username" required>
                </div>
                
                <div class="form-group">
                    <label for="password">Password:</label>
                    <!-- VULNERABILITY: No password complexity requirements -->
                    <input type="password" id="password" name="password" required>
                </div>
                
                <div class="form-group">
                    <label for="email">Email:</label>
                    <input type="email" id="email" name="email" required>
                </div>
                
                <div class="form-group">
                    <label for="firstName">First Name:</label>
                    <input type="text" id="firstName" name="firstName">
                </div>
                
                <div class="form-group">
                    <label for="lastName">Last Name:</label>
                    <input type="text" id="lastName" name="lastName">
                </div>
                
                <!-- VULNERABILITY: Collecting sensitive data -->
                <div class="form-group">
                    <label for="ssn">SSN (Optional):</label>
                    <input type="text" id="ssn" name="ssn" placeholder="XXX-XX-XXXX">
                </div>
                
                <div class="form-group">
                    <label for="creditCard">Credit Card (Optional):</label>
                    <input type="text" id="creditCard" name="creditCard" placeholder="1234-5678-9012-3456">
                </div>
                
                <button type="submit" class="btn btn-primary">Register</button>
            </form>

            <div class="links">
                <a href="/login">Already have an account? Login</a>
                <br>
                <a href="/">Back to Home</a>
            </div>
        </div>
    </div>

    <!-- VULNERABILITY: Client-side validation only -->
    <script>
        document.querySelector('form').addEventListener('submit', function(e) {
            var password = document.getElementById('password').value;
            if (password.length < 3) {
                alert('Password too short!');
                e.preventDefault();
            }
        });
    </script>
</body>
</html>
