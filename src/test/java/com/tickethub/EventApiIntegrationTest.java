package com.tickethub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test tích hợp: chạy toàn bộ ứng dụng với MariaDB THẬT trong Docker (Testcontainers).
 * Kiểm tra được cả Flyway migration lẫn việc entity khớp schema (ddl-auto=validate).
 *
 * Docker chưa chạy thì test này tự bỏ qua (disabledWithoutDocker = true).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class EventApiIntegrationTest {

    @Container
    @ServiceConnection
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createPublishAndSearchEvent() throws Exception {
        String body = """
                {"name":"Đêm nhạc Trịnh","description":"Tiếng Việt có dấu phải lưu đúng",
                 "location":"Nhà hát lớn Hà Nội",
                 "startTime":"2099-01-10T19:00:00+07:00","endTime":"2099-01-10T22:00:00+07:00"}
                """;

        String location = mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Đêm nhạc Trịnh"))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(post(location + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/v1/events").param("status", "PUBLISHED").param("keyword", "trịnh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void publishTwiceReturns409() throws Exception {
        String location = mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Hội thảo","location":"HCM",
                                 "startTime":"2099-02-01T08:00:00Z","endTime":"2099-02-01T12:00:00Z"}
                                """))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(post(location + "/publish")).andExpect(status().isOk());
        mockMvc.perform(post(location + "/publish"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EVENT_INVALID_STATUS_TRANSITION"));
    }
}
