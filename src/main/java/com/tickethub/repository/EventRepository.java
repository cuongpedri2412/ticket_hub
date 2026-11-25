package com.tickethub.repository;

import com.tickethub.entity.Event;
import com.tickethub.entity.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Tìm kiếm có bộ lọc tùy chọn: tham số nào null thì bỏ qua điều kiện đó.
     *
     * Bài tập tuần 3: bật show-sql, xem câu SQL sinh ra, chạy EXPLAIN trên MariaDB
     * và trả lời: index idx_event_status_start_time có được dùng không? Vì sao
     * LIKE '%abc%' không dùng được index?
     */
    @Query("""
            select e from Event e
            where (:status is null or e.status = :status)
              and (:keyword is null or lower(e.name) like lower(concat('%', :keyword, '%')))
              and (:from is null or e.startTime >= :from)
            """)
    Page<Event> search(@Param("status") EventStatus status,
                       @Param("keyword") String keyword,
                       @Param("from") Instant from,
                       Pageable pageable);
}
