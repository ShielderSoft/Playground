package com.testvwa.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String email;
    
    @Column
    private String firstName;
    
    @Column
    private String lastName;
    
    @Column
    private String role;
    
    @Column
    private Date createdDate;
    
    @Column
    private String apiKey; // VULNERABILITY: API keys stored in plain text
    
    @Column
    private String ssn; // VULNERABILITY: Sensitive data without encryption
    
    @Column
    private String creditCard; // VULNERABILITY: Credit card stored in plain text

    // Constructors
    public User() {
        this.createdDate = new Date();
        this.role = "USER";
    }

    public User(String username, String password, String email) {
        this();
        this.username = username;
        this.password = password; // VULNERABILITY: Password stored in plain text
        this.email = email;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }

    public String getCreditCard() { return creditCard; }
    public void setCreditCard(String creditCard) { this.creditCard = creditCard; }
}
