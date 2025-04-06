package com.epam.training.spring_boot_epam.repository;

import com.epam.training.spring_boot_epam.domain.*;
import com.epam.training.spring_boot_epam.repository.impl.TraineeDaoImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TraineeDaoImpl.class)
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TraineeRepositoryTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TraineeDaoImpl traineeDao;

    @Test
    void save_ShouldPersistTraineeWithRoleTrainee() {
        User user = new User();
        user.setUsername("test.user");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);

        Trainee savedTrainee = traineeDao.save(trainee);

        assertThat(savedTrainee.getUser().getRole()).isEqualTo("ROLE_TRAINEE");
        assertThat(entityManager.find(Trainee.class, savedTrainee.getId())).isNotNull();
    }

    @Test
    void findById_WhenTraineeExists_ShouldReturnTrainee() {
        User user = createUser("test.user");
        Trainee trainee = createTrainee(user);

        Optional<Trainee> found = traineeDao.findById(trainee.getId());
        assertThat(found).isPresent().contains(trainee);
    }

    @Test
    void findById_WhenTraineeDoesNotExist_ShouldReturnEmpty() {
        Optional<Trainee> found = traineeDao.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findByUsername_WhenTraineeExists_ShouldReturnTrainee() {
        User user = createUser("test.user");
        Trainee trainee = createTrainee(user);

        Optional<Trainee> found = traineeDao.findByUsername("test.user");
        assertThat(found).isPresent().contains(trainee);
    }

    @Test
    void update_ShouldModifyTraineeDetails() {
        User user = createUser("test.user");
        Trainee trainee = createTrainee(user);
        trainee.setAddress("New Address");

        traineeDao.update(trainee);
        entityManager.flush();
        entityManager.clear();

        Trainee updated = entityManager.find(Trainee.class, trainee.getId());
        assertThat(updated.getAddress()).isEqualTo("New Address");
    }

    @Test
    void delete_ShouldRemoveTrainee() {
        User user = createUser("test.user");
        Trainee trainee = createTrainee(user);

        traineeDao.delete(trainee);
        assertThat(entityManager.find(Trainee.class, trainee.getId())).isNull();
    }

//    @Test
//    void deleteByUsername_ShouldDeleteTraineeAndRelatedData() {
//        User traineeUser = createUser("trainee.user");
//        Trainee trainee = createTrainee(traineeUser);
//        Long traineeUserId = traineeUser.getId();
//
//        User trainerUser = createUser("trainer.user");
//        Trainer trainer = createTrainer(trainerUser);
//
//        TrainingType type = createTrainingType("Type1");
//        Training training = createTraining(trainee, trainer, type);
//
//        trainee.getTrainers().add(trainer);
//        entityManager.merge(trainee);
//        entityManager.flush();
//
//        traineeDao.deleteByUsername("trainee.user");
//        entityManager.flush();
//        entityManager.clear();
//
//        // Check each table separately
//        Query userQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM app_users WHERE id = ?1");
//        userQuery.setParameter(1, traineeUserId);
//        Long userCount = ((Number) userQuery.getSingleResult()).longValue();
//        System.out.println("User count: " + userCount);
//
//        Query traineeQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM trainees WHERE user_id = ?1");
//        traineeQuery.setParameter(1, traineeUserId);
//        Long traineeCount = ((Number) traineeQuery.getSingleResult()).longValue();
//        System.out.println("Trainee count: " + traineeCount);
//
//        Query trainingQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM trainings WHERE trainee_id = ?1");
//        trainingQuery.setParameter(1, traineeUserId);
//        Long trainingCount = ((Number) trainingQuery.getSingleResult()).longValue();
//        System.out.println("Training count: " + trainingCount);
//
//        Query relationQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM trainees_trainers WHERE trainee_user_id = ?1");
//        relationQuery.setParameter(1, traineeUserId);
//        Long relationCount = ((Number) relationQuery.getSingleResult()).longValue();
//        System.out.println("Relation count: " + relationCount);
//
//        // Original assertions
//        assertThat(entityManager.find(User.class, traineeUser.getId())).isNull();
//        assertThat(entityManager.find(Trainee.class, trainee.getId())).isNull();
//        assertThat(entityManager.find(Training.class, training.getId())).isNull();
//        assertThat((Number) relationQuery.getSingleResult()).isEqualTo(0L);
//    }
//
    @Test
    void findAll_ShouldReturnAllTrainees() {
        User user1 = createUser("user1");
        User user2 = createUser("user2");
        Trainee trainee1 = createTrainee(user1);
        Trainee trainee2 = createTrainee(user2);

        List<Trainee> trainees = traineeDao.findAll();
        assertThat(trainees).containsExactlyInAnyOrder(trainee1, trainee2);
    }

    @Test
    void findAllTraineeTrainers_ShouldReturnTrainersForTrainee() {
        Trainee trainee = createTrainee(createUser("trainee.user"));
        Trainer trainer1 = createTrainer(createUser("trainer1"));
        Trainer trainer2 = createTrainer(createUser("trainer2"));

        createTraining(trainee, trainer1, createTrainingType("Type1"));
        createTraining(trainee, trainer2, createTrainingType("Type2"));

        List<Trainer> trainers = traineeDao.findAllTraineeTrainers(trainee.getId());
        assertThat(trainers).containsExactlyInAnyOrder(trainer1, trainer2);
    }

    @Test
    void existsByUsername_WhenExists_ShouldReturnTrue() {
        createTrainee(createUser("test.user"));
        assertThat(traineeDao.existsByUsername("test.user")).isTrue();
    }

    @Test
    void existsById_WhenExists_ShouldReturnTrue() {
        Trainee trainee = createTrainee(createUser("test.user"));
        assertThat(traineeDao.existsById(trainee.getId())).isTrue();
    }

    @Test
    void findTraineeTrainings_ShouldFilterTrainings() {
        Trainee trainee = createTrainee(createUser("trainee.user"));
        Trainer trainer = createTrainer(createUser("trainer.user"));
        TrainingType type1 = createTrainingType("Type1");
        TrainingType type2 = createTrainingType("Type2");

        LocalDateTime date1 = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2023, 2, 1, 10, 0);
        createTraining(trainee, trainer, type1, date1);
        createTraining(trainee, trainer, type2, date2);

        List<Training> result = traineeDao.findTraineeTrainings(
                "trainee.user",
                LocalDateTime.of(2023, 1, 15, 0, 0),
                LocalDateTime.of(2023, 2, 15, 0, 0),
                null,
                null
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .extracting(Training::getTrainingType)
                .extracting(TrainingType::getTrainingTypeName).isEqualTo("Type2");
    }

    @Test
    void findAvailableTrainersForTrainee_ShouldExcludeExistingTrainers() {
        Trainee trainee = createTrainee(createUser("trainee.user"));
        Trainer trainer1 = createTrainer(createUser("trainer1"));
        Trainer trainer2 = createTrainer(createUser("trainer2"));
        createTraining(trainee, trainer1, createTrainingType("Type1"));

        List<Trainer> available = traineeDao.findAvailableTrainersForTrainee("trainee.user");
        assertThat(available).containsExactly(trainer2);
    }

    @Test
    void updateTraineeTrainers_ShouldUpdateTrainersList() {
        Trainee trainee = createTrainee(createUser("trainee.user"));
        createTrainer(createUser("trainer1"));
        createTrainer(createUser("trainer2"));

        traineeDao.updateTraineeTrainers("trainee.user", List.of("trainer1", "trainer2"));
        Trainee updated = traineeDao.findByUsername("trainee.user").get();

        assertThat(updated.getTrainers()).extracting(t -> t.getUser().getUsername())
                .containsExactlyInAnyOrder("trainer1", "trainer2");
    }

    @Test
    void updateTraineeTrainers_WhenTraineeNotFound_ShouldThrow() {
        assertThatThrownBy(() -> traineeDao.updateTraineeTrainers("unknown", List.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainee not found");
    }

    // Helper methods to create entities
    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setActive(true);
        user.setRole("ROLE_TRAINEE");

        entityManager.persist(user);
        return user;
    }

    private Trainee createTrainee(User user) {
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        entityManager.persist(trainee);
        return trainee;
    }

    private Trainer createTrainer(User user) {
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        entityManager.persist(trainer);
        return trainer;
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
        training.setTrainingName("Test training");
        training.setTrainingDateTime(date);
        training.setTrainingDurationInHours(40);

        entityManager.persist(training);
        return training;
    }
}