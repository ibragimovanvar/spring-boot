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
import com.epam.training.spring_boot_epam.exception.AuthorizationException;
import com.epam.training.spring_boot_epam.exception.DomainException;
import com.epam.training.spring_boot_epam.mapper.TrainingMapper;
import com.epam.training.spring_boot_epam.repository.TraineeDao;
import com.epam.training.spring_boot_epam.repository.TrainerDao;
import com.epam.training.spring_boot_epam.repository.TrainingDao;
import com.epam.training.spring_boot_epam.repository.UserDao;
import com.epam.training.spring_boot_epam.service.TraineeService;
import com.epam.training.spring_boot_epam.service.TrainerService;
import com.epam.training.spring_boot_epam.service.TrainingService;
import com.epam.training.spring_boot_epam.util.DomainUtils;
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
    private final DomainUtils domainUtils;

    public void checkAuthProfile(String username) {
        User user = getByUsername(username);

        if(user.getUsername().equals(domainUtils.getCurrentUser().getUsername()) && domainUtils.getCurrentUser().getPassword().equals(user.getPassword())) {
            return;
        }


        throw new AuthorizationException("You dont have permission to access this resource");
    }

    private User getByUsername(String username) {
        return userDao.findByUsername(username).orElseThrow(() -> new DomainException("User not found with username " + username));
    }

    @Override
    public ApiResponse<List<TraineeFilterResponseDTO>> getTraineeTrainings(TraineeTrainingsFilter filter) {
        LOGGER.info("Request to get trainings for trainee with username: {}", filter);

        List<Training> traineeTrainings = traineeDao.findTraineeTrainings(filter.getUsername(), domainUtils.getCurrentUser().getUsername(), filter.getFrom(), filter.getTo(), filter.getTrainerFirstname(), filter.getTrainingTypeName());

        List<TraineeFilterResponseDTO> filterResponseDTOS = traineeTrainings
                .stream()
                .map(training -> {
                    TraineeFilterResponseDTO responseDTO = new TraineeFilterResponseDTO();
                    responseDTO.setId(training.getId());
                    responseDTO.setTrainingName(training.getTrainingName());
                    responseDTO.setTrainingDateTime(training.getTrainingDateTime());
                    responseDTO.setTrainingDurationInHours(training.getTrainingDurationInMinutes());
                    responseDTO.setTrainerFirstname(training.getTrainer().getUser().getFirstName());
                    responseDTO.setTrainingType(training.getTrainingType().getTrainingTypeName());
                    return responseDTO;
                })
                .collect(Collectors.toList());

        return new ApiResponse<>(true, null, filterResponseDTOS);
    }

    @Override
    public ApiResponse<List<TrainerFilterResponseDTO>> getTrainerTrainings(TrainerTrainingsFilter filter) {
        LOGGER.info("Request to get trainings for trainer with username: {}", filter);
        List<Training> trainerTrainings = trainerDao.findTrainerTrainings(domainUtils.getCurrentUser().getUsername(), filter.getUsername(), filter.getFrom(), filter.getTo(), filter.getTraineeFirstname());

        List<TrainerFilterResponseDTO> filterResponseDTOS = trainerTrainings
                .stream()
                .map(training -> {
                    TrainerFilterResponseDTO responseDTO = new TrainerFilterResponseDTO();
                    responseDTO.setId(training.getId());
                    responseDTO.setTrainingName(training.getTrainingName());
                    responseDTO.setTrainingDateTime(training.getTrainingDateTime());
                    responseDTO.setTrainingDurationInHours(training.getTrainingDurationInMinutes());
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

        checkAuthProfile(dto.getTrainerUsername());

        Training training = trainingMapper.toEntity(dto);


        Trainee trainee = traineeService.getByUsername(dto.getTraineeUsername());
        Trainer trainer = trainerService.getByUsername(dto.getTrainerUsername());

        training.setTrainer(trainer);
        training.setTrainee(trainee);
        training.setTrainingType(trainer.getSpecialization());

        training = trainingDao.save(training);

        if (!trainee.getTrainings().contains(training)) {
            if(!trainee.getTrainers().contains(trainer)){
                trainee.getTrainers().add(trainer);
            }
            trainee.getTrainings().add(training);
            traineeDao.update(trainee);
        }

        if (!trainer.getTrainings().contains(training)) {
            if (!trainer.getTrainees().contains(trainee)) {
                trainer.getTrainees().add(trainee);
            }
            trainer.getTrainings().add(training);
            trainerDao.update(trainer);
        }


        LOGGER.info("Training added successfully: {}", training.getId());

        return new ApiResponse<>(true, "Successfully created", null);
    }


}