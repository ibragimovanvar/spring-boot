package com.epam.training.spring_boot_epam.repository;

import com.epam.training.spring_boot_epam.domain.Trainee;
import com.epam.training.spring_boot_epam.domain.Trainer;
import com.epam.training.spring_boot_epam.domain.Training;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerDao {
    Trainer save(Trainer trainer);

    Optional<Trainer> findById(Long id);

    Optional<Trainer> findByUsername(String username);
    List<Trainee> findAllTrainerTrainees(Long id);

    Trainer update(Trainer trainer);

    void delete(Trainer trainer);

    List<Trainer> findAll();

    boolean existsByUsername(String username);

    List<Training> findTrainerTrainings(String username, String traineeUsername, LocalDateTime fromDate, LocalDateTime toDate, String traineeName);
}