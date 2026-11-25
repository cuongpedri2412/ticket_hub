# TicketHub

Nền tảng bán vé sự kiện: chọn ghế, giữ ghế có thời hạn, thanh toán, vé QR, soát vé, flash sale.
Dự án học Java Spring Boot trong 8 tuần.

**Công nghệ:** Java 21 · Spring Boot 3.5 · MariaDB 11.4 · Flyway · Spring Data JPA · JUnit 5 · Testcontainers · Docker

---

## Chạy dự án

Yêu cầu: JDK 21, Docker Desktop (đang chạy).

```bash
# 1. Bật MariaDB
docker compose up -d

# 2. Chạy ứng dụng (Windows dùng mvnw.cmd)
./mvnw spring-boot:run

# 3. Chạy test (test tích hợp tự bỏ qua nếu Docker chưa bật)
./mvnw test
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health check: http://localhost:8080/actuator/health

Trong IntelliJ: mở thư mục dự án → IntelliJ tự nhận Maven → chạy `TickethubApplication`.

---

## Kiến trúc

**Kiến trúc phân tầng** (layered architecture): mỗi tầng một package, request đi theo một chiều
`Controller → Service → Repository → Database`.

```
com.tickethub
├── controller/      Nhận HTTP request, gọi service, trả response   (EventController)
├── service/         Điều phối nghiệp vụ, quản lý transaction        (EventService)
├── repository/      Truy cập DB bằng Spring Data JPA                (EventRepository)
├── entity/          Entity JPA + quy tắc nghiệp vụ                  (BaseEntity, Event, EventStatus)
├── dto/
│   ├── request/     Dữ liệu client gửi lên                          (EventRequest)
│   └── response/    Dữ liệu trả về client                           (EventResponse, PageResponse, ErrorResponse)
├── exception/       Mã lỗi, exception, xử lý lỗi tập trung          (ErrorCode, BusinessException, GlobalExceptionHandler)
└── config/          Cấu hình Spring                                 (ClockConfig, JpaAuditingConfig)
```

Tính năng mới thêm class vào đúng tầng, ví dụ đặt vé: `BookingController`, `BookingService`,
`BookingRepository`, `Booking`, `BookingRequest`, `BookingResponse`.

**Quy tắc phụ thuộc:** tầng trên gọi tầng dưới, không gọi ngược. Controller không gọi thẳng repository.

### Danh sách tính năng cần làm

| Tuần | Tính năng | Bài toán chính |
|---|---|---|
| 4 | **Venue, Section, Seat, TicketType, EventSeat**; thay `location` (String) bằng quan hệ tới Venue | Vì sao trạng thái ghế nằm ở EventSeat chứ không phải Seat? |
| 5 | **Booking:** giữ ghế 10 phút, job `@Scheduled` nhả ghế quá hạn | 100 thread cùng giữ 1 ghế → đúng 1 thành công. So sánh: không khóa, `@Version`, `SELECT ... FOR UPDATE`, `UPDATE ... WHERE status='AVAILABLE'` |
| 5 | **Payment:** cổng thanh toán giả lập, header `Idempotency-Key`, webhook, hoàn tiền | Webhook đến hai lần hoặc đến khi ghế đã bị nhả |
| 6 | **User & Auth:** đăng ký, đăng nhập JWT, BCrypt, vai trò CUSTOMER / ORGANIZER / ADMIN | Thu hồi JWT đã phát ra thế nào? |
| 6 | **Ticket:** mã vé ngẫu nhiên, ảnh QR (ZXing), soát vé chỉ thành công một lần | Hai cổng quét cùng lúc |
| 6 | **Notification:** email bất đồng bộ (`@TransactionalEventListener`), nhắc lịch | Vì sao gửi email sau khi transaction commit? |
| 7 | **Report & Flash sale:** doanh thu, tỉ lệ lấp đầy; Redis cache, `DECR` số vé, rate limit; load test k6 | 1.000 người mua 100 vé → không bán vượt |

**Quy ước của dự án**
1. Entity không có setter công khai; thay đổi trạng thái qua method nghiệp vụ (`event.publish()`).
2. Controller không chứa logic, không trả entity ra API, chỉ trả DTO.
3. Lỗi nghiệp vụ → `throw new BusinessException(ErrorCode.X)`; `GlobalExceptionHandler` lo phần còn lại.
4. Không gọi `Instant.now()` trực tiếp; inject `Clock` để test được.
5. Schema chỉ thay đổi qua Flyway migration mới (`V2__...sql`), không sửa file cũ.
6. Mọi thời điểm lưu theo UTC.

---

## API hiện có (Event)

| Method | Đường dẫn | Mô tả |
|---|---|---|
| POST | `/api/v1/events` | Tạo sự kiện (trạng thái DRAFT) |
| GET | `/api/v1/events?status=&keyword=&from=&page=&size=&sort=` | Tìm kiếm, phân trang |
| GET | `/api/v1/events/{id}` | Xem chi tiết |
| PUT | `/api/v1/events/{id}` | Sửa (không sửa được khi đã hủy) |
| POST | `/api/v1/events/{id}/publish` | Công bố (DRAFT → PUBLISHED) |
| POST | `/api/v1/events/{id}/cancel` | Hủy |

Ví dụ tạo sự kiện:

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{"name":"Đêm nhạc Trịnh","location":"Nhà hát lớn Hà Nội",
       "startTime":"2026-12-31T19:00:00+07:00","endTime":"2026-12-31T22:00:00+07:00"}'
```

Lỗi luôn trả về cùng một định dạng:

```json
{
  "code": "VALIDATION_FAILED",
  "message": "Dữ liệu không hợp lệ",
  "path": "/api/v1/events",
  "timestamp": "2026-11-24T10:00:00Z",
  "errors": [{ "field": "name", "message": "Tên sự kiện không được để trống" }]
}
```

---

## Test

| Loại | File | Cần gì |
|---|---|---|
| Unit test nghiệp vụ | `EventTest` | Không cần gì, chạy trong vài ms |
| Test tầng web | `EventControllerTest` | Không cần DB (service được mock) |
| Test tích hợp | `EventApiIntegrationTest` | Docker — chạy MariaDB thật qua Testcontainers |

---

## Tiến độ

- [x] Tuần 0 — Khung dự án, chức năng Event mẫu
- [ ] Tuần 1–2 — Core Java (OOP, Collections, Stream, đa luồng) qua bài tập trên domain
- [ ] Tuần 3 — SQL & MariaDB: index, transaction, EXPLAIN
- [ ] Tuần 4 — Venue, Seat, TicketType
- [ ] Tuần 5 — Booking (giữ ghế, chống đặt trùng) & Payment (idempotency)
- [ ] Tuần 6 — Auth JWT, Ticket QR, Notification
- [ ] Tuần 7 — Report, Flash sale, Redis, CI, load test
- [ ] Tuần 8 — Hoàn thiện, demo, ôn phỏng vấn
