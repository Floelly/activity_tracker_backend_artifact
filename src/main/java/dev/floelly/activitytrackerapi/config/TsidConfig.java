package dev.floelly.activitytrackerapi.config;

import io.hypersistence.tsid.TSID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TsidConfig {

    @Bean
    public TSID.Factory tsidFactory() {
        return TSID.Factory.INSTANCE;
    }
}
