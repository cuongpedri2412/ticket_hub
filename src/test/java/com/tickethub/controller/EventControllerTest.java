package com.tickethub.controller;

import com.tickethub.exception.ResourceNotFoundException;
import com.tickethub.entity.EventStatus;
import com.tickethub.dto.response.EventResponse;
import com.tickethub.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WebMvcTest chỉ khởi động tầng web (controller, validation, exception handler).
 * Service được thay bằng mock, nên test không cần DB.
 */
@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    private static final Instant START = Instant.parse("2027-01-10T12:00:00Z");
    private static final Instant END = Instant.parse("2027-01-10T15:00:00Z");

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        given(eventService.create(any())).willReturn(new EventResponse(
                1L, "Concert", null, "Nhà hát lớn", START, END, EventStatus.DRAFT, START, START));

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Concert","location":"Nhà hát lớn",
                                 "startTime":"2027-01-10T19:00:00+07:00","endTime":"2027-01-10T22:00:00+07:00"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/events/1"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void createWithBlankNameReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","location":"HN",
                                 "startTime":"2027-01-10T19:00:00+07:00","endTime":"2027-01-10T22:00:00+07:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void getUnknownIdReturns404() throws Exception {
        given(eventService.getById(99L)).willThrow(new ResourceNotFoundException("sự kiện", 99L));

        mockMvc.perform(get("/api/v1/events/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
