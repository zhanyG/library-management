package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.reservation.Reservation;
import com.example.library.domain.model.reservation.ReservationStatus;
import com.example.library.domain.repository.ReservationRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * ReservationRepository 的持久化适配器：把领域端口委托给 Spring Data JPA。
 */
@Component
public class ReservationRepositoryAdapter implements ReservationRepository {

    private final JpaReservationRepository jpa;

    public ReservationRepositoryAdapter(JpaReservationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return jpa.save(reservation);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existsWaitingByReaderAndTitle(Long readerId, Long titleId) {
        return jpa.existsByReaderIdAndTitleIdAndStatus(readerId, titleId, ReservationStatus.WAITING);
    }

    @Override
    public long countWaitingByReader(Long readerId) {
        return jpa.countByReaderIdAndStatus(readerId, ReservationStatus.WAITING);
    }

    @Override
    public long countWaitingByTitle(Long titleId) {
        return jpa.countByTitleIdAndStatus(titleId, ReservationStatus.WAITING);
    }

    @Override
    public Optional<Reservation> findHeadWaitingByTitleId(Long titleId) {
        return jpa.findFirstByTitleIdAndStatusOrderByQueuePositionAsc(titleId, ReservationStatus.WAITING);
    }
}
