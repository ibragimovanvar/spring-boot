package com.epam.training.spring_boot_epam.service.impl;

import com.epam.training.spring_boot_epam.domain.Trainee;
import com.epam.training.spring_boot_epam.domain.Trainer;
import com.epam.training.spring_boot_epam.domain.Training;
import com.epam.training.spring_boot_epam.domain.User;
import com.epam.training.spring_boot_epam.dto.TrainingDTO;
import com.epam.training.spring_boot_epam.dto.filters.TraineeTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.filters.TrainerTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.dto.response.TraineeFilterResponseDTO;
import com.epam.training.spring_boot_epam.dto.response.TrainerFilterResponseDTO;
import com.epam.training.spring_boot_epam.exception.DomainException;
import com.epam.training.spring_boot_epam.mapper.TrainingMapper;
import com.epam.training.spring_boot_epam.repository.TraineeDao;
import com.epam.training.spring_boot_epam.repository.TrainerDao;
import com.epam.training.spring_boot_epam.repository.TrainingDao;
import com.epam.training.spring_boot_epam.repository.UserDao;
import com.epam.training.spring_boot_epam.service.TraineeService;
import com.epam.training.spring_boot_epam.service.TrainerService;
import com.epam.training.spring_boot_epam.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("trainingService")
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final TrainingDao trainingDao;
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final UserDao userDao;
    private final TrainingMapper trainingMapper;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public void checkAuthProfile(String headerUsername, String password, String username) {
        User user = getByUsername(username);

        if(user.getUsername().equals(headerUsername) && user.getPassword().equals(password)) {
            return;
        }


        throw new DomainException("Invalid username or password");
    }

    private User getByUsername(String username) {
        return userDao.findByUsername(username).orElseThrow(() -> new DomainException("Invalid username or password"));
    }

    @Override
    public ApiResponse<List<TraineeFilterResponseDTO>> getTraineeTrainings(String headerUsername, String password, TraineeTrainingsFilter filter) {
        LOGGER.info("Request to get trainings for trainee with username: {}", filter);
        List<Training> traineeTrainings = traineeDao.findTraineeTrainings(filter.getUsername(), filter.getFrom(), filter.getTo(), filter.getTrainerFirstname(), filter.getTrainingTypeName());

        checkAuthProfile(headerUsername, password, filter.getUsername());

        List<TraineeFilterResponseDTO> filterResponseDTOS = traineeTrainings
                .stream()
                .map(training -> {
                    TraineeFilterResponseDTO responseDTO = new TraineeFilterResponseDTO();
                    responseDTO.setId(training.getId());
                    responseDTO.setTrainingName(training.getTrainingName());
                    responseDTO.setTrainingDateTime(training.getTrainingDateTime());
                    responseDTO.setTrainingDurationInHours(training.getTrainingDurationInHours());
                    responseDTO.setTrainerFirstname(training.getTrainer().getUser().getFirstName());
                    responseDTO.setTrainingType(training.getTrainingType().getTrainingTypeName());
                    return responseDTO;
                })
                .collect(Collectors.toList());

        return new ApiResponse<>(true, null, filterResponseDTOS);
    }

    @Override
    public ApiResponse<List<TrainerFilterResponseDTO>> getTrainerTrainings(String headerUsername, String password, TrainerTrainingsFilter filter) {
        LOGGER.info("Request to get trainings for trainer with username: {}", filter);
        List<Training> trainerTrainings = trainerDao.findTrainerTrainings(filter.getUsername(), filter.getFrom(), filter.getTo(), filter.getTraineeFirstname());


        checkAuthProfile(headerUsername, password, filter.getUsername());

        List<TrainerFilterResponseDTO> filterResponseDTOS = trainerTrainings
                .stream()
                .map(training -> {
                    TrainerFilterResponseDTO responseDTO = new TrainerFilterResponseDTO();
                    responseDTO.setId(training.getId());
                    responseDTO.setTrainingName(training.getTrainingName());
                    responseDTO.setTrainingDateTime(training.getTrainingDateTime());
                    responseDTO.setTrainingDurationInHours(training.getTrainingDurationInHours());
                    responseDTO.setTraineeFirstname(training.getTrainee().getUser().getFirstName());
                    responseDTO.setTrainingType(training.getTrainingType() != null ? training.getTrainingType().getTrainingTypeName() : "");
                    return responseDTO;
                })
                .collect(Collectors.toList());

        return new ApiResponse<>(true, null, filterResponseDTOS);
    }

    @Transactional
    @Override
    public ApiResponse<Void> addTraining(TrainingDTO dto) {
        LOGGER.info("Request to add training: {}", dto);

        Training training = trainingMapper.toEntity(dto);


        Trainee trainee = traineeService.getByUsername(dto.getTraineeUsername());
        Trainer trainer = trainerService.getByUsername(dto.getTrainerUsername());

        training.setTrainer(trainer);
        training.setTrainee(trainee);

        training = trainingDao.save(training);

        if (!trainee.getTrainings().contains(training)) {
            trainee.getTrainings().add(training);
            traineeDao.update(trainee);
        }

        if (!trainer.getTrainings().contains(training)) {
            trainer.getTrainings().add(training);
            trainerDao.update(trainer);
        }

        LOGGER.info("Training added successfully: {}", training.getId());

        return new ApiResponse<>(true, null, null);
    }


}