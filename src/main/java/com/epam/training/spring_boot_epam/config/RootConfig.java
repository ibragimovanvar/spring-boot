package com.epam.training.spring_boot_epam.config;

import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.repository.TrainingTypeDao;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Configuration
@EnableTransactionManagement
public class RootConfig {

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void initializeDatabaseIfEmpty() {
        List<TrainingType> trainingTypes = trainingTypeDao.findAll();
        if (trainingTypes.isEmpty()) {
            try (Connection connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("import/initial.sql"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}