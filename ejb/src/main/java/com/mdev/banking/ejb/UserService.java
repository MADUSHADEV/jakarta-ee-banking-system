package com.mdev.banking.ejb;

import com.mdev.banking.core.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.Optional;
import java.util.Set;

@Stateless
public class UserService {

    @PersistenceContext(unitName = "BankPU")
    private EntityManager em;

    /**
     * Creates a new user, storing the password in plain text.
     */
    public void createUser(String username, String password, Set<String> roles) {
        User user = new User();
        user.setUsername(username);
        // Store the password directly without hashing.
        user.setPassword(password);
        user.setRoles(roles);
        em.persist(user);
    }

    public Optional<User> findUserByUsername(String username) {
        try {
            User user = em.createNamedQuery("User.findByUsername", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public boolean validate(String username, String password) {
        try {
            User user = em.createNamedQuery("User.validate", User.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();

            return user != null;

        } catch (NoResultException e) {
            throw new NoResultException();
        }
    }
}