package com.tickethub.entity;

import com.tickethub.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Unit test thuần cho quy tắc nghiệp vụ: không cần Spring, không cần DB, chạy rất nhanh. */
class EventTest {

    private static final Instant NOW = Instant.parse("2026-11-24T00:00:00Z");
    private static final Instant START = NOW.plus(Duration.ofDays(7));
    private static final Instant END = START.plus(Duration.ofHours(3));

    private Event newEvent() {
        return new Event("Concert", "Mô tả", "Nhà hát lớn", START, END, NOW);
    }

    @Test
    void newEventStartsAsDraft() {
        assertThat(newEvent().getStatus()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void rejectsStartTimeInThePast() {
        assertThatThrownBy(() -> new Event("Concert", null, "HN", NOW.minusSeconds(1), END, NOW))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tương lai");
    }

    @Test
    void rejectsEndTimeBeforeStartTime() {
        assertThatThrownBy(() -> new Event("Concert", null, "HN", START, START.minusSeconds(1), NOW))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("kết thúc");
    }

    @Test
    void publishMovesDraftToPublished() {
        Event event = newEvent();
        event.publish(NOW);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void cannotPublishTwice() {
        Event event = newEvent();
        event.publish(NOW);
        assertThatThrownBy(() -> event.publish(NOW)).isInstanceOf(BusinessException.class);
    }

    @Test
    void cancelledEventCannotBeEdited() {
        Event event = newEvent();
        event.cancel();
        assertThatThrownBy(() -> event.updateDetails("Mới", null, "HCM", START, END, NOW))
                .isInstanceOf(BusinessException.class);
    }

    // BÀI TẬP: viết thêm test cho các trường hợp sau
    // - không thể cancel một sự kiện đã CANCELLED
    // - không thể publish khi sự kiện đã bắt đầu (now sau startTime)
    // - tên có khoảng trắng đầu/cuối thì được trim
}
