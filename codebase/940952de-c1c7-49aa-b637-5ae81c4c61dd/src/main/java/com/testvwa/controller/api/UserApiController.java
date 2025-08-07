package com.testvwa.controller.api;

import com.testvwa.model.User;
import com.testvwa.service.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private static final Logger logger = Logger.getLogger(UserApiController.class);

    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<User>> getAllUsers() {
        // VULNERABILITY: No authentication required for sensitive data
        List<User> users = userService.getAllUsers();
        // VULNERABILITY: Exposing all user data including sensitive fields
        return ResponseEntity.ok(users);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        // VULNERABILITY: No authorization check
        User user = userService.findById(id);
        if (user != null) {
            // VULNERABILITY: Exposing sensitive data via API
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> authenticate(@RequestBody Map<String, String> credentials,
                                                           HttpServletRequest request) {
        
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        // VULNERABILITY: Logging sensitive data
        logger.info("API authentication attempt from IP: " + request.getRemoteAddr() + 
                   " for user: " + username + " with password: " + password);
        
        User user = userService.authenticate(username, password);
        Map<String, Object> response = new HashMap<>();
        
        if (user != null) {
            // VULNERABILITY: Weak token generation
            String token = "token_" + user.getId() + "_" + System.currentTimeMillis();
            
            response.put("success", true);
            response.put("token", token);
            response.put("user", user); // VULNERABILITY: Exposing all user data
            response.put("apiKey", user.getApiKey());
            
            return ResponseEntity.ok(response);
        } else {
            // VULNERABILITY: Information disclosure
            response.put("success", false);
            response.put("error", "Invalid credentials for user: " + username);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ResponseEntity<List<User>> searchUsers(@RequestParam String q) {
        // VULNERABILITY: No rate limiting, no input validation
        List<User> users = userService.searchUsers(q);
        return ResponseEntity.ok(users);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userUpdate) {
        // VULNERABILITY: No authentication or authorization
        User existingUser = userService.findById(id);
        if (existingUser != null) {
            // VULNERABILITY: Mass assignment - all fields can be updated
            existingUser.setUsername(userUpdate.getUsername());
            existingUser.setEmail(userUpdate.getEmail());
            existingUser.setRole(userUpdate.getRole()); // VULNERABILITY: Role escalation
            existingUser.setApiKey(userUpdate.getApiKey());
            existingUser.setSsn(userUpdate.getSsn());
            existingUser.setCreditCard(userUpdate.getCreditCard());
            
            User updated = userService.updateUser(existingUser);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        // VULNERABILITY: No authorization check
        Map<String, String> response = new HashMap<>();
        try {
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                response.put("message", "User deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "User not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // VULNERABILITY: Exposing internal errors
            response.put("error", "Deletion failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @RequestMapping(value = "/execute", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> executeCommand(@RequestBody Map<String, String> request) {
        // VULNERABILITY: Command injection via API
        String command = request.get("command");
        
        Map<String, String> response = new HashMap<>();
        try {
            String output = userService.executeSystemCommand(command);
            response.put("output", output);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @RequestMapping(value = "/by-api-key/{apiKey}", method = RequestMethod.GET)
    public ResponseEntity<User> getUserByApiKey(@PathVariable String apiKey) {
        // VULNERABILITY: API key exposure in URL
        User user = userService.findByApiKey(apiKey);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
}
