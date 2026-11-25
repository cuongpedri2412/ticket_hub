-- Quy tắc Flyway: KHÔNG sửa file migration đã chạy. Muốn đổi schema thì tạo file mới V2__..., V3__...
--
-- Vì sao DATETIME(6) mà không phải TIMESTAMP?
--   TIMESTAMP của MariaDB chỉ lưu được tới năm 2038 và có hành vi tự cập nhật khó lường.
--   DATETIME(6) lưu tới micro giây; ứng dụng tự đảm bảo luôn ghi giá trị UTC.

CREATE TABLE event (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    name         VARCHAR(255)  NOT NULL,
    description  VARCHAR(2000) NULL,
    location     VARCHAR(255)  NOT NULL,
    start_time   DATETIME(6)   NOT NULL,
    end_time     DATETIME(6)   NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    version      BIGINT        NOT NULL DEFAULT 0,
    created_at   DATETIME(6)   NOT NULL,
    updated_at   DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    -- Phục vụ truy vấn phổ biến nhất: "sự kiện PUBLISHED sắp diễn ra, sắp xếp theo giờ bắt đầu".
    -- Câu hỏi tự học: vì sao status đứng trước start_time trong index này?
    INDEX idx_event_status_start_time (status, start_time),
    CONSTRAINT chk_event_time CHECK (end_time > start_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
