package com.epam.training.spring_boot_epam.repository.jpa;

import com.epam.training.spring_boot_epam.domain.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraineeJpaRepository extends JpaRepository<Trainee, Long> {
}
