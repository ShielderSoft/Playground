package com.testvwa.repository;

import com.testvwa.model.Post;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class PostRepository {

    @Autowired
    private SessionFactory sessionFactory;

    public Post save(Post post) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(post);
        return post;
    }

    public Post findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return (Post) session.get(Post.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Post> findAll() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("FROM Post ORDER BY createdDate DESC").list();
    }

    @SuppressWarnings("unchecked")
    public List<Post> findByUserId(Long userId) {
        Session session = sessionFactory.getCurrentSession();
        // VULNERABILITY: SQL Injection
        String hql = "FROM Post WHERE author.id = " + userId;
        Query query = session.createQuery(hql);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Post> searchPosts(String keyword) {
        Session session = sessionFactory.getCurrentSession();
        // VULNERABILITY: SQL Injection in search
        String hql = "FROM Post WHERE title LIKE '%" + keyword + 
                    "%' OR content LIKE '%" + keyword + "%'";
        Query query = session.createQuery(hql);
        return query.list();
    }

    public void delete(Post post) {
        Session session = sessionFactory.getCurrentSession();
        session.delete(post);
    }

    @SuppressWarnings("unchecked")
    public List<Post> getPostsByQuery(String customQuery) {
        Session session = sessionFactory.getCurrentSession();
        // VULNERABILITY: Direct execution of user-provided query
        Query query = session.createQuery(customQuery);
        return query.list();
    }
}
