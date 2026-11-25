package com.tickethub.controller;

import com.tickethub.dto.response.PageResponse;
import com.tickethub.entity.EventStatus;
import com.tickethub.dto.request.EventRequest;
import com.tickethub.dto.response.EventResponse;
import com.tickethub.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;

/**
 * REST API cho sự kiện. Controller chỉ làm 3 việc: nhận request, gọi service, trả response.
 *
 * Không có API xóa: sự kiện đã bán vé thì không được xóa, chỉ được hủy (cancel).
 * Tuần 6 sẽ thêm phân quyền: chỉ ORGANIZER/ADMIN được tạo, sửa, công bố, hủy.
 */
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest req) {
        EventResponse created = eventService.create(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public PageResponse<EventResponse> search(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Instant from,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {
        return eventService.search(status, keyword, from, pageable);
    }

    @GetMapping("/{id}")
    public EventResponse getById(@PathVariable long id) {
        return eventService.getById(id);
    }

    @PutMapping("/{id}")
    public EventResponse update(@PathVariable long id, @Valid @RequestBody EventRequest req) {
        return eventService.update(id, req);
    }

    @PostMapping("/{id}/publish")
    public EventResponse publish(@PathVariable long id) {
        return eventService.publish(id);
    }

    @PostMapping("/{id}/cancel")
    public EventResponse cancel(@PathVariable long id) {
        return eventService.cancel(id);
    }
}
