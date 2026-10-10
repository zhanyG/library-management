package com.example.library.domain.repository;
import com.example.library.domain.model.reservation.Reservation;
import com.example.library.domain.model.reservation.ReservationStatus;
import java.util.List;
import java.util.Optional;

/**
 * 预约记录持久化端口。
 *
 * <p>领域层端口：只声明业务语义方法，不依赖任何框架。</p>
 */
public interface ReservationRepository {

    Reservation save(Reservation reservation);
    Optional<Reservation> findById(Long id);
    boolean existsWaitingByReaderAndTitle(Long readerId, Long titleId);
    long countWaitingByReader(Long readerId);
    long countWaitingByTitle(Long titleId);
    Optional<Reservation> findHeadWaitingByTitleId(Long titleId);

    List<Reservation> findByStatus(ReservationStatus status);
}
