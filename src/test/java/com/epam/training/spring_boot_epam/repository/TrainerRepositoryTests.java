package com.epam.training.spring_boot_epam.repository;

import com.epam.training.spring_boot_epam.domain.*;
import com.epam.training.spring_boot_epam.repository.impl.TrainerDaoImpl;
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
@Import(TrainerDaoImpl.class)
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrainerRepositoryTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TrainerDaoImpl trainerDao;

    @Test
    void save_ShouldPersistTrainerWithRoleTrainer() {
        User user = new User();
        user.setUsername("trainer.user");
        user.setPassword("password");
        user.setFirstName("Trainer");
        user.setLastName("User");
        user.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);

        Trainer savedTrainer = trainerDao.save(trainer);

        assertThat(savedTrainer.getUser().getRole()).isEqualTo("ROLE_TRAINER");
        assertThat(entityManager.find(Trainer.class, savedTrainer.getId())).isNotNull();
    }

    @Test
    void findById_WhenTrainerExists_ShouldReturnTrainer() {
        User user = createUser("trainer.user");
        Trainer trainer = createTrainer(user);

        Optional<Trainer> found = trainerDao.findById(trainer.getId());
        assertThat(found).isPresent().contains(trainer);
    }

    @Test
    void findTrainerTrainings_ShouldFilterCorrectly() {
        Trainer trainer = createTrainer(createUser("trainer.user"));
        Trainee trainee1 = createTrainee(createUser("trainee1"));
        Trainee trainee2 = createTrainee(createUser("trainee2"));
        TrainingType type = createTrainingType("Yoga");

        createTraining(trainee1, trainer, type, LocalDateTime.of(2023, 1, 1, 10, 0));
        createTraining(trainee2, trainer, type, LocalDateTime.of(2023, 2, 1, 10, 0));

        List<Training> results = trainerDao.findTrainerTrainings(
                "trainer.user",
                "",
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 3, 1, 0, 0),
                "trainee2"
        );

        assertThat(results).isEmpty();
    }

    @Test
    void findById_WhenTrainerDoesNotExist_ShouldReturnEmpty() {
        Optional<Trainer> found = trainerDao.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findByUsername_WhenTrainerExists_ShouldReturnTrainer() {
        User user = createUser("trainer.user");
        Trainer trainer = createTrainer(user);

        Optional<Trainer> found = trainerDao.findByUsername("trainer.user");
        assertThat(found).isPresent().contains(trainer);
    }

    @Test
    void findAllTrainerTrainees_ShouldReturnAssociatedTrainees() {
        Trainer trainer = createTrainer(createUser("trainer.user"));
        Trainee trainee1 = createTrainee(createUser("trainee1"));
        Trainee trainee2 = createTrainee(createUser("trainee2"));

        createTraining(trainee1, trainer, createTrainingType("Type1"));
        createTraining(trainee2, trainer, createTrainingType("Type2"));

        List<Trainee> trainees = trainerDao.findAllTrainerTrainees(trainer.getId());
        assertThat(trainees).containsExactlyInAnyOrder(trainee1, trainee2);
    }

    @Test
    void update_ShouldModifyTrainerDetails() {
        User user = createUser("trainer.user");
        Trainer trainer = createTrainer(user);
        trainer.setSpecialization(createTrainingType("New Specialization"));

        trainerDao.update(trainer);
        entityManager.flush();
        entityManager.clear();

        Trainer updated = entityManager.find(Trainer.class, trainer.getId());
        assertThat(updated.getSpecialization().getTrainingTypeName()).isEqualTo("New Specialization");
    }

    @Test
    void delete_ShouldRemoveTrainer() {
        User user = createUser("trainer.user");
        Trainer trainer = createTrainer(user);

        trainerDao.delete(trainer);
        assertThat(entityManager.find(Trainer.class, trainer.getId())).isNull();
    }

    @Test
    void findAll_ShouldReturnAllTrainers() {
        User user1 = createUser("trainer1");
        User user2 = createUser("trainer2");
        Trainer trainer1 = createTrainer(user1);
        Trainer trainer2 = createTrainer(user2);

        List<Trainer> trainers = trainerDao.findAll();
        assertThat(trainers).doesNotContainNull();
    }

    @Test
    void existsByUsername_WhenExists_ShouldReturnTrue() {
        createTrainer(createUser("trainer.user"));
        assertThat(trainerDao.existsByUsername("trainer.user")).isTrue();
    }

    @Test
    void existsByUsername_WhenNotExists_ShouldReturnFalse() {
        assertThat(trainerDao.existsByUsername("nonexistent.user")).isFalse();
    }

   /* @Test
    void findTrainerTrainings_ShouldFilterCorrectly() {
        Trainer trainer = createTrainer(createUser("trainer.user"));
        Trainee trainee1 = createTrainee(createUser("trainee1"));
        Trainee trainee2 = createTrainee(createUser("trainee2"));

        LocalDateTime date1 = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2023, 2, 1, 10, 0);
        TrainingType type = createTrainingType("Type1");

        createTraining(trainee1, trainer, type, date1);
        createTraining(trainee2, trainer, type, date2);

        List<Training> result = trainerDao.findTrainerTrainings(
                "trainer.user",
                null,
                null,
                "trainee2"
        );

        assertThat(result).hasSize(0);
        assertThat(result.get(0))
                .extracting(Training::getTrainee)
                .extracting(Trainee::getUser)
                .extracting(User::getUsername)
                .isEqualTo("trainee2");
    }
*/
    // Helper methods
    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setRole("ROLE_TRAINER");
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

    private Training createTraining(Trainee trainee, Trainer trainer, TrainingType type) {
        return createTraining(trainee, trainer, type, LocalDateTime.now());
    }

    private Training createTraining(Trainee trainee, Trainer trainer, TrainingType type, LocalDateTime date) {
        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(type);
        training.setTrainingName("Training Name");
        training.setTrainingDateTime(date);
        training.setTrainingDurationInMinutes(1);
        entityManager.persist(training);
        return training;
    }
}