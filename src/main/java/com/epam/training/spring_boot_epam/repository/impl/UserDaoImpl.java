package com.epam.training.spring_boot_epam.repository.impl;

import com.epam.training.spring_boot_epam.domain.User;
import com.epam.training.spring_boot_epam.repository.UserDao;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@Repository
@Transactional
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {
    @PersistenceContext
    private EntityManager em;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findByUsername(String username) {
        return em.createQuery("SELECT u FROM app_users u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            em.persist(user);
        } else {
            em.merge(user);
        }
        return user;
    }

    @Override
    public boolean existsByUsernameAndPassword(String username, String password) {
        Optional<User> userOptional = findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return password.equals(user.getPassword());
        }

        return false;
    }

    @Override
    public boolean existsByUsername(String username) {
        Optional<User> userOptional = findByUsername(username);

        if (userOptional.isPresent()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean updatePassword(String username, String newPassword) {

        int updatedRows = em.createQuery("UPDATE app_users u SET u.password = :newPassword WHERE u.username = :username")
                .setParameter("newPassword", newPassword)
                .setParameter("username", username)
                .executeUpdate();

        return updatedRows > 0;
    }
}
