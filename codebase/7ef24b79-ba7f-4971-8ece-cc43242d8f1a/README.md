# TestVWA - Vulnerable Web Application

TestVWA is a deliberately vulnerable Java web application designed for security testing and vulnerability scanning tool validation. This application contains numerous security vulnerabilities and uses outdated dependencies with known CVEs.

## ⚠️ WARNING
This application is intentionally vulnerable and should NEVER be deployed in a production environment or on a public-facing server. Use only in isolated testing environments.

## Architecture
- **Framework**: Spring MVC 4.3.9 (outdated)
- **Security**: Spring Security 4.2.3 (vulnerable)
- **Database**: H2 in-memory database / Hibernate 4.3.11
- **Build Tool**: Maven
- **Java Version**: 8

## Vulnerabilities Included

### 1. Outdated Dependencies (CVEs)
- Spring Framework 4.3.9 (CVE-2018-1270, CVE-2018-1271)
- Spring Security 4.2.3 (CVE-2017-8028, CVE-2018-1258)
- Jackson 2.8.11 (CVE-2018-7489, CVE-2017-17485)
- Log4j 1.2.17 (CVE-2021-44228, CVE-2019-17571)
- Apache Commons FileUpload 1.3.2 (CVE-2016-1000031)
- Apache Commons Collections 4.0 (CVE-2015-6420)
- Hibernate 4.3.11 (CVE-2020-25638)
- MySQL Connector 5.1.46 (CVE-2017-3523)

### 2. Authentication & Session Management
- **Weak Passwords**: Hardcoded credentials (admin/admin123, user/password)
- **No Password Encryption**: Passwords stored in plain text
- **Session Fixation**: Session ID not regenerated after login
- **Weak Session Management**: Predictable session tokens
- **Long Session Timeout**: 8-hour session timeout
- **Insecure Cookies**: Secure and HttpOnly flags not set
- **Backdoor Account**: Hidden backdoor credentials

### 3. Injection Vulnerabilities
- **SQL Injection**: Multiple endpoints with string concatenation
- **Command Injection**: Direct execution of user input
- **LDAP Injection**: (if LDAP integration added)
- **Log Injection**: User input logged without sanitization
- **XML External Entity (XXE)**: XML processing without entity restrictions

### 4. Cross-Site Scripting (XSS)
- **Reflected XSS**: User input directly rendered in responses
- **Stored XSS**: Blog posts and user data stored without encoding
- **DOM-based XSS**: Client-side JavaScript vulnerabilities

### 5. Broken Access Control
- **Insecure Direct Object Reference (IDOR)**: No authorization checks
- **Missing Access Controls**: Administrative functions accessible to all
- **Privilege Escalation**: Users can modify their own roles
- **Path Traversal**: File operations without path validation

### 6. Security Misconfiguration
- **CSRF Protection Disabled**: No CSRF tokens
- **Debug Mode Enabled**: Detailed error messages and stack traces
- **Information Disclosure**: System information exposed
- **Default Credentials**: Well-known default passwords
- **Unnecessary HTTP Methods**: All HTTP methods enabled

### 7. Insecure Cryptographic Storage
- **Weak Encryption**: DES algorithm usage
- **Hardcoded Keys**: Encryption keys in source code
- **MD5 Hashing**: Weak password hashing algorithm
- **Plain Text Storage**: Sensitive data (SSN, credit cards) unencrypted

### 8. Insecure Deserialization
- **Java Deserialization**: Unsafe object deserialization
- **JSON Deserialization**: Jackson vulnerabilities

### 9. File Upload Vulnerabilities
- **Unrestricted File Upload**: No file type validation
- **Path Traversal**: User-controlled file paths
- **File Inclusion**: Local and remote file inclusion

### 10. Server-Side Request Forgery (SSRF)
- **Remote File Fetching**: Application fetches user-specified URLs
- **Internal Network Access**: Potential access to internal services

## Project Structure

```
TestVWA/
├── pom.xml
├── README.md
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── testvwa/
│       │           ├── config/
│       │           │   ├── DatabaseConfig.java
│       │           │   ├── SecurityConfig.java
│       │           │   ├── WebAppInitializer.java
│       │           │   └── WebConfig.java
│       │           ├── controller/
│       │           │   ├── AuthController.java
│       │           │   ├── FileController.java
│       │           │   ├── PostController.java
│       │           │   ├── UserController.java
│       │           │   ├── VulnerabilityTestController.java
│       │           │   └── api/
│       │           │       └── UserApiController.java
│       │           ├── model/
│       │           │   ├── Post.java
│       │           │   └── User.java
│       │           ├── repository/
│       │           │   ├── PostRepository.java
│       │           │   └── UserRepository.java
│       │           ├── service/
│       │           │   ├── PostService.java
│       │           │   └── UserService.java
│       │           └── util/
│       │               └── SecurityUtils.java
│       ├── resources/
│       │   ├── database.properties
│       │   └── log4j.properties
│       └── webapp/
│           ├── WEB-INF/
│           │   ├── views/
│           │   │   ├── dashboard.jsp
│           │   │   ├── file-upload.jsp
│           │   │   ├── index.jsp
│           │   │   ├── login.jsp
│           │   │   ├── post-view.jsp
│           │   │   └── register.jsp
│           │   └── web.xml
│           └── resources/
│               └── css/
│                   └── style.css
```

## Building and Running

### Prerequisites
- Java 8 or higher
- Maven 3.x
- Tomcat 8.x or similar servlet container

### Build Instructions
```bash
# Clone the repository
git clone <repository-url>
cd TestVWA

# Build the project
mvn clean compile

# Package as WAR
mvn package

# Deploy to Tomcat
cp target/testvwa.war $TOMCAT_HOME/webapps/
```

### Running
1. Deploy the WAR file to your servlet container
2. Start the server
3. Navigate to `http://localhost:8080/testvwa`

## Default Credentials
- **Admin**: username: `admin`, password: `admin123`
- **User**: username: `user`, password: `password`
- **Test User**: username: `test`, password: `test`
- **Backdoor**: username: `backdoor`, password: `secret`

## Testing Endpoints

### Authentication
- `POST /login` - User login
- `POST /register` - User registration
- `GET /logout` - User logout

### User Management
- `GET /user/profile` - User profile
- `POST /user/profile` - Update profile
- `GET /user/search` - Search users
- `GET /user/view/{id}` - View user details
- `POST /user/delete/{id}` - Delete user
- `GET /user/exec` - Execute system commands

### Posts
- `GET /post/list` - List all posts
- `POST /post/create` - Create new post
- `GET /post/view/{id}` - View post
- `POST /post/edit/{id}` - Edit post
- `POST /post/delete/{id}` - Delete post
- `GET /post/search` - Search posts

### File Operations
- `POST /file/upload` - File upload
- `GET /file/download` - File download
- `GET /file/view` - View file content
- `POST /file/xml` - XML processing
- `GET /file/remote` - Remote file inclusion

### API Endpoints
- `GET /api/users` - List users
- `GET /api/users/{id}` - Get user
- `POST /api/users/auth` - API authentication
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Vulnerability Testing
- `GET /test/xss` - Reflected XSS
- `GET /test/sqli` - SQL Injection
- `GET /test/lfi` - Local File Inclusion
- `GET /test/rfi` - Remote File Inclusion
- `GET /test/cmd` - Command Injection
- `GET /test/idor` - Insecure Direct Object Reference
- `GET /test/path-traversal` - Path Traversal
- `GET /test/weak-crypto` - Weak Cryptography

## Example Payloads

### XSS
```
GET /test/xss?input=<script>alert('XSS')</script>
```

### SQL Injection
```
GET /test/sqli?id=1 UNION SELECT username,password FROM users--
```

### Command Injection
```
GET /test/cmd?cmd=whoami
GET /user/exec?cmd=dir
```

### Path Traversal
```
GET /file/view?file=../../../etc/passwd
GET /test/path-traversal?path=../../../windows/system32/drivers/etc/hosts
```

### XXE Injection
```xml
POST /file/xml
Content-Type: application/xml

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE foo [
<!ENTITY xxe SYSTEM "file:///etc/passwd">
]>
<root>&xxe;</root>
```

## License
This project is intended for educational and testing purposes only. Use at your own risk.

## Disclaimer
This application is intentionally vulnerable and should only be used in controlled environments for security testing purposes. The authors are not responsible for any damage or misuse of this application.
