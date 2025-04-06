package com.epam.training.spring_boot_epam.repository.impl;

import com.epam.training.spring_boot_epam.domain.Training;
import com.epam.training.spring_boot_epam.repository.TrainingDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class TrainingDaoImpl implements TrainingDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Training save(Training training) {
        if (training.getId() == null) {
            entityManager.persist(training);
        }

        training = entityManager.merge(training);

        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Training.class, id));
    }

    @Override
    public void update(Training training) {
        entityManager.merge(training);
    }

    @Override
    public void delete(Training training) {
        entityManager.remove(entityManager.contains(training) ? training : entityManager.merge(training));
    }

    @Override
    public List<Training> findAll() {
        return entityManager.createQuery("SELECT t FROM Training t", Training.class)
                .getResultList();
    }
}