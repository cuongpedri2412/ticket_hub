package com.tickethub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Dữ liệu client gửi lên khi tạo/sửa sự kiện.
 * Thời gian dùng định dạng ISO-8601, ví dụ "2026-12-31T19:00:00+07:00".
 *
 * Validation ở đây chỉ kiểm tra "hình thức" (rỗng, độ dài).
 * Quy tắc nghiệp vụ (bắt đầu ở tương lai, kết thúc sau bắt đầu) nằm trong Event.
 */
public record EventRequest(
        @NotBlank(message = "Tên sự kiện không được để trống")
        @Size(max = 255, message = "Tên tối đa 255 ký tự")
        String name,

        @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
        String description,

        @NotBlank(message = "Địa điểm không được để trống")
        @Size(max = 255, message = "Địa điểm tối đa 255 ký tự")
        String location,

        @NotNull(message = "Thiếu thời gian bắt đầu")
        Instant startTime,

        @NotNull(message = "Thiếu thời gian kết thúc")
        Instant endTime
) {
}
