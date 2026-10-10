package com.example.library.domain.service;

import com.example.library.domain.model.catalog.BookTitle;
import com.example.library.domain.model.reservation.Reservation;
import com.example.library.domain.model.reservation.ReservationStatus;
import com.example.library.domain.repository.BookTitleRepository;
import com.example.library.domain.repository.ReaderRepository;
import com.example.library.domain.repository.ReservationRepository;
import com.example.library.exception.DuplicateReservationException;
import com.example.library.exception.ReaderNotFoundException;
import com.example.library.exception.ReservationLimitExceededException;
import com.example.library.exception.ReservationNotFoundException;
import com.example.library.exception.TitleNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 预约领域服务：排队、取消与到书保留失效（UC-004、UC-005、BR-010、BR-012）。
 */
@Service
public class ReservationService {

    /** 单人同时有效预约数量上限（BR-012）。 */
    public static final int MAX_ACTIVE_RESERVATIONS = 3;

    /** 到书保留天数（BR-010）。 */
    public static final int HOLD_DAYS = 7;

    private final ReaderRepository readerRepository;
    private final BookTitleRepository bookTitleRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(ReaderRepository readerRepository,
                              BookTitleRepository bookTitleRepository,
                              ReservationRepository reservationRepository) {
        this.readerRepository = readerRepository;
        this.bookTitleRepository = bookTitleRepository;
        this.reservationRepository = reservationRepository;
    }

    /** UC-004 预约图书：校验读者与题名，分配排队序号（按创建时间先到先服务）。 */
    public Reservation reserve(Long readerId, Long titleId, LocalDateTime now) {
        readerRepository.findById(readerId).orElseThrow(ReaderNotFoundException::new);
        BookTitle title = bookTitleRepository.findById(titleId).orElseThrow(TitleNotFoundException::new);

        if (reservationRepository.existsWaitingByReaderAndTitle(readerId, titleId)) {
            throw new DuplicateReservationException("读者已预约题名 " + title.getName());
        }
        if (reservationRepository.countWaitingByReader(readerId) >= MAX_ACTIVE_RESERVATIONS) {
            throw new ReservationLimitExceededException("有效预约数量已达上限 " + MAX_ACTIVE_RESERVATIONS + " 条");
        }

        int queuePosition = (int) reservationRepository.countWaitingByTitle(titleId) + 1;
        Reservation reservation = new Reservation(readerId, titleId, now, queuePosition);
        reservation.holdUntil(now.plusDays(HOLD_DAYS));
        return reservationRepository.save(reservation);
    }

    /** UC-005 取消预约（权限校验在应用服务层完成）。 */
    public Reservation cancel(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);
        reservation.cancel();
        return reservationRepository.save(reservation);
    }

    /** 队首预约（BR-011）。 */
    public Optional<Reservation> nextWaiting(Long titleId) {
        return reservationRepository.findHeadWaitingByTitleId(titleId);
    }

    /** 到书保留超时失效，返回本次失效的预约数量。 */
    public int expireOverdue(LocalDateTime now) {
        int expired = 0;
        for (Reservation reservation : reservationRepository.findByStatus(ReservationStatus.WAITING)) {
            if (reservation.getExpireAt() != null && now.isAfter(reservation.getExpireAt())) {
                reservation.expire(now);
                reservationRepository.save(reservation);
                expired++;
            }
        }
        return expired;
    }
}
