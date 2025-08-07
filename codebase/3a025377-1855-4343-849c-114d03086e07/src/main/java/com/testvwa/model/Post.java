package com.testvwa.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "posts")
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content; // VULNERABILITY: No XSS protection
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;
    
    @Column
    private Date createdDate;
    
    @Column
    private Date modifiedDate;
    
    @Column
    private boolean isPublic;

    // Constructors
    public Post() {
        this.createdDate = new Date();
        this.modifiedDate = new Date();
        this.isPublic = true;
    }

    public Post(String title, String content, User author) {
        this();
        this.title = title;
        this.content = content;
        this.author = author;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Date getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(Date modifiedDate) { this.modifiedDate = modifiedDate; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
}
