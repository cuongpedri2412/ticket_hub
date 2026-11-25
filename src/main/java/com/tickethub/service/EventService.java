package com.tickethub.service;

import com.tickethub.exception.ResourceNotFoundException;
import com.tickethub.dto.response.PageResponse;
import com.tickethub.entity.Event;
import com.tickethub.entity.EventStatus;
import com.tickethub.dto.request.EventRequest;
import com.tickethub.dto.response.EventResponse;
import com.tickethub.repository.EventRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Tầng service: điều phối use case (lấy dữ liệu, gọi hành vi của entity, lưu lại).
 * Quy tắc nghiệp vụ nằm trong Event, service không tự kiểm tra lại.
 *
 * Câu hỏi tự học:
 * - Vì sao updateDetails() không cần gọi repository.save() mà dữ liệu vẫn được lưu?
 *   (gợi ý: "dirty checking" của Hibernate)
 * - readOnly = true giúp gì về hiệu năng?
 */
@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final Clock clock;

    // Constructor injection: không cần @Autowired, dễ test, field có thể là final.
    public EventService(EventRepository eventRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.clock = clock;
    }

    @Transactional
    public EventResponse create(EventRequest req) {
        Event event = new Event(req.name(), req.description(), req.location(),
                req.startTime(), req.endTime(), now());
        return EventResponse.from(eventRepository.save(event));
    }

    public EventResponse getById(long id) {
        return EventResponse.from(findOrThrow(id));
    }

    public PageResponse<EventResponse> search(EventStatus status, String keyword, Instant from, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return PageResponse.from(
                eventRepository.search(status, normalizedKeyword, from, pageable),
                EventResponse::from);
    }

    @Transactional
    public EventResponse update(long id, EventRequest req) {
        Event event = findOrThrow(id);
        event.updateDetails(req.name(), req.description(), req.location(),
                req.startTime(), req.endTime(), now());
        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse publish(long id) {
        Event event = findOrThrow(id);
        event.publish(now());
        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse cancel(long id) {
        Event event = findOrThrow(id);
        event.cancel();
        return EventResponse.from(event);
    }

    private Event findOrThrow(long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("sự kiện", id));
    }

    private Instant now() {
        return Instant.now(clock);
    }
}
