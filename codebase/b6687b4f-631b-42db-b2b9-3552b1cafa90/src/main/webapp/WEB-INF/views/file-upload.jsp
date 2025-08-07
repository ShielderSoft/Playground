<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>File Upload - TestVWA</title>
    <link rel="stylesheet" href="/resources/css/style.css">
</head>
<body>
    <div class="container">
        <h2>File Upload</h2>
        
        <!-- VULNERABILITY: XSS in messages -->
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

        <!-- VULNERABILITY: No CSRF protection -->
        <form action="/file/upload" method="post" enctype="multipart/form-data">
            <div class="form-group">
                <label for="file">Select File:</label>
                <!-- VULNERABILITY: No file type restrictions -->
                <input type="file" id="file" name="file" required>
            </div>
            
            <div class="form-group">
                <label for="description">Description:</label>
                <textarea id="description" name="description" rows="3"></textarea>
            </div>
            
            <button type="submit" class="btn btn-primary">Upload File</button>
        </form>

        <div class="file-operations">
            <h3>File Operations</h3>
            
            <!-- VULNERABILITY: Directory traversal forms -->
            <form action="/file/download" method="get" style="display: inline;">
                <label>Download File:</label>
                <input type="text" name="filename" placeholder="Enter filename">
                <button type="submit">Download</button>
            </form>
            
            <br><br>
            
            <form action="/file/view" method="get" style="display: inline;">
                <label>View File:</label>
                <input type="text" name="file" placeholder="Full file path">
                <button type="submit">View</button>
            </form>
            
            <br><br>
            
            <form action="/file/list" method="get" style="display: inline;">
                <label>List Directory:</label>
                <input type="text" name="dir" placeholder="Directory path">
                <button type="submit">List</button>
            </form>
        </div>

        <div class="xml-processing">
            <h3>XML Processing</h3>
            <!-- VULNERABILITY: XXE injection -->
            <textarea id="xmlContent" rows="10" cols="80" placeholder="Enter XML content here..."></textarea>
            <br>
            <button onclick="processXML()">Process XML</button>
            <div id="xmlResult"></div>
        </div>

        <div class="remote-file">
            <h3>Remote File Fetch</h3>
            <!-- VULNERABILITY: SSRF -->
            <input type="text" id="remoteUrl" placeholder="Enter URL to fetch">
            <button onclick="fetchRemoteFile()">Fetch</button>
            <div id="remoteResult"></div>
        </div>

        <div class="links">
            <a href="/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <script>
        function processXML() {
            var xmlContent = document.getElementById('xmlContent').value;
            fetch('/file/xml', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/xml'
                },
                body: xmlContent
            })
            .then(response => response.text())
            .then(data => {
                document.getElementById('xmlResult').innerHTML = data;
            });
        }

        function fetchRemoteFile() {
            var url = document.getElementById('remoteUrl').value;
            fetch('/file/remote?url=' + encodeURIComponent(url))
                .then(response => response.text())
                .then(data => {
                    document.getElementById('remoteResult').innerHTML = '<pre>' + data + '</pre>';
                });
        }

        // VULNERABILITY: Sample XXE payload in comments
        /*
        Sample XXE payload:
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE foo [
        <!ENTITY xxe SYSTEM "file:///etc/passwd">
        ]>
        <root>&xxe;</root>
        */
    </script>
</body>
</html>
