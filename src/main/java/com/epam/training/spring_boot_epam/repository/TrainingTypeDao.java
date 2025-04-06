package com.epam.training.spring_boot_epam.repository;

import com.epam.training.spring_boot_epam.domain.TrainingType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingTypeDao {
    Optional<TrainingType> findById(Long id);
    List<TrainingType> findAll();
}