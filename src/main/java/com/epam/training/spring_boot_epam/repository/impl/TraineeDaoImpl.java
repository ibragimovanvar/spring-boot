package com.epam.training.spring_boot_epam.repository.impl;

import com.epam.training.spring_boot_epam.domain.Trainer;
import com.epam.training.spring_boot_epam.domain.Training;
import com.epam.training.spring_boot_epam.domain.Trainee;
import com.epam.training.spring_boot_epam.repository.TraineeDao;
import com.epam.training.spring_boot_epam.repository.jpa.TraineeJpaRepository;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
@Transactional
public class TraineeDaoImpl implements TraineeDao {

    private final TraineeJpaRepository traineeJpaRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public TraineeDaoImpl(TraineeJpaRepository traineeJpaRepository) {
        this.traineeJpaRepository = traineeJpaRepository;
    }

    @Override
    @Transactional(readOnly = false)
    public Trainee save(Trainee trainee) {
        trainee.getUser().setRole("ROLE_TRAINEE");
        entityManager.persist(trainee);
        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainee.class, id));
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        return entityManager.createQuery("SELECT t FROM Trainee t WHERE t.user.username = :username", Trainee.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public void update(Trainee trainee) {
        entityManager.merge(trainee);
    }

    @Override
    public void delete(Trainee trainee) {
        entityManager.remove(entityManager.contains(trainee) ? trainee : entityManager.merge(trainee));
    }

    @Override
    public void deleteByUsername(String username) {
        Trainee trainee = findByUsername(username).get();

        for (Training training : trainee.getTrainings()) {
            entityManager.createNativeQuery(
                    "DELETE FROM trainers_trainings WHERE trainings_id = ?"
            ).setParameter(1, training.getId()).executeUpdate();
        }

        entityManager.createNativeQuery(
                "DELETE FROM trainers_trainees WHERE trainees_user_id IN (SELECT t.user_id FROM trainees t JOIN app_users u ON t.user_id = u.id WHERE u.username = ?)"
        ).setParameter(1, username).executeUpdate();

        entityManager.createNativeQuery(
                "DELETE FROM trainees_trainings WHERE trainee_user_id IN (SELECT t.user_id FROM trainees t JOIN app_users u ON t.user_id = u.id WHERE u.username = ?)"
        ).setParameter(1, username).executeUpdate();

        entityManager.createNativeQuery(
                "DELETE FROM trainings WHERE trainee_id IN (SELECT t.user_id FROM trainees t JOIN app_users u ON t.user_id = u.id WHERE u.username = ?)"
        ).setParameter(1, username).executeUpdate();

        entityManager.createNativeQuery(
                "DELETE FROM jwt_tokens WHERE username = ?"
        ).setParameter(1, username).executeUpdate();


        traineeJpaRepository.delete(trainee);
    }

    @Override
    public List<Trainee> findAll() {
        return entityManager.createQuery("SELECT t FROM Trainee t", Trainee.class)
                .getResultList();
    }

    @Override
    public List<Trainer> findAllTraineeTrainers(Long traineeId) {
        List<Trainer> trainingTrainers = entityManager.createQuery(
                        "SELECT DISTINCT tr.trainer FROM Training tr WHERE tr.trainee.id = :id", Trainer.class)
                .setParameter("id", traineeId)
                .getResultList();

        List<Trainer> trainerTrainers = entityManager.createQuery(
                        "SELECT DISTINCT tr.trainers FROM Trainee tr WHERE tr.id = :id", Trainer.class)
                .setParameter("id", traineeId)
                .getResultList();

        List<Trainer> trainers = new ArrayList<>();
        trainers.addAll(trainingTrainers);
        trainers.addAll(trainerTrainers);

        return trainers;
    }

    @Override
    public boolean existsByUsername(String username) {
        return entityManager.createQuery("SELECT COUNT(t) FROM Trainee t WHERE t.user.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult() > 0;
    }

    @Override
    public boolean existsById(Long id) {
        return entityManager.createQuery("SELECT COUNT(t) FROM Trainee t WHERE t.user.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }

    @Override
    public List<Training> findTraineeTrainings(String username, String trainerUsername, LocalDateTime fromDate, LocalDateTime toDate,
                                               String trainerName, String trainingType) {
        StringBuilder jpql = new StringBuilder("""
                    SELECT t FROM Training t
                    WHERE t.trainee.user.username = :username
                """);

        if (fromDate != null) {
            jpql.append(" AND t.trainingDateTime >= :fromDate");
        }
        if (toDate != null) {
            jpql.append(" AND t.trainingDateTime <= :toDate");
        }
        if (trainerName != null && !trainerName.isEmpty()) {
            jpql.append(" AND LOWER(t.trainer.user.firstName) LIKE :trainerName");
        }
        if (trainingType != null && !trainingType.isEmpty()) {
            jpql.append(" AND t.trainingType.trainingTypeName = :trainingType");
        }

        TypedQuery<Training> query = entityManager.createQuery(jpql.toString(), Training.class);
        query.setParameter("username", username);
        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (trainerName != null && !trainerName.isEmpty())
            query.setParameter("trainerName", "%" + trainerName.toLowerCase() + "%");
        if (trainingType != null && !trainingType.isEmpty()) query.setParameter("trainingType", trainingType);

        return query.getResultList();
    }

    @Override
    public List<Trainer> findAvailableTrainersForTrainee(String traineeUsername) {
        return entityManager.createQuery(
                        "SELECT tr FROM Trainer tr WHERE tr NOT IN (" +
                                "SELECT t.trainer FROM Training t WHERE t.trainee.user.username = :username" +
                                ")",
                        Trainer.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }

    @Override
    public void updateTraineeTrainers(String username, List<String> trainerUsernames) {
        Trainee trainee = findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Trainee not found"));
        List<Trainer> trainers = entityManager.createQuery(
                        "SELECT t FROM Trainer t WHERE t.user.username IN :usernames", Trainer.class)
                .setParameter("usernames", trainerUsernames)
                .getResultList();
        trainee.setTrainers(trainers);
        entityManager.merge(trainee);
    }
}