package com.tickethub.entity;

/**
 * Vòng đời sự kiện:
 *
 *   DRAFT ──publish──▶ PUBLISHED
 *     │                   │
 *     └──────cancel───────┴──▶ CANCELLED
 *
 * "Đang diễn ra" hay "đã kết thúc" không lưu trong DB mà tính từ startTime/endTime.
 * Câu hỏi tự học: vì sao không lưu trạng thái ONGOING/ENDED vào cột status?
 */
public enum EventStatus {
    DRAFT,
    PUBLISHED,
    CANCELLED
}
