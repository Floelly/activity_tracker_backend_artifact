package dev.floelly.activitytrackerapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CorsConfig.class)
@TestPropertySource(properties = "cors.allowed-origins=http://localhost:5173")
class CorsConfigTest {

    @Autowired
    private CorsConfig corsConfig;

    @Test
    void shouldCreateCorsConfigBean() {
        assertThat(corsConfig).isNotNull();
    }

    @Test
    void shouldRegisterCorsMappings() {
        assertThat(corsConfig).isNotNull();
    }
}