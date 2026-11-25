package com.tickethub.entity;

import com.tickethub.exception.BusinessException;
import com.tickethub.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity sự kiện.
 *
 * Quy ước của dự án: entity KHÔNG có setter công khai. Mọi thay đổi trạng thái
 * đi qua method nghiệp vụ (publish, cancel, updateDetails) để quy tắc luôn được kiểm tra.
 * Đây gọi là "rich domain model", đối lập với "anemic model" chỉ có getter/setter.
 */
@Entity
@Table(name = "event")
public class Event extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    /** Tạm thời là chuỗi; tuần 4 sẽ thay bằng quan hệ tới Venue. */
    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    /** Optimistic locking: chặn hai người cùng sửa đè lên nhau. */
    @Version
    private long version;

    /** JPA cần constructor rỗng; để protected để code khác không gọi nhầm. */
    protected Event() {
    }

    public Event(String name, String description, String location,
                 Instant startTime, Instant endTime, Instant now) {
        this.status = EventStatus.DRAFT;
        applyDetails(name, description, location, startTime, endTime, now);
    }

    // ===== Hành vi nghiệp vụ =====

    public void updateDetails(String name, String description, String location,
                              Instant startTime, Instant endTime, Instant now) {
        if (status == EventStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.EVENT_INVALID_STATUS_TRANSITION,
                    "Sự kiện đã hủy, không thể chỉnh sửa");
        }
        applyDetails(name, description, location, startTime, endTime, now);
    }

    public void publish(Instant now) {
        if (status != EventStatus.DRAFT) {
            throw new BusinessException(ErrorCode.EVENT_INVALID_STATUS_TRANSITION,
                    "Chỉ sự kiện ở trạng thái DRAFT mới được công bố (hiện tại: " + status + ")");
        }
        if (!startTime.isAfter(now)) {
            throw new BusinessException(ErrorCode.EVENT_INVALID_TIME,
                    "Không thể công bố sự kiện đã bắt đầu");
        }
        this.status = EventStatus.PUBLISHED;
    }

    public void cancel() {
        if (status == EventStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.EVENT_INVALID_STATUS_TRANSITION,
                    "Sự kiện đã bị hủy trước đó");
        }
        this.status = EventStatus.CANCELLED;
    }

    private void applyDetails(String name, String description, String location,
                              Instant startTime, Instant endTime, Instant now) {
        Objects.requireNonNull(startTime, "startTime");
        Objects.requireNonNull(endTime, "endTime");
        if (!startTime.isAfter(now)) {
            throw new BusinessException(ErrorCode.EVENT_INVALID_TIME,
                    "Thời gian bắt đầu phải ở tương lai");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(ErrorCode.EVENT_INVALID_TIME,
                    "Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        this.name = name.trim();
        this.description = description;
        this.location = location.trim();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // ===== Getter =====

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public EventStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }
}
