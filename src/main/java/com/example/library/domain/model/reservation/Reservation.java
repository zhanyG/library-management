package com.example.library.domain.model.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 预约记录：按创建时间排队，到书保留 7 天（BR-010、BR-012）。
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reader_id", nullable = false)
    private Long readerId;

    @Column(name = "title_id", nullable = false)
    private Long titleId;

    @Column(name = "queue_position", nullable = false)
    private int queuePosition;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ReservationStatus status = ReservationStatus.WAITING;

    protected Reservation() {
    }

    public Reservation(Long readerId, Long titleId, LocalDateTime createdAt, int queuePosition) {
        this.readerId = readerId;
        this.titleId = titleId;
        this.createdAt = createdAt;
        this.queuePosition = queuePosition;
        this.status = ReservationStatus.WAITING;
    }

    /** 是否为队首预约（BR-011）。 */
    public boolean isHeadOfQueue() {
        return queuePosition == 1 && status == ReservationStatus.WAITING;
    }

    public boolean isActive() {
        return status == ReservationStatus.WAITING;
    }

    /** 到书保留（默认 7 天）。 */
    public void holdUntil(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public void fulfill() {
        this.status = ReservationStatus.FULFILLED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    /** 超时失效（保留期已过）。 */
    public void expire(LocalDateTime now) {
        if (status == ReservationStatus.WAITING && expireAt != null && now.isAfter(expireAt)) {
            this.status = ReservationStatus.EXPIRED;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getReaderId() {
        return readerId;
    }

    public Long getTitleId() {
        return titleId;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
