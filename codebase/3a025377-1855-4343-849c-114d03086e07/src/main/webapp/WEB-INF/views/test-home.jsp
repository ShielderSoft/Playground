<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Vulnerability Testing - TestVWA</title>
    <link rel="stylesheet" href="/resources/css/style.css">
</head>
<body>
    <div class="container">
        <h1>Vulnerability Testing Interface</h1>
        <p>This page provides direct access to various vulnerability testing endpoints.</p>

        <div class="vulnerability-section">
            <h2>Cross-Site Scripting (XSS)</h2>
            <form action="/test/xss" method="get">
                <input type="text" name="input" placeholder="Enter XSS payload" size="50">
                <button type="submit">Test XSS</button>
            </form>
            <p><strong>Example:</strong> &lt;script&gt;alert('XSS')&lt;/script&gt;</p>
        </div>

        <div class="vulnerability-section">
            <h2>SQL Injection</h2>
            <form action="/test/sqli" method="get">
                <input type="text" name="id" placeholder="Enter SQL injection payload" size="50">
                <button type="submit">Test SQL Injection</button>
            </form>
            <p><strong>Example:</strong> 1 UNION SELECT username,password FROM users--</p>
        </div>

        <div class="vulnerability-section">
            <h2>Command Injection</h2>
            <form action="/test/cmd" method="get">
                <input type="text" name="cmd" placeholder="Enter command" size="50">
                <button type="submit">Execute Command</button>
            </form>
            <p><strong>Example:</strong> whoami</p>
        </div>

        <div class="vulnerability-section">
            <h2>Local File Inclusion</h2>
            <form action="/test/lfi" method="get">
                <input type="text" name="file" placeholder="Enter file path" size="50">
                <button type="submit">Read File</button>
            </form>
            <p><strong>Example:</strong> ../../../etc/passwd (Linux) or ../../../windows/system32/drivers/etc/hosts (Windows)</p>
        </div>

        <div class="vulnerability-section">
            <h2>Remote File Inclusion / SSRF</h2>
            <form action="/test/rfi" method="get">
                <input type="text" name="url" placeholder="Enter URL" size="50">
                <button type="submit">Fetch Remote Content</button>
            </form>
            <p><strong>Example:</strong> http://example.com or file:///etc/passwd</p>
        </div>

        <div class="vulnerability-section">
            <h2>Path Traversal</h2>
            <form action="/test/path-traversal" method="get">
                <input type="text" name="path" placeholder="Enter relative path" size="50">
                <button type="submit">Access File</button>
            </form>
            <p><strong>Example:</strong> ../../../etc/passwd</p>
        </div>

        <div class="vulnerability-section">
            <h2>Insecure Direct Object Reference</h2>
            <form action="/test/idor" method="get">
                <input type="text" name="userId" placeholder="Enter user ID" size="50">
                <button type="submit">Access User Data</button>
            </form>
            <p><strong>Example:</strong> 1, 2, 3, etc.</p>
        </div>

        <div class="vulnerability-section">
            <h2>Weak Cryptography</h2>
            <form action="/test/weak-crypto" method="get">
                <input type="text" name="data" placeholder="Enter data to encrypt" size="50">
                <button type="submit">Test Encryption</button>
            </form>
        </div>

        <div class="vulnerability-section">
            <h2>Information Disclosure</h2>
            <a href="/test/info" class="btn btn-primary">View System Information</a>
        </div>

        <div class="vulnerability-section">
            <h2>Session Management</h2>
            <a href="/test/weak-session" class="btn btn-primary">Generate Weak Session Token</a>
        </div>

        <div class="vulnerability-section">
            <h2>Backdoor Authentication</h2>
            <form action="/test/backdoor" method="get">
                <input type="text" name="username" placeholder="Username" size="25">
                <input type="password" name="password" placeholder="Password" size="25">
                <button type="submit">Test Authentication</button>
            </form>
            <p><strong>Hint:</strong> Try "backdoor" / "secret"</p>
        </div>

        <div class="vulnerability-section">
            <h2>Open Redirect</h2>
            <form action="/test/redirect" method="get">
                <input type="text" name="url" placeholder="Enter redirect URL" size="50">
                <button type="submit">Test Redirect</button>
            </form>
            <p><strong>Example:</strong> http://evil.com</p>
        </div>

        <div class="vulnerability-section">
            <h2>Log Injection</h2>
            <form action="/test/log-injection" method="get">
                <input type="text" name="data" placeholder="Enter data to log" size="50">
                <button type="submit">Inject Log</button>
            </form>
            <p><strong>Example:</strong> normal_user%0d%0aADMIN_LOGIN_SUCCESS</p>
        </div>

        <div class="links">
            <a href="/dashboard">Back to Dashboard</a> |
            <a href="/">Home</a>
        </div>
    </div>

    <style>
        .vulnerability-section {
            background: white;
            margin: 1rem 0;
            padding: 1.5rem;
            border-radius: 8px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        
        .vulnerability-section h2 {
            color: #dc3545;
            margin-bottom: 1rem;
        }
        
        .vulnerability-section form {
            margin-bottom: 0.5rem;
        }
        
        .vulnerability-section input {
            margin-right: 0.5rem;
            padding: 0.5rem;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        
        .vulnerability-section button {
            padding: 0.5rem 1rem;
            background-color: #dc3545;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        
        .vulnerability-section p {
            font-size: 0.9rem;
            color: #666;
            margin-top: 0.5rem;
        }
    </style>
</body>
</html>
