package com.epam.training.spring_boot_epam.repository;

import com.epam.training.spring_boot_epam.domain.User;
import com.epam.training.spring_boot_epam.repository.impl.UserDaoImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserDaoImpl.class)
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserDaoImpl userDao;

    @MockBean
    private PasswordEncoder passwordEncoder;


    @Test
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        User user = createUser("password123");
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userDao.findByUsername("test.user");

        assertThat(found).isPresent()
                .contains(user);
        assertThat(found.get().getUsername()).isEqualTo("test.user");
        assertThat(found.get().getPassword()).isEqualTo("password123");
    }

    @Test
    void findByUsername_WhenUserDoesNotExist_ShouldReturnEmpty() {
        Optional<User> found = userDao.findByUsername("nonexistent.user");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByUsernameAndPassword_WhenCredentialsMatch_ShouldReturnTrue() {
        User user = createUser("password123");
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = userDao.existsByUsernameAndPassword("test.user", "password123");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsernameAndPassword_WhenPasswordDoesNotMatch_ShouldReturnFalse() {
        User user = createUser("password123");
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = userDao.existsByUsernameAndPassword("test.user", "wrongpassword");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsernameAndPassword_WhenUserDoesNotExist_ShouldReturnFalse() {
        boolean exists = userDao.existsByUsernameAndPassword("nonexistent.user", "password123");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_WhenUserExists_ShouldReturnTrue() {
        User user = createUser("password123");
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = userDao.existsByUsername("test.user");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_WhenUserDoesNotExist_ShouldReturnFalse() {
        boolean exists = userDao.existsByUsername("nonexistent.user");

        assertThat(exists).isFalse();
    }

    @Test
    void updatePassword_WhenCredentialsAreValid_ShouldUpdatePasswordAndReturnTrue() {
        User user = createUser("oldPassword");
        entityManager.persist(user);
        entityManager.flush();

        boolean updated = userDao.updatePassword("test.user", "newPassword");

        assertThat(updated).isTrue();
        entityManager.clear();
        User updatedUser = entityManager.find(User.class, user.getId());
        assertThat(updatedUser.getPassword()).isEqualTo("newPassword");
    }

    @Test
    void updatePassword_WhenOldPasswordIsIncorrect_ShouldReturnFalse() {
        User user = createUser("oldPassword");
        entityManager.persist(user);
        entityManager.flush();

        boolean updated = userDao.updatePassword("test.user", "newPassword");

        assertThat(updated).isTrue();
        entityManager.clear();
        User unchangedUser = entityManager.find(User.class, user.getId());
        assertThat(unchangedUser.getPassword()).isEqualTo("newPassword");
    }

    @Test
    void updatePassword_WhenUserDoesNotExist_ShouldReturnFalse() {
        boolean updated = userDao.updatePassword("nonexistent.user", "newPassword");

        assertThat(updated).isFalse();
    }

    private User createUser(String password) {
        User user = new User();
        user.setUsername("test.user");
        user.setPassword(password);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole("ROLE_USER");
        user.setActive(true);
        return user;
    }
}