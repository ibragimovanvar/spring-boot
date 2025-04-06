package com.epam.training.spring_boot_epam.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trainees")
public class Trainee {

    @Id
    @Column(name = "user_id")
    private Long id;

    @MapsId
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "address")
    private String address;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Trainer> trainers = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Training> trainings = new ArrayList<>();

    public Trainee(User user, LocalDate birthDate, String address) {
        this.user = user;
        this.birthDate = birthDate;
        this.address = address;
    }

    public Trainee(Long id) {
        this.id = id;
    }
}
