package dev.floelly.activitytrackerapi.repository;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;

public abstract class MySQLContainerInitializer {
    static final MySQLContainer mysql = new MySQLContainer("mysql:8.0.36")
            .withDatabaseName("activitytracker")
            .withUsername("testUser")
            .withPassword("<PASSWORD>");

    static {
        mysql.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
}
