package com.testvwa.service;

import com.testvwa.model.Post;
import com.testvwa.model.User;
import com.testvwa.repository.PostRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class PostService {

    private static final Logger logger = Logger.getLogger(PostService.class);

    @Autowired
    private PostRepository postRepository;

    public Post createPost(Post post) {
        // VULNERABILITY: No XSS protection - content stored as-is
        logger.info("Creating post: " + post.getTitle());
        post.setCreatedDate(new Date());
        post.setModifiedDate(new Date());
        return postRepository.save(post);
    }

    public Post findById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> getPostsByUser(Long userId) {
        return postRepository.findByUserId(userId);
    }

    public List<Post> searchPosts(String keyword) {
        // VULNERABILITY: No input sanitization
        logger.debug("Searching posts with keyword: " + keyword);
        return postRepository.searchPosts(keyword);
    }

    public Post updatePost(Post post) {
        // VULNERABILITY: No authorization check
        post.setModifiedDate(new Date());
        return postRepository.save(post);
    }

    public boolean deletePost(Long postId) {
        try {
            Post post = postRepository.findById(postId);
            if (post != null) {
                // VULNERABILITY: No authorization check - anyone can delete any post
                postRepository.delete(post);
                return true;
            }
        } catch (Exception e) {
            logger.error("Error deleting post", e);
            throw new RuntimeException("Failed to delete post: " + e.getMessage());
        }
        return false;
    }

    public List<Post> executeCustomQuery(String query) {
        // VULNERABILITY: Direct execution of user queries
        logger.warn("Executing custom query: " + query);
        return postRepository.getPostsByQuery(query);
    }

    public String renderPostContent(String content) {
        // VULNERABILITY: No HTML encoding - direct rendering of user content
        return content;
    }
}
