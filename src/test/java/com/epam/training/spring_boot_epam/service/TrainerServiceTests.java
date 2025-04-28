package com.epam.training.spring_boot_epam.service;

import com.epam.training.spring_boot_epam.domain.Trainer;
import com.epam.training.spring_boot_epam.domain.Training;
import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.domain.User;
import com.epam.training.spring_boot_epam.dto.TrainerDTO;
import com.epam.training.spring_boot_epam.dto.request.ActivateDeactiveRequest;
import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TrainerCreateDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.exception.DomainException;
import com.epam.training.spring_boot_epam.exception.ForbiddenException;
import com.epam.training.spring_boot_epam.mapper.TraineeMapper;
import com.epam.training.spring_boot_epam.mapper.TrainerMapper;
import com.epam.training.spring_boot_epam.mapper.TrainingTypeMapper;
import com.epam.training.spring_boot_epam.repository.TrainerDao;
import com.epam.training.spring_boot_epam.repository.UserDao;
import com.epam.training.spring_boot_epam.service.impl.TrainerServiceImpl;
import com.epam.training.spring_boot_epam.util.DomainUtils;
import com.epam.training.spring_boot_epam.util.OperationTypes;
import com.epam.training.spring_boot_epam.util.StatusTypes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTests {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UserDao userDao;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TrainingTypeMapper trainingTypeMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DomainUtils domainUtils;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer trainer;
    private User user;
    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        user = new User("Jane", "Smith", true);
        user.setUsername("jane_smith");
        user.setPassword("password123");
        trainingType = new TrainingType(1L, "Yoga");
        trainer = new Trainer(user, trainingType);
        trainer.setId(1L);
    }

    @Test
    void createProfile_ShouldCreateTrainerAndReturnAuthDTO() {
        TrainerCreateDTO dto = new TrainerCreateDTO("Jane", "Smith", 1L);
        when(userDao.existsByUsername(anyString())).thenReturn(false); // No username conflict
        when(trainingTypeService.getTrainingType(1L)).thenReturn(trainingType);

        Trainer savedTrainer = new Trainer(new User("Jane", "Smith", true), trainingType);
        savedTrainer.getUser().setUsername("jane_smith");
        savedTrainer.getUser().setPassword("Xy7Kp9Mn2Q"); // 10-character password
        when(trainerDao.save(any(Trainer.class))).thenReturn(savedTrainer);

        ApiResponse<AuthDTO> response = trainerService.createProfile(dto);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getUsername()).startsWith("jane_smith");
        assertThat(response.getData().getPassword()).hasSize(10);
        verify(trainerDao, times(1)).save(any(Trainer.class));
    }

    @Test
    void getProfile_WhenTrainerExists_ShouldReturnTrainerDTO() {
        when(trainerDao.findByUsername("jane_smith")).thenReturn(Optional.of(trainer));
        TrainerDTO trainerDTO = new TrainerDTO();
        trainerDTO.setUsername("jane_smith");
        when(trainerMapper.toDto(trainer)).thenReturn(trainerDTO);
        when(trainerDao.findAllTrainerTrainees(1L)).thenReturn(Collections.emptyList());

        ApiResponse<TrainerDTO> response = trainerService.getProfile("jane_smith");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getUsername()).isEqualTo("jane_smith");
        assertThat(response.getData().getTraineeList()).isEmpty();
        verify(trainerMapper, times(1)).toDto(trainer);
    }

    @Test
    void getProfile_WhenTrainerDoesNotExist_ShouldThrowDomainException() {
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class, () -> trainerService.getProfile("unknown"));
        assertThat(exception.getMessage()).isEqualTo("Trainer not found: unknown");
    }

    @Test
    void checkAuthProfile_WithUsername_WhenValid_ShouldNotThrowException() {
        when(trainerDao.findByUsername("jane_smith")).thenReturn(Optional.of(trainer));

        trainerService.checkAuthProfile("jane_smith", "password123", "jane_smith");

        verify(trainerDao, times(1)).findByUsername("jane_smith");
    }

    @Test
    void checkAuthProfile_WithUsername_WhenInvalid_ShouldThrowDomainException() {
        when(trainerDao.findByUsername("jane_smith")).thenReturn(Optional.of(trainer));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> trainerService.checkAuthProfile("jane_smith", "wrongpassword", "jane_smith"));
        assertThat(exception.getMessage()).isEqualTo("You dont have permission to access this trainer");
    }

    @Test
    void checkAuthProfile_WithId_WhenValid_ShouldNotThrowException() {
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));

        trainerService.checkAuthProfile("jane_smith", "password123", 1L);

        verify(trainerDao, times(1)).findById(1L);
    }

    @Test
    void checkAuthProfile_WithId_WhenInvalid_ShouldThrowDomainException() {
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> trainerService.checkAuthProfile("jane_smith", "wrongpassword", 1L));
        assertThat(exception.getMessage()).isEqualTo("You dont have permission to access this trainer");
    }

    @Test
    void updateProfile_WhenTrainerExists_ShouldUpdateAndReturnTrainerDTO() {
        TrainerDTO dto = new TrainerDTO();
        dto.setFirstName("Janet");
        dto.setLastName("Smith");
        dto.setActive(true);
        dto.setTrainingTypeId(1L);

        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainingTypeService.getTrainingType(1L)).thenReturn(trainingType);
        when(trainerDao.update(trainer)).thenReturn(trainer);
        when(trainerMapper.toDto(trainer)).thenReturn(dto);
        when(trainerDao.findAllTrainerTrainees(1L)).thenReturn(Collections.emptyList());

        ApiResponse<TrainerDTO> response = trainerService.updateProfile(dto, 1L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getFirstName()).isEqualTo("Janet");
        verify(trainerDao, times(1)).update(trainer);
    }

    @Test
    void updateProfile_WhenTrainerDoesNotExist_ShouldThrowDomainException() {
        when(trainerDao.findById(1L)).thenReturn(Optional.empty());

        TrainerDTO dto = new TrainerDTO();
        DomainException exception = assertThrows(DomainException.class, () -> trainerService.updateProfile(dto, 1L));
        assertThat(exception.getMessage()).isEqualTo("Trainer not found");
    }

    @Test
    void deleteProfile_WhenTrainerExists_ShouldDeactivateAndReturnSuccess() {
        when(trainerDao.findByUsername("jane_smith")).thenReturn(Optional.of(trainer));
        when(trainerDao.update(trainer)).thenReturn(trainer);
        when(domainUtils.getMessage("Trainer", StatusTypes.MESSAGE_TYPE_SUCCESS, OperationTypes.DELETION)).thenReturn("Trainer deleted successfully");

        ApiResponse<Void> response = trainerService.deleteProfile("jane_smith");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Trainer deleted successfully");
        assertThat(trainer.getUser().getActive()).isFalse();
        verify(trainerDao, times(1)).update(trainer);
    }

    @Test
    void deleteProfile_WhenTrainerDoesNotExist_ShouldThrowDomainException() {
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class, () -> trainerService.deleteProfile("unknown"));
        assertThat(exception.getMessage()).isEqualTo("Trainer not found");
    }

    @Test
    void activateOrDeactivate_WhenTrainerExists_ShouldToggleStatus() {
        ActivateDeactiveRequest request = new ActivateDeactiveRequest("jane_smith", false);
        when(trainerDao.findByUsername("jane_smith")).thenReturn(Optional.of(trainer));
        when(trainerDao.update(trainer)).thenReturn(trainer);

        ApiResponse<Void> response = trainerService.activateOrDeactivate(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(trainer.getUser().getActive()).isFalse(); // Toggled from true to false
        verify(trainerDao, times(1)).update(trainer);
    }

    @Test
    void activateOrDeactivate_WhenTrainerDoesNotExist_ShouldThrowDomainException() {
        ActivateDeactiveRequest request = new ActivateDeactiveRequest("unknown", true);
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class, () -> trainerService.activateOrDeactivate(request));
        assertThat(exception.getMessage()).isEqualTo("Trainer not found");
    }

    @Test
    void getTrainerTrainings_WhenAuthenticated_ShouldReturnTrainings() {
        Training training = new Training();
        when(domainUtils.getCurrentUser()).thenReturn(trainer.getUser());

        when(trainerDao.findByUsername("jane_smith")).thenReturn(Optional.of(trainer));
        when(trainerDao.findTrainerTrainings("jane_smith", "jane_smith", null, null, null)).thenReturn(List.of(training));

        List<Training> trainings = trainerService.getTrainerTrainings("jane_smith", null, null, null);

        assertThat(trainings).hasSize(1);
        verify(trainerDao, times(1)).findTrainerTrainings("jane_smith", "jane_smith", null, null, null);
    }

    @Test
    void getTrainerTrainings_WhenNotAuthenticated_ShouldThrowSecurityException() {
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(SecurityException.class, () -> trainerService.getTrainerTrainings("unknown", null, null, null));
    }
}
