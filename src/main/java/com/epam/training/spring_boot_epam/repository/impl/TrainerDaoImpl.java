package com.epam.training.spring_boot_epam.repository.impl;

import com.epam.training.spring_boot_epam.domain.Trainee;
import com.epam.training.spring_boot_epam.domain.Trainer;
import com.epam.training.spring_boot_epam.domain.Training;
import com.epam.training.spring_boot_epam.domain.User;
import com.epam.training.spring_boot_epam.repository.TrainerDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class TrainerDaoImpl implements TrainerDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainer save(Trainer trainer) {
        trainer.getUser().setRole("ROLE_TRAINER");
        entityManager.persist(trainer);
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainer.class, id));
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        return entityManager.createQuery("SELECT t FROM Trainer t WHERE t.user.username = :username", Trainer.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Trainee> findAllTrainerTrainees(Long id) {
        return entityManager.createQuery(
                        "SELECT DISTINCT tr.trainee FROM Training tr WHERE tr.trainer.id = :id", Trainee.class)
                .setParameter("id", id)
                .getResultList();
    }

    @Override
    public Trainer update(Trainer trainer) {
        return entityManager.merge(trainer);
    }

    @Override
    public void delete(Trainer trainer) {
        entityManager.remove(entityManager.contains(trainer) ? trainer : entityManager.merge(trainer));
    }

    @Override
    public List<Trainer> findAll() {
        return entityManager.createQuery("SELECT t FROM Trainer t", Trainer.class)
                .getResultList();
    }

    @Override
    public boolean existsByUsername(String username) {
        return entityManager.createQuery("SELECT COUNT(t) FROM Trainer t WHERE t.user.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult() > 0;
    }

    @Override
    public List<Training> findTrainerTrainings(String username, String traineeUsername, LocalDateTime fromDate, LocalDateTime toDate, String traineeName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> cq = cb.createQuery(Training.class);
        Root<Training> root = cq.from(Training.class);
        Join<Training, Trainer> trainerJoin = root.join("trainer");
        Join<Trainer, User> trainerUserJoin = trainerJoin.join("user");
        Join<Training, Trainee> traineeJoin = root.join("trainee");
        Join<Trainee, User> traineeUserJoin = traineeJoin.join("user");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(trainerUserJoin.get("username"), username));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDateTime"), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("trainingDateTime"), toDate));
        }
        if (traineeName != null && !traineeName.isEmpty()) {
            predicates.add(cb.like(cb.lower(traineeUserJoin.get("username")), traineeUsername.toLowerCase()));
        }

        cq.select(root).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getResultList();
    }
}