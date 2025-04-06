package com.epam.training.spring_boot_epam.repository.impl;

import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.repository.TrainingTypeDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class TrainingTypeDaoImpl implements TrainingTypeDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TrainingType.class, id));
    }

    @Override
    public List<TrainingType> findAll() {
        return entityManager
                .createQuery("SELECT t FROM TrainingType t", TrainingType.class)
                .getResultList();
    }
}
