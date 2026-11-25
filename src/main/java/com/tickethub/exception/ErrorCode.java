package com.tickethub.exception;

import org.springframework.http.HttpStatus;

/**
 * Danh sách mã lỗi nghiệp vụ của toàn hệ thống.
 * Mỗi tính năng thêm mã lỗi của mình vào đây (tách enum riêng khi dự án lớn hơn).
 */
public enum ErrorCode {

    // Chung
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu"),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "Dữ liệu đã bị người khác thay đổi, vui lòng thử lại"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống"),

    // Event
    EVENT_INVALID_TIME(HttpStatus.BAD_REQUEST, "Thời gian sự kiện không hợp lệ"),
    EVENT_INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "Không thể chuyển trạng thái sự kiện");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
