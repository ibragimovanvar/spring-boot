package com.epam.training.spring_boot_epam.repository;

import com.epam.training.spring_boot_epam.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDao {
    Optional<User> findByUsername(String username);
    User save(User user);
    boolean existsByUsernameAndPassword(String username, String password);
    boolean existsByUsername(String username);
    boolean updatePassword(String username, String newPassword);
}
