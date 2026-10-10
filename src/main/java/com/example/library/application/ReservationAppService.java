package com.example.library.application;

import com.example.library.domain.model.catalog.BookTitle;
import com.example.library.domain.model.reservation.Reservation;
import com.example.library.domain.repository.BookTitleRepository;
import com.example.library.domain.repository.ReservationRepository;
import com.example.library.domain.service.ReservationService;
import com.example.library.exception.PermissionDeniedException;
import com.example.library.exception.ReservationNotFoundException;
import com.example.library.exception.TitleNotFoundException;
import com.example.library.presentation.dto.ReservationResponse;
import com.example.library.presentation.dto.ReserveBookRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 预约应用服务：权限校验与事务边界（UC-004、UC-005）。
 */
@Service
public class ReservationAppService {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final BookTitleRepository bookTitleRepository;
    private final PermissionChecker permissionChecker;

    public ReservationAppService(ReservationService reservationService,
                                 ReservationRepository reservationRepository,
                                 BookTitleRepository bookTitleRepository,
                                 PermissionChecker permissionChecker) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.bookTitleRepository = bookTitleRepository;
        this.permissionChecker = permissionChecker;
    }

    /** FR-014 预约图书：读者只能为本人预约（BR-015）。 */
    @Transactional
    public ReservationResponse reserve(ReserveBookRequest request, Operator operator) {
        permissionChecker.assertSelfOrStaff(operator, request.readerId());
        Reservation reservation = reservationService.reserve(request.readerId(), request.titleId(), LocalDateTime.now());
        BookTitle title = bookTitleRepository.findById(request.titleId()).orElseThrow(TitleNotFoundException::new);
        return toResponse(reservation, title.getName());
    }

    /** UC-005 取消预约：仅预约所属读者本人或系统管理员。 */
    @Transactional
    public void cancel(Long reservationId, Operator operator) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);
        boolean isOwner = operator != null && operator.id() != null
                && operator.id().equals(reservation.getReaderId());
        boolean isAdmin = operator != null && operator.role() == OperatorRole.SYSTEM_ADMIN;
        if (!isOwner && !isAdmin) {
            throw new PermissionDeniedException("只能取消本人的预约");
        }
        reservationService.cancel(reservationId);
    }

    private ReservationResponse toResponse(Reservation reservation, String titleName) {
        return new ReservationResponse(reservation.getId(), titleName, reservation.getQueuePosition(),
                reservation.getCreatedAt(), reservation.getStatus().name());
    }
}
