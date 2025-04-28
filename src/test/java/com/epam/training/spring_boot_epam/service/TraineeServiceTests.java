package com.epam.training.spring_boot_epam.service;

import com.epam.training.spring_boot_epam.domain.Trainee;
import com.epam.training.spring_boot_epam.domain.Trainer;
import com.epam.training.spring_boot_epam.domain.Training;
import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.domain.User;
import com.epam.training.spring_boot_epam.dto.TraineeDTO;
import com.epam.training.spring_boot_epam.dto.TrainerDTO;
import com.epam.training.spring_boot_epam.dto.request.ActivateDeactiveRequest;
import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TraineeCreateDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.exception.DomainException;
import com.epam.training.spring_boot_epam.exception.ForbiddenException;
import com.epam.training.spring_boot_epam.mapper.TraineeMapper;
import com.epam.training.spring_boot_epam.mapper.TrainerMapper;
import com.epam.training.spring_boot_epam.repository.TraineeDao;
import com.epam.training.spring_boot_epam.repository.UserDao;
import com.epam.training.spring_boot_epam.service.impl.TraineeServiceImpl;

import java.time.LocalDate;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTests {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private UserDao userDao;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DomainUtils domainUtils;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee trainee;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("John", "Doe", true);
        user.setUsername("john_doe");
        user.setPassword("password123");
        trainee = new Trainee(user, LocalDate.of(1990, 1, 1), "123 Main St");
        trainee.setId(1L);
    }

    @Test
    void createProfile_ShouldCreateTraineeAndReturnAuthDTO() {
        TraineeCreateDTO dto = new TraineeCreateDTO("John", "Doe", "123 Main St", LocalDate.of(1990, 1, 1));
        when(userDao.existsByUsername(anyString())).thenReturn(false).thenReturn(true); // Simulate username generation

        Trainee savedTrainee = new Trainee(new User("John", "Doe", true), LocalDate.of(1990, 1, 1), "123 Main St");
        savedTrainee.getUser().setUsername("john_doe");
        savedTrainee.getUser().setPassword("Ab1Cd2Ef3G"); // 10-character password
        when(traineeDao.save(any(Trainee.class))).thenReturn(savedTrainee);

        ApiResponse<AuthDTO> response = traineeService.createProfile(dto);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getUsername()).startsWith("john_doe");
        assertThat(response.getData().getPassword()).hasSize(10);
        verify(traineeDao, times(1)).save(any(Trainee.class));
    }

    @Test
    void getProfile_WhenTraineeExists_ShouldReturnTraineeDTO() {
        when(domainUtils.getCurrentUser()).thenReturn(user);
        when(traineeDao.findByUsername("john_doe")).thenReturn(Optional.of(trainee));
        TraineeDTO traineeDTO = new TraineeDTO();
        traineeDTO.setFirstName("john_doe");
        when(traineeMapper.toDto(trainee)).thenReturn(traineeDTO);

        ApiResponse<TraineeDTO> response = traineeService.getProfile("john_doe");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getFirstName()).isEqualTo("john_doe");
        assertThat(response.getData().getTrainerList()).isEmpty();
        verify(traineeMapper, times(1)).toDto(trainee);
    }

    @Test
    void getProfile_WhenTraineeDoesNotExist_ShouldThrowDomainException() {
        when(domainUtils.getCurrentUser()).thenReturn(user);
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class, () -> traineeService.getProfile("unknown"));
        assertThat(exception.getMessage()).isEqualTo("Trainee not found: unknown");
    }

    @Test
    void checkAuthProfile_WithUsername_WhenValid_ShouldNotThrowException() {
        when(traineeDao.findByUsername("john_doe")).thenReturn(Optional.of(trainee));

        traineeService.checkAuthProfile("john_doe", "password123", "john_doe");

        verify(traineeDao, times(1)).findByUsername("john_doe");
    }

    @Test
    void checkAuthProfile_WithUsername_WhenInvalid_ShouldThrowDomainException() {
        when(traineeDao.findByUsername("john_doe")).thenReturn(Optional.of(trainee));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> traineeService.checkAuthProfile("john_doe", "wrongpassword", "john_doe"));
        assertThat(exception.getMessage()).isEqualTo("You dont have permission to access this trainee");
    }

    @Test
    void checkAuthProfile_WithId_WhenValid_ShouldNotThrowException() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        traineeService.checkAuthProfile("john_doe", "password123", 1L);

        verify(traineeDao, times(1)).findById(1L);
    }

    @Test
    void checkAuthProfile_WithId_WhenInvalid_ShouldThrowForbiddenException() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> traineeService.checkAuthProfile("john_doe", "wrongpassword", 1L));
        assertThat(exception.getMessage()).isEqualTo("You dont have permission to access this trainee");
    }

    @Test
    void updateProfile_WhenTraineeExists_ShouldUpdateAndReturnTraineeDTO() {
        TraineeDTO dto = new TraineeDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setAddress("456 Elm St");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));

        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeDao.findAllTraineeTrainers(1L)).thenReturn(Collections.emptyList());
        when(traineeMapper.toDto(trainee)).thenReturn(dto);

        ApiResponse<TraineeDTO> response = traineeService.updateProfile(dto, 1L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getFirstName()).isEqualTo("Jane");
        assertThat(response.getData().getAddress()).isEqualTo("456 Elm St");
        verify(traineeDao, times(1)).update(trainee);
    }

    @Test
    void updateProfile_WhenTraineeDoesNotExist_ShouldThrowDomainException() {
        TraineeDTO dto = new TraineeDTO();
        DomainException exception = assertThrows(DomainException.class, () -> traineeService.updateProfile(dto, 1L));
        assertThat(exception.getMessage()).isEqualTo("Trainee not found: 1");
    }

    @Test
    void deleteTraineeProfile_WhenTraineeExists_ShouldDeleteAndReturnSuccess() {
        when(traineeDao.existsByUsername("john_doe")).thenReturn(true);

        ApiResponse<Void> response = traineeService.deleteTraineeProfile("john_doe");

        assertThat(response.isSuccess()).isTrue();
        verify(traineeDao, times(1)).deleteByUsername("john_doe");
    }

    @Test
    void deleteTraineeProfile_WhenTraineeDoesNotExist_ShouldThrowDomainException() {
        when(traineeDao.existsByUsername("unknown")).thenReturn(false);

        DomainException exception = assertThrows(DomainException.class, () -> traineeService.deleteTraineeProfile("unknown"));
        assertThat(exception.getMessage()).isEqualTo("Trainee not found: unknown");
    }

    @Test
    void activateOrDeactivate_WhenTraineeExists_ShouldUpdateStatus() {
        ActivateDeactiveRequest request = new ActivateDeactiveRequest("john_doe", false);
        when(traineeDao.existsByUsername("john_doe")).thenReturn(true);
        when(traineeDao.findByUsername("john_doe")).thenReturn(Optional.of(trainee));

        ApiResponse<Void> response = traineeService.activateOrDeactivate(request);

        assertThat(response.isSuccess()).isTrue();
        verify(traineeDao, times(1)).update(trainee);
        assertThat(trainee.getUser().getActive()).isFalse();
    }

    @Test
    void getNotAssignedActiveTrainers_WhenTrainersExist_ShouldReturnTrainerDTOs() {
        Trainer trainer = new Trainer();
        trainer.setId(2L);
        User trainerUser = new User("Trainer", "One", true);
        trainerUser.setUsername("trainer_one");
        trainer.setUser(trainerUser);
        trainer.setSpecialization(new TrainingType(1L, "Yoga"));
        when(traineeDao.findAvailableTrainersForTrainee("john_doe")).thenReturn(List.of(trainer));

        ApiResponse<List<TrainerDTO>> response = traineeService.getNotAssignedActiveTrainers("john_doe");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getUsername()).isEqualTo("trainer_one");
    }

    @Test
    void getTraineeTrainings_ShouldReturnTrainings() {
        Training training = new Training();

        when(domainUtils.getCurrentUser()).thenReturn(trainee.getUser());
        when(traineeDao.findTraineeTrainings(eq("john_doe"), anyString(), isNull(), isNull(), isNull(), isNull())).thenReturn(List.of(training));

        List<Training> trainings = traineeService.getTraineeTrainings("john_doe", null, null, null, null);

        assertThat(trainings).hasSize(1);
        verify(traineeDao, times(1)).findTraineeTrainings("john_doe",  "john_doe", null, null, null, null);
    }

    @Test
    void updateTraineeTrainers_ShouldCallDaoMethod() {
        List<String> trainerUsernames = List.of("trainer_one", "trainer_two");

        traineeService.updateTraineeTrainers("john_doe", trainerUsernames);

        verify(traineeDao, times(1)).updateTraineeTrainers("john_doe", trainerUsernames);
    }
}