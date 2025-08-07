package com.testvwa.repository;

import com.testvwa.model.User;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class UserRepository {

    @Autowired
    private SessionFactory sessionFactory;

    public User save(User user) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(user);
        return user;
    }

    public User findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return (User) session.get(User.class, id);
    }

    public User findByUsername(String username) {
        Session session = sessionFactory.getCurrentSession();
        // VULNERABILITY: SQL Injection - Direct string concatenation
        String hql = "FROM User WHERE username = '" + username + "'";
        Query query = session.createQuery(hql);
        return (User) query.uniqueResult();
    }

    public User authenticateUser(String username, String password) {
        Session session = sessionFactory.getCurrentSession();
        // VULNERABILITY: SQL Injection via concatenation
        String sql = "SELECT * FROM users WHERE username = '" + username + 
                    "' AND password = '" + password + "'";
        Query query = session.createSQLQuery(sql).addEntity(User.class);
        return (User) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<User> findAll() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("FROM User").list();
    }

    @SuppressWarnings("unchecked")
    public List<User> searchUsers(String searchTerm) {
        Session session = sessionFactory.getCurrentSession();
        // VULNERABILITY: Another SQL injection point
        String hql = "FROM User WHERE firstName LIKE '%" + searchTerm + 
                    "%' OR lastName LIKE '%" + searchTerm + "%'";
        Query query = session.createQuery(hql);
        return query.list();
    }

    public void delete(User user) {
        Session session = sessionFactory.getCurrentSession();
        session.delete(user);
    }

    public User findByApiKey(String apiKey) {
        Session session = sessionFactory.getCurrentSession();
        // VULNERABILITY: Yet another SQL injection
        String hql = "FROM User WHERE apiKey = '" + apiKey + "'";
        Query query = session.createQuery(hql);
        return (User) query.uniqueResult();
    }
}
