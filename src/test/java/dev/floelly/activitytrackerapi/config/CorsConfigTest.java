package dev.floelly.activitytrackerapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    private final CorsConfig corsConfig = new CorsConfig();

    @Test
    void addCorsMappings_shouldAllowConfiguredOriginMethodsAndHeadersForApiPaths() {
        CorsRegistry registry = new CorsRegistry();
        corsConfig.addCorsMappings(registry);
        Map<String, CorsConfiguration> configMap = registry.getCorsConfigurations();
        CorsConfiguration config = configMap.get("/api/**");
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).contains("http://localhost:5173");
        assertThat(config.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD");
        assertThat(config.getAllowedHeaders()).contains("*");
    }
}