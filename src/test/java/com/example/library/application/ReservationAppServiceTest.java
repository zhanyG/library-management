package com.example.library.application;

import com.example.library.domain.model.catalog.BookTitle;
import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.domain.model.reservation.Reservation;
import com.example.library.domain.model.reservation.ReservationStatus;
import com.example.library.domain.repository.ReservationRepository;
import com.example.library.domain.service.ReservationService;
import com.example.library.exception.DuplicateReservationException;
import com.example.library.exception.PermissionDeniedException;
import com.example.library.exception.ReaderNotFoundException;
import com.example.library.exception.ReservationLimitExceededException;
import com.example.library.exception.TitleNotFoundException;
import com.example.library.presentation.dto.ReservationResponse;
import com.example.library.presentation.dto.ReserveBookRequest;
import com.example.library.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TASK-011 预约与取消预约集成测试（TC-032 ~ TC-037、TC-049）。
 */
@SpringBootTest
@Transactional
class ReservationAppServiceTest {

    @Autowired
    private ReservationAppService reservationAppService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestDataFactory dataFactory;

    private Reader reader;

    private BookTitle title;

    private void givenReaderAndTitle() {
        reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
    }

    @Test
    @DisplayName("TC-032 预约成功：状态 WAITING，排队序号为 1")
    void reserveSuccess() {
        givenReaderAndTitle();

        ReservationResponse response = reservationAppService.reserve(
                new ReserveBookRequest(reader.getId(), title.getId()), Operator.reader(reader.getId()));

        assertThat(response.reservationId()).isNotNull();
        assertThat(response.queuePosition()).isEqualTo(1);
        assertThat(response.status()).isEqualTo("WAITING");
        assertThat(response.titleName()).isEqualTo(title.getName());
    }

    @Test
    @DisplayName("TC-033 重复预约失败（FR-014 验收标准 1）")
    void duplicateReservation() {
        givenReaderAndTitle();
        reservationAppService.reserve(new ReserveBookRequest(reader.getId(), title.getId()),
                Operator.reader(reader.getId()));

        assertThatThrownBy(() -> reservationAppService.reserve(
                new ReserveBookRequest(reader.getId(), title.getId()), Operator.reader(reader.getId())))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("TC-034 有效预约达到上限失败（BR-012）")
    void reservationLimitExceeded() {
        givenReaderAndTitle();
        for (int i = 0; i < ReservationService.MAX_ACTIVE_RESERVATIONS; i++) {
            BookTitle another = dataFactory.createTitle(ItemType.CHINESE_BOOK);
            reservationAppService.reserve(new ReserveBookRequest(reader.getId(), another.getId()),
                    Operator.reader(reader.getId()));
        }

        assertThatThrownBy(() -> reservationAppService.reserve(
                new ReserveBookRequest(reader.getId(), title.getId()), Operator.reader(reader.getId())))
                .isInstanceOf(ReservationLimitExceededException.class);
    }

    @Test
    @DisplayName("TC-035 排队序号按创建顺序递增（BR-010）")
    void queuePositionIncreases() {
        title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
        Reader first = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        Reader second = dataFactory.createReader("李四", "20240002", ReaderType.UNDERGRADUATE);
        Reader third = dataFactory.createReader("王五", "20240003", ReaderType.UNDERGRADUATE);

        int p1 = reservationAppService.reserve(new ReserveBookRequest(first.getId(), title.getId()),
                Operator.reader(first.getId())).queuePosition();
        int p2 = reservationAppService.reserve(new ReserveBookRequest(second.getId(), title.getId()),
                Operator.reader(second.getId())).queuePosition();
        int p3 = reservationAppService.reserve(new ReserveBookRequest(third.getId(), title.getId()),
                Operator.reader(third.getId())).queuePosition();

        assertThat(p1).isEqualTo(1);
        assertThat(p2).isEqualTo(2);
        assertThat(p3).isEqualTo(3);
        assertThat(reservationRepository.findHeadWaitingByTitleId(title.getId()).orElseThrow().getReaderId())
                .isEqualTo(first.getId());
    }

    @Test
    @DisplayName("TC-036 读者不存在或题名不存在时预约失败")
    void reserveWithMissingReaderOrTitle() {
        givenReaderAndTitle();

        assertThatThrownBy(() -> reservationAppService.reserve(
                new ReserveBookRequest(9999L, title.getId()), Operator.reader(9999L)))
                .isInstanceOf(ReaderNotFoundException.class);

        assertThatThrownBy(() -> reservationAppService.reserve(
                new ReserveBookRequest(reader.getId(), 9999L), Operator.reader(reader.getId())))
                .isInstanceOf(TitleNotFoundException.class);
    }

    @Test
    @DisplayName("TC-049 取消本人预约成功，其他预约排队序号不变")
    void cancelOwnReservation() {
        title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
        Reader first = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        Reader second = dataFactory.createReader("李四", "20240002", ReaderType.UNDERGRADUATE);
        Reservation firstReservation = reservationRepository.save(
                new Reservation(first.getId(), title.getId(), LocalDateTime.now(), 1));
        Reservation secondReservation = reservationRepository.save(
                new Reservation(second.getId(), title.getId(), LocalDateTime.now(), 2));

        reservationAppService.cancel(firstReservation.getId(), Operator.reader(first.getId()));

        assertThat(reservationRepository.findById(firstReservation.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CANCELLED);
        assertThat(reservationRepository.findById(secondReservation.getId()).orElseThrow().getQueuePosition())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("TC-049 取消他人预约失败（BR-015）")
    void cancelOthersReservationDenied() {
        givenReaderAndTitle();
        Reader other = dataFactory.createReader("李四", "20240002", ReaderType.UNDERGRADUATE);
        Reservation reservation = reservationRepository.save(
                new Reservation(reader.getId(), title.getId(), LocalDateTime.now(), 1));

        assertThatThrownBy(() -> reservationAppService.cancel(reservation.getId(), Operator.reader(other.getId())))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    @DisplayName("到书保留超时后预约自动失效（BR-010）")
    void expireOverdueReservation() {
        givenReaderAndTitle();
        Reservation reservation = reservationRepository.save(
                new Reservation(reader.getId(), title.getId(), LocalDateTime.now().minusDays(10), 1));
        reservation.holdUntil(LocalDateTime.now().minusDays(3));
        reservationRepository.save(reservation);

        int expired = reservationService.expireOverdue(LocalDateTime.now());

        assertThat(expired).isPositive();
        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.EXPIRED);
    }
}
