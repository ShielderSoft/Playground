package com.testvwa.service;

import com.testvwa.model.User;
import com.testvwa.repository.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        // VULNERABILITY: No input validation
        // VULNERABILITY: Password stored in plain text
        logger.info("Creating user: " + user.getUsername()); // VULNERABILITY: Logging sensitive data
        
        // Generate weak API key
        user.setApiKey(generateWeakApiKey());
        
        return userRepository.save(user);
    }

    public User authenticate(String username, String password) {
        // VULNERABILITY: Logging credentials
        logger.debug("Authenticating user: " + username + " with password: " + password);
        
        User user = userRepository.authenticateUser(username, password);
        if (user != null) {
            logger.info("User authenticated successfully: " + username);
        } else {
            logger.warn("Authentication failed for user: " + username);
        }
        return user;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> searchUsers(String searchTerm) {
        // VULNERABILITY: No input sanitization
        return userRepository.searchUsers(searchTerm);
    }

    public boolean deleteUser(Long userId) {
        try {
            User user = userRepository.findById(userId);
            if (user != null) {
                userRepository.delete(user);
                return true;
            }
        } catch (Exception e) {
            // VULNERABILITY: Exposing stack traces
            logger.error("Error deleting user: " + e.getMessage(), e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
        return false;
    }

    public User updateUser(User user) {
        // VULNERABILITY: No authorization check - anyone can update any user
        return userRepository.save(user);
    }

    private String generateWeakApiKey() {
        // VULNERABILITY: Weak API key generation
        Random random = new Random(System.currentTimeMillis());
        return "api_" + random.nextInt(100000);
    }

    public User findByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey);
    }

    public String executeSystemCommand(String command) {
        // VULNERABILITY: Command injection
        try {
            logger.info("Executing system command: " + command);
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        } catch (Exception e) {
            logger.error("Command execution failed", e);
            return "Error: " + e.getMessage();
        }
    }

    public String hashPassword(String password) {
        try {
            // VULNERABILITY: Using MD5 for password hashing
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return password; // VULNERABILITY: Fallback to plain text
        }
    }
}
