package com.epam.training.spring_boot_epam.service;


import com.epam.training.spring_boot_epam.domain.Trainee;
import com.epam.training.spring_boot_epam.domain.Trainer;
import com.epam.training.spring_boot_epam.domain.Training;
import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.domain.User;
import com.epam.training.spring_boot_epam.dto.filters.TraineeTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.filters.TrainerTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.dto.response.TraineeFilterResponseDTO;
import com.epam.training.spring_boot_epam.dto.response.TrainerFilterResponseDTO;
import com.epam.training.spring_boot_epam.exception.AuthorizationException;
import com.epam.training.spring_boot_epam.mapper.TrainingMapper;
import com.epam.training.spring_boot_epam.repository.TraineeDao;
import com.epam.training.spring_boot_epam.repository.TrainerDao;
import com.epam.training.spring_boot_epam.repository.TrainingDao;
import com.epam.training.spring_boot_epam.repository.UserDao;
import com.epam.training.spring_boot_epam.service.impl.TrainingServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.epam.training.spring_boot_epam.util.DomainUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTests {

    @Mock
    private TrainingDao trainingDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UserDao userDao;
    @Mock
    private TrainingMapper trainingMapper;
    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private DomainUtils domainUtils;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Trainee trainee;
    private Trainer trainer;
    private Training training;
    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        User traineeUser = new User("John", "Doe", true);
        traineeUser.setUsername("john_doe");
        traineeUser.setPassword("secret");
        trainee = new Trainee(traineeUser, null, null);
        trainee.setId(1L);
        trainee.setTrainings(new ArrayList<>());

        User trainerUser = new User("Jane", "Smith", true);
        trainerUser.setUsername("jane_smith");
        trainerUser.setPassword("secret");
        trainingType = new TrainingType(1L, "Yoga");
        trainer = new Trainer(trainerUser, trainingType);
        trainer.setId(2L);
        trainer.setTrainings(new ArrayList<>());

        training = new Training();
        training.setId(1L);
        training.setTrainingName("Morning Yoga");
        training.setTrainingDateTime(LocalDateTime.of(2023, 10, 1, 9, 0));
        training.setTrainingDurationInMinutes(1);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);
    }

    @Test
    void getTraineeTrainings_WhenTrainingsExist_ShouldReturnTraineeFilterResponseDTOs() {
        TraineeTrainingsFilter filter = new TraineeTrainingsFilter("john_doe", null, null, "Jane", "Yoga");
//        when(userDao.findByUsername("john_doe")).thenReturn(Optional.of(trainee.getUser()));
        when(traineeDao.findTraineeTrainings("john_doe", "john_doe", null, null, "Jane", "Yoga")).thenReturn(List.of(training));
        when(domainUtils.getCurrentUser()).thenReturn(trainee.getUser());

        ApiResponse<List<TraineeFilterResponseDTO>> response =
                trainingService.getTraineeTrainings(filter);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).hasSize(1);
        TraineeFilterResponseDTO dto = response.getData().get(0);
        assertThat(dto.getTrainingName()).isEqualTo("Morning Yoga");
        verify(traineeDao).findTraineeTrainings("john_doe", "john_doe",null, null, "Jane", "Yoga");
    }

    @Test
    void getTraineeTrainings_WhenNoTrainingsExist_ShouldReturnEmptyList() {
        TraineeTrainingsFilter filter = new TraineeTrainingsFilter("john_doe", null, null, null, null);
//        when(userDao.findByUsername("john_doe")).thenReturn(Optional.of(trainee.getUser()));
        when(traineeDao.findTraineeTrainings("john_doe", "john_doe",null, null, null, null)).thenReturn(Collections.emptyList());
        when(domainUtils.getCurrentUser()).thenReturn(trainee.getUser());

        ApiResponse<List<TraineeFilterResponseDTO>> response =
                trainingService.getTraineeTrainings(filter);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEmpty();
        verify(traineeDao).findTraineeTrainings("john_doe", "john_doe",null, null, null, null);
    }

    @Test
    void getTrainerTrainings_WhenTrainingsExist_ShouldReturnTrainerFilterResponseDTOs() {
        TrainerTrainingsFilter filter = new TrainerTrainingsFilter("jane_smith", null, null, "John");
        when(domainUtils.getCurrentUser()).thenReturn(trainer.getUser());

//        when(userDao.findByUsername("jane_smith")).thenReturn(Optional.of(trainer.getUser()));
        when(trainerDao.findTrainerTrainings("jane_smith", "jane_smith",null, null, "John")).thenReturn(List.of(training));

        ApiResponse<List<TrainerFilterResponseDTO>> response =
                trainingService.getTrainerTrainings(filter);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).hasSize(1);
        TrainerFilterResponseDTO dto = response.getData().get(0);
        assertThat(dto.getTraineeFirstname()).isEqualTo("John");
        verify(trainerDao).findTrainerTrainings("jane_smith", "jane_smith",null, null, "John");
    }

    @Test
    void getTrainerTrainings_WhenNoTrainingsExist_ShouldReturnEmptyList() {
        TrainerTrainingsFilter filter = new TrainerTrainingsFilter("jane_smith", null, null, null);
//        when(userDao.findByUsername("jane_smith")).thenReturn(Optional.of(trainer.getUser()));
        when(trainerDao.findTrainerTrainings("jane_smith", "jane_smith",null, null, null)).thenReturn(Collections.emptyList());
        when(domainUtils.getCurrentUser()).thenReturn(trainer.getUser());

        ApiResponse<List<TrainerFilterResponseDTO>> response =
                trainingService.getTrainerTrainings(filter);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEmpty();
        verify(trainerDao).findTrainerTrainings("jane_smith", "jane_smith",null, null, null);
    }
}