package com.testvwa.controller;

import com.testvwa.model.Post;
import com.testvwa.model.User;
import com.testvwa.service.PostService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/post")
public class PostController {

    private static final Logger logger = Logger.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    @RequestMapping("/create")
    public String createPostForm() {
        return "create-post";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String createPost(@RequestParam String title,
                            @RequestParam String content,
                            HttpServletRequest request,
                            Model model) {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        try {
            User user = (User) session.getAttribute("user");
            // VULNERABILITY: No XSS protection - content stored as-is
            Post post = new Post(title, content, user);
            postService.createPost(post);
            
            model.addAttribute("success", "Post created successfully!");
            return "redirect:/post/list";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create post: " + e.getMessage());
            return "create-post";
        }
    }

    @RequestMapping("/list")
    public String listPosts(Model model) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        return "post-list";
    }

    @RequestMapping("/view/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        Post post = postService.findById(id);
        if (post != null) {
            // VULNERABILITY: Rendering user content without sanitization
            String renderedContent = postService.renderPostContent(post.getContent());
            model.addAttribute("post", post);
            model.addAttribute("renderedContent", renderedContent);
        }
        return "post-view";
    }

    @RequestMapping("/edit/{id}")
    public String editPostForm(@PathVariable Long id, Model model) {
        // VULNERABILITY: No authorization check
        Post post = postService.findById(id);
        model.addAttribute("post", post);
        return "edit-post";
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.POST)
    public String editPost(@PathVariable Long id,
                          @RequestParam String title,
                          @RequestParam String content,
                          Model model) {
        
        try {
            // VULNERABILITY: Insecure Direct Object Reference
            Post post = postService.findById(id);
            if (post != null) {
                post.setTitle(title);
                post.setContent(content); // VULNERABILITY: No XSS protection
                postService.updatePost(post);
                
                return "redirect:/post/view/" + id;
            }
        } catch (Exception e) {
            model.addAttribute("error", "Update failed: " + e.getMessage());
        }
        
        return "edit-post";
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public String deletePost(@PathVariable Long id) {
        // VULNERABILITY: No authorization check
        try {
            boolean deleted = postService.deletePost(id);
            return deleted ? "Post deleted" : "Post not found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @RequestMapping("/search")
    public String searchPosts(@RequestParam(required = false) String q, Model model) {
        if (q != null && !q.isEmpty()) {
            // VULNERABILITY: No input sanitization
            List<Post> posts = postService.searchPosts(q);
            model.addAttribute("posts", posts);
            model.addAttribute("query", q);
        }
        return "post-search";
    }

    @RequestMapping("/query")
    @ResponseBody
    public List<Post> customQuery(@RequestParam String query) {
        // VULNERABILITY: Direct execution of user queries
        logger.warn("Executing custom query: " + query);
        return postService.executeCustomQuery(query);
    }

    @RequestMapping("/my-posts")
    public String myPosts(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        List<Post> posts = postService.getPostsByUser(user.getId());
        model.addAttribute("posts", posts);
        return "my-posts";
    }
}
