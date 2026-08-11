package dev.floelly.activitytrackerapi.config;

import dev.floelly.activitytrackerapi.controller.ActivityController;
import dev.floelly.activitytrackerapi.service.ActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityController.class)
@Import(CorsConfig.class)
class CorsConfigTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ActivityService activityService;

    @Test
    void shouldReturnCorsHeadersForAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/activities")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    void shouldNotReturnCorsHeadersForDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/activities")
                        .header("Origin", "http://bad-ppl.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}