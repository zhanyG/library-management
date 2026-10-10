package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.reservation.Reservation;
import com.example.library.domain.model.reservation.ReservationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Reservation 的 Spring Data JPA 接口（派生查询）。
 */
public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByReaderIdAndTitleIdAndStatus(Long readerId, Long titleId, ReservationStatus status);
    long countByReaderIdAndStatus(Long readerId, ReservationStatus status);
    long countByTitleIdAndStatus(Long titleId, ReservationStatus status);
    Optional<Reservation> findFirstByTitleIdAndStatusOrderByQueuePositionAsc(Long titleId, ReservationStatus status);

    List<Reservation> findByStatus(ReservationStatus status);
}
