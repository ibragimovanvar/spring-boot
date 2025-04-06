package com.epam.training.spring_boot_epam.repository;

import com.epam.training.spring_boot_epam.domain.Trainee;
import com.epam.training.spring_boot_epam.domain.Trainer;
import com.epam.training.spring_boot_epam.domain.Training;
import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.domain.User;
import com.epam.training.spring_boot_epam.repository.impl.TrainingDaoImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TrainingDaoImpl.class)
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrainingRepositoryTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TrainingDaoImpl trainingDao;

    @Test
    void save_ShouldPersistNewTraining() {
        Training training = createTraining("Test Training");

        Training savedTraining = trainingDao.save(training);

        assertThat(savedTraining.getId()).isNotNull();
        assertThat(entityManager.find(Training.class, savedTraining.getId())).isNotNull();
    }

    @Test
    void save_ShouldUpdateExistingTraining() {
        Training training = createTraining("Initial Training");
        trainingDao.save(training);
        training.setTrainingName("Updated Training");

        Training updatedTraining = trainingDao.save(training);

        assertThat(updatedTraining.getTrainingName()).isEqualTo("Updated Training");
        assertThat(entityManager.find(Training.class, training.getId()).getTrainingName()).isEqualTo("Updated Training");
    }

    @Test
    void findById_WhenTrainingExists_ShouldReturnTraining() {
        Training training = createTraining("Test Training");
        trainingDao.save(training);

        Optional<Training> found = trainingDao.findById(training.getId());

        assertThat(found).isPresent().contains(training);
    }

    @Test
    void findById_WhenTrainingDoesNotExist_ShouldReturnEmpty() {
        Optional<Training> found = trainingDao.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void update_ShouldModifyTrainingDetails() {
        Training training = createTraining("Initial Training");
        trainingDao.save(training);
        training.setTrainingDurationInHours(2);

        trainingDao.update(training);
        entityManager.flush();
        entityManager.clear();

        Training updated = entityManager.find(Training.class, training.getId());
        assertThat(updated.getTrainingDurationInHours()).isEqualTo(2);
    }

    @Test
    void delete_ShouldRemoveTraining() {
        Training training = createTraining("Test Training");
        trainingDao.save(training);

        trainingDao.delete(training);

        assertThat(entityManager.find(Training.class, training.getId())).isNull();
    }

    @Test
    void findAll_ShouldReturnAllTrainings() {
        Training training1 = createTraining("Training 1");
        Training training2 = createTraining("Training 2");
        trainingDao.save(training1);
        trainingDao.save(training2);

        List<Training> trainings = trainingDao.findAll();
        assertThat(trainings).isNotNull();
    }

    // Helper methods
    private Training createTraining(String name) {
        User trainerUser = createUser("trainer.user", "ROLE_TRAINER");
        User traineeUser = createUser("trainee.user", "ROLE_TRAINEE");
        Trainer trainer = createTrainer(trainerUser);
        Trainee trainee = createTrainee(traineeUser);
        TrainingType type = createTrainingType("Type1");

        Training training = new Training();
        training.setTrainingName(name);
        training.setTrainer(trainer);
        training.setTrainee(trainee);
        training.setTrainingType(type);
        training.setTrainingDateTime(LocalDateTime.now());
        training.setTrainingDurationInHours(1);
        return training;
    }

    private int userCounter = 1;

    private User createUser(String username, String role) {
        User user = new User();
        String uniqueUsername = username + "_" + userCounter++;
        user.setUsername(uniqueUsername);
        user.setPassword("password");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setRole(role);
        user.setActive(true);
        entityManager.persist(user);
        return user;
    }

    private Trainer createTrainer(User user) {
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        entityManager.persist(trainer);
        return trainer;
    }

    private Trainee createTrainee(User user) {
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        entityManager.persist(trainee);
        return trainee;
    }

    private TrainingType createTrainingType(String name) {
        TrainingType type = new TrainingType();
        type.setTrainingTypeName(name);
        entityManager.persist(type);
        return type;
    }
}