package com.testvwa.controller;

import com.testvwa.model.User;
import com.testvwa.service.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public String home() {
        return "index";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginPost(@RequestParam String username, 
                           @RequestParam String password,
                           HttpServletRequest request,
                           Model model) {
        
        // VULNERABILITY: Logging credentials
        logger.info("Login attempt for user: " + username + " with password: " + password);
        
        try {
            User user = userService.authenticate(username, password);
            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                session.setAttribute("username", username);
                
                // VULNERABILITY: Session fixation - not regenerating session ID
                return "redirect:/dashboard";
            } else {
                // VULNERABILITY: Information disclosure
                model.addAttribute("error", "Invalid username or password for user: " + username);
                return "login";
            }
        } catch (Exception e) {
            // VULNERABILITY: Exposing stack traces to users
            model.addAttribute("error", "Login error: " + e.getMessage());
            logger.error("Login error", e);
            return "login";
        }
    }

    @RequestMapping("/register")
    public String register() {
        return "register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String registerPost(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String email,
                              @RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam(required = false) String ssn,
                              @RequestParam(required = false) String creditCard,
                              Model model) {
        
        try {
            // VULNERABILITY: No input validation
            // VULNERABILITY: Storing sensitive data
            User user = new User(username, password, email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setSsn(ssn); // VULNERABILITY: Storing SSN without encryption
            user.setCreditCard(creditCard); // VULNERABILITY: Storing credit card without encryption
            
            userService.createUser(user);
            
            model.addAttribute("success", "User registered successfully!");
            return "login";
            
        } catch (Exception e) {
            // VULNERABILITY: Detailed error messages
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // VULNERABILITY: Not invalidating session properly
            session.removeAttribute("user");
            session.removeAttribute("username");
        }
        return "redirect:/";
    }

    @RequestMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        
        // VULNERABILITY: Exposing sensitive data in logs
        logger.info("Dashboard accessed by user: " + user.getUsername() + 
                   " with API key: " + user.getApiKey());
        
        return "dashboard";
    }
}
