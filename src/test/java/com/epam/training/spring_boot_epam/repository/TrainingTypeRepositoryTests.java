package com.epam.training.spring_boot_epam.repository;

import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.repository.impl.TrainingTypeDaoImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TrainingTypeDaoImpl.class)
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainingTypeRepositoryTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TrainingTypeDaoImpl trainingTypeDao;

    @Autowired
    private DataSource dataSource;

    @BeforeAll
    void init() throws Exception {
        List<TrainingType> trainingTypes = trainingTypeDao.findAll();
        if (trainingTypes.isEmpty()) {
            try (Connection connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("import/initial.sql"));
            }
        }
    }

    @Test
    void findById_WhenTrainingTypeExists_ShouldReturnTrainingType() {
        TrainingType trainingType = createTrainingType("Strength Training");
        entityManager.persist(trainingType);
        entityManager.flush();

        Optional<TrainingType> found = trainingTypeDao.findById(trainingType.getId());

        assertThat(found).isPresent()
                .contains(trainingType);
        assertThat(found.get().getTrainingTypeName()).isEqualTo("Strength Training");
    }

    @Test
    void findById_WhenTrainingTypeDoesNotExist_ShouldReturnEmpty() {
        Optional<TrainingType> found = trainingTypeDao.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_WhenTrainingTypesExist_ShouldReturnAllTrainingTypes() {
        TrainingType type1 = createTrainingType("Cardio");
        TrainingType type2 = createTrainingType("Yoga");
        entityManager.persist(type1);
        entityManager.persist(type2);
        entityManager.flush();

        List<TrainingType> trainingTypes = trainingTypeDao.findAll();
        assertThat(trainingTypes).isNotNull().isNotEmpty();
    }

    @Test
    void findAll_WhenNoTrainingTypesExist_ShouldNotReturnEmptyList() {
        List<TrainingType> trainingTypes = trainingTypeDao.findAll();
        assertThat(trainingTypes).isNotEmpty();
    }

    // Helper method
    private TrainingType createTrainingType(String name) {
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName(name);
        return trainingType;
    }
}
