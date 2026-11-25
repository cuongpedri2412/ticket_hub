package com.tickethub.dto.response;

import com.tickethub.entity.Event;
import com.tickethub.entity.EventStatus;

import java.time.Instant;

/**
 * Dữ liệu trả về cho client. Không trả thẳng entity ra API vì:
 * 1) lộ cấu trúc DB, 2) dễ dính lỗi lazy loading, 3) đổi entity sẽ làm vỡ API.
 */
public record EventResponse(
        Long id,
        String name,
        String description,
        String location,
        Instant startTime,
        Instant endTime,
        EventStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static EventResponse from(Event e) {
        return new EventResponse(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getLocation(),
                e.getStartTime(),
                e.getEndTime(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
