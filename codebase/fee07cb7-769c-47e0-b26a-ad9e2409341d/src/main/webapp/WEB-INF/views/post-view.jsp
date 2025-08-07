<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>View Post - TestVWA</title>
    <link rel="stylesheet" href="/resources/css/style.css">
</head>
<body>
    <div class="container">
        <article class="post">
            <h1>${post.title}</h1>
            <div class="post-meta">
                <p>By: ${post.author.username} | Date: ${post.createdDate}</p>
                <p>Post ID: ${post.id}</p>
            </div>
            
            <div class="post-content">
                <!-- VULNERABILITY: XSS - Unescaped user content -->
                ${renderedContent}
            </div>

            <div class="post-actions">
                <!-- VULNERABILITY: No authorization check in links -->
                <a href="/post/edit/${post.id}" class="btn btn-secondary">Edit</a>
                <button onclick="deletePost('${post.id}')" class="btn btn-danger">Delete</button>
            </div>
        </article>

        <div class="links">
            <a href="/post/list">Back to Posts</a>
        </div>
    </div>

    <script>
        function deletePost(postId) {
            if (confirm('Are you sure you want to delete this post?')) {
                // VULNERABILITY: No CSRF protection
                fetch('/post/delete/' + postId, {
                    method: 'POST'
                })
                .then(response => response.text())
                .then(data => {
                    alert(data);
                    window.location.href = '/post/list';
                });
            }
        }

        // VULNERABILITY: XSS via JavaScript injection
        var postContent = `${post.content}`;
        console.log(postContent);
    </script>
</body>
</html>
