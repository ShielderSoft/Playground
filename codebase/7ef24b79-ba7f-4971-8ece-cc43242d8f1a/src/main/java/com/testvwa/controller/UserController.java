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
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @RequestMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "profile";
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public String updateProfile(@RequestParam Long userId,
                               @RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String email,
                               @RequestParam(required = false) String ssn,
                               @RequestParam(required = false) String creditCard,
                               HttpServletRequest request,
                               Model model) {
        
        try {
            // VULNERABILITY: Insecure Direct Object Reference - no authorization check
            User user = userService.findById(userId);
            if (user != null) {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(email);
                user.setSsn(ssn);
                user.setCreditCard(creditCard);
                
                userService.updateUser(user);
                model.addAttribute("success", "Profile updated successfully!");
                model.addAttribute("user", user);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Update failed: " + e.getMessage());
        }
        
        return "profile";
    }

    @RequestMapping("/search")
    public String searchUsers(@RequestParam(required = false) String q, Model model) {
        if (q != null && !q.isEmpty()) {
            // VULNERABILITY: No input sanitization
            List<User> users = userService.searchUsers(q);
            model.addAttribute("users", users);
            model.addAttribute("query", q);
        }
        return "user-search";
    }

    @RequestMapping("/view/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        // VULNERABILITY: No access control - anyone can view any user
        User user = userService.findById(id);
        if (user != null) {
            model.addAttribute("viewUser", user);
            // VULNERABILITY: Exposing sensitive data
            logger.info("User profile viewed: " + user.getUsername() + 
                       " SSN: " + user.getSsn() + " Credit Card: " + user.getCreditCard());
        }
        return "user-view";
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public String deleteUser(@PathVariable Long id) {
        // VULNERABILITY: No authorization check - anyone can delete any user
        try {
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                return "User deleted successfully";
            } else {
                return "User not found";
            }
        } catch (Exception e) {
            // VULNERABILITY: Exposing error details
            return "Error: " + e.getMessage();
        }
    }

    @RequestMapping("/admin")
    public String adminPanel(Model model) {
        // VULNERABILITY: No role-based access control
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin";
    }

    @RequestMapping("/exec")
    @ResponseBody
    public String executeCommand(@RequestParam String cmd) {
        // VULNERABILITY: Command injection
        return userService.executeSystemCommand(cmd);
    }
}
