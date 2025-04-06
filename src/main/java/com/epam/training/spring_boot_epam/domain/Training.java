package com.epam.training.spring_boot_epam.domain;

import com.epam.training.spring_boot_epam.domain.enumeration.DomainStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trainings")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @ManyToOne
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @ManyToOne
    @JoinColumn(name = "training_type_id")
    private TrainingType trainingType;

    @Column(name = "training_date_time", nullable = false)
    private LocalDateTime trainingDateTime;

    @Column(name = "training_duration_in_hours", nullable = false)
    private Integer trainingDurationInHours;

    @Enumerated(EnumType.STRING)
    private DomainStatus status = DomainStatus.ACTIVE;

    public Training(Long id) {
        this.id = id;
    }

    public Training(Trainer trainer, Trainee trainee, String trainingName, TrainingType trainingType, LocalDateTime trainingDateTime, int trainingDurationInHours) {
        this.trainer = trainer;
        this.trainee = trainee;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDateTime = trainingDateTime;
        this.trainingDurationInHours = trainingDurationInHours;
    }
}
