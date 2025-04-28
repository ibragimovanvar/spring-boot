package com.epam.training.spring_boot_epam.service.impl;

import com.epam.training.spring_boot_epam.domain.*;
import com.epam.training.spring_boot_epam.dto.TraineeDTO;
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
import com.epam.training.spring_boot_epam.service.TrainerService;
import com.epam.training.spring_boot_epam.service.TrainingTypeService;
import com.epam.training.spring_boot_epam.util.DomainUtils;
import com.epam.training.spring_boot_epam.util.OperationTypes;
import com.epam.training.spring_boot_epam.util.StatusTypes;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service("trainerService")
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerServiceImpl.class);
    private static final String ENTITY_NAME = "Trainer";
    private final TrainerDao trainerDao;
    private final TrainerMapper trainerMapper;
    private final DomainUtils domainUtils;
    private final TrainingTypeService trainingTypeService;
    private final UserDao userDao;
    private final TraineeMapper traineeMapper;
    private final PasswordEncoder passwordEncoder;
    private final TrainingTypeMapper trainingTypeMapper;

    @Transactional
    @Override
    public ApiResponse<AuthDTO> createProfile(TrainerCreateDTO dto) {
        LOGGER.info("Request to create {} profile with data: {}",
                ENTITY_NAME, dto);

        String generatedPassword = generatePassword();

        Trainer trainer = new Trainer(new User(dto.getFirstName(), dto.getLastName(), true), trainingTypeService.getTrainingType(dto.getTrainingTypeId()));
        trainer.getUser().setUsername(generateUsername(dto.getFirstName(), dto.getLastName()));
        trainer.getUser().setPassword(passwordEncoder.encode(generatedPassword));

        Trainer savedTrainer = trainerDao.save(trainer);

        AuthDTO authDTO = new AuthDTO(savedTrainer.getUser().getUsername(), generatedPassword);

        return new ApiResponse<>(true, null, authDTO);
    }

    @Override
    public void checkAuthProfile(String headerUsername, String password, String username) {
        Trainer trainer = getByUsername(username);

        if(trainer.getUser().getUsername().equals(headerUsername) && trainer.getUser().getPassword().equals(password)) {
            return;
        }

        throw new ForbiddenException("You dont have permission to access this trainer");
    }

    @Override
    public void checkAuthProfile(String headerUsername, String password, Long id) {
        Trainer trainer = trainerDao.findById(id).orElseThrow(() -> new DomainException("Trainee not found: " + id));

        if(trainer.getUser().getUsername().equals(headerUsername) && trainer.getUser().getPassword().equals(password)) {
            return;
        }

        throw new ForbiddenException("You dont have permission to access this trainer");
    }

    @Override
    public ApiResponse<TrainerDTO> getProfile(String username) {
        LOGGER.info("Request to get {} profile with username: {}", ENTITY_NAME, username);

        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new DomainException("Trainer not found: " + username));

        TrainerDTO dto = trainerMapper.toDto(trainer);

        dto.setTraineeList(getTraineesTrainer(trainer.getId()));
        dto.setTrainingType(trainingTypeMapper.toDto(trainer.getSpecialization()));
        return new ApiResponse<>(true, null, dto);
    }

    @Override
    public Trainer getByUsername(String username) {
        return trainerDao.findByUsername(username)
                .orElseThrow(() -> new DomainException("Trainer not found: " + username));
    }


    @Transactional
    @Override
    public ApiResponse<TrainerDTO> updateProfile(TrainerDTO dto, Long id) {
        LOGGER.info("Request to update {} profile: {}", ENTITY_NAME, dto);

        Trainer trainer = trainerDao
                .findById(id)
                .orElseThrow(() -> new DomainException("Trainer not found"));

        trainer.getUser().setFirstName(dto.getFirstName());
        trainer.getUser().setLastName(dto.getLastName());
        trainer.getUser().setActive(dto.getActive());
        trainer.setSpecialization(trainingTypeService.getTrainingType(dto.getTrainingTypeId()));

        Trainer update = trainerDao.update(trainer);

        TrainerDTO responseDto = trainerMapper.toDto(update);
        responseDto.setTraineeList(getTraineesTrainer(update.getId()));

        return new ApiResponse<>(true, null, responseDto);
    }

    @Transactional
    public List<Training> getTrainerTrainings(String username, LocalDateTime fromDate, LocalDateTime toDate, String traineeName) {
        LOGGER.info("Request to get trainings for {} with username: {}", ENTITY_NAME, username);
        requireAuthentication(username);
        return trainerDao.findTrainerTrainings(domainUtils.getCurrentUser().getUsername(), username, fromDate, toDate, traineeName);
    }

    @Override
    public ApiResponse<Void> deleteProfile(String username) {
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new DomainException("Trainer not found"));

        trainer.getUser().setActive(false);
        trainerDao.update(trainer);

        return new ApiResponse<>(true, domainUtils.getMessage(ENTITY_NAME, StatusTypes.MESSAGE_TYPE_SUCCESS, OperationTypes.DELETION), null);
    }

    @Override
    public ApiResponse<Void> activateOrDeactivate(ActivateDeactiveRequest request) {
        Trainer trainer = trainerDao.findByUsername(request.getUsername())
                .orElseThrow(() -> new DomainException("Trainer not found"));

        trainer.getUser().setActive(!trainer.getUser().getActive());
        trainerDao.update(trainer);

        return new ApiResponse<>(true, null, null);

    }

    private void requireAuthentication(String username) {
        if (!trainerDao.findByUsername(username).isPresent()) {
            throw new SecurityException("Authentication required");
        }
    }

    private List<TraineeDTO> getTraineesTrainer(Long trainerId) {
        return trainerDao.findAllTrainerTrainees(trainerId)
                .stream()
                .map(traineeMapper::toDto)
                .collect(Collectors.toList());
    }

    private synchronized String generateUsername(String firstName, String lastName) {
        String baseUsername = (firstName + "_" + lastName).toLowerCase();
        String username = baseUsername;
        int suffix = 1;

        while (userDao.existsByUsername(username)) {
            username = baseUsername + suffix++;
        }

        LOGGER.info("Generated username: {}", username);
        return username;
    }

    private String generatePassword() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        LOGGER.info("Generated password: {}", sb);
        return sb.toString();
    }
}