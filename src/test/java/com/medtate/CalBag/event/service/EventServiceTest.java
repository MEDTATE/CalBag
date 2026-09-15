package com.medtate.CalBag.event.service;

import com.medtate.CalBag.calendar.domain.Calendar;
import com.medtate.CalBag.calendar.domain.CalendarMember;
import com.medtate.CalBag.calendar.domain.CalendarRole;
import com.medtate.CalBag.calendar.repository.CalendarMemberRepository;
import com.medtate.CalBag.calendar.repository.CalendarRepository;
import com.medtate.CalBag.event.domain.Event;
import com.medtate.CalBag.event.dto.EventResponse;
import com.medtate.CalBag.event.dto.EventUpdateRequest;
import com.medtate.CalBag.event.repository.EventRepository;
import com.medtate.CalBag.global.exception.BusinessException;
import com.medtate.CalBag.notification.domain.Notification;
import com.medtate.CalBag.notification.domain.NotificationType;
import com.medtate.CalBag.notification.repository.NotificationRepository;
import com.medtate.CalBag.user.domain.User;
import com.medtate.CalBag.user.repository.UserRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private CalendarMemberRepository calendarMemberRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired 
    private NotificationRepository notificationRepository;

    @Autowired 
    private EntityManager em;

    private User testUser;
    private Calendar testCalendar;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(
                User.builder()
                        .email("eventtest@test.com")
                        .passwordHash("hashedpassword")
                        .name("테스트유저")
                        .build()
        );


        testCalendar = calendarRepository.save(
                Calendar.builder()
                        .title("테스트 캘린더")
                        .description("설명")
                        .color("#FF0000")
                        .build()
        );

        calendarMemberRepository.save(
                CalendarMember.builder()
                        .calendar(testCalendar)
                        .user(testUser)
                        .role(CalendarRole.OWNER)
                        .build()
        );

        testEvent = eventRepository.save(
                Event.builder()
                        .calendar(testCalendar)
                        .createdBy(testUser)
                        .title("팀 회의")
                        .description("주간 회의")
                        .color("#0000FF")
                        .startAt(LocalDateTime.of(2026, 6, 10, 10, 0))
                        .endAt(LocalDateTime.of(2026, 6, 10, 11, 0))
                        .isAllDay(false)
                        .build()
        );
    }

    @Test
    @DisplayName("일정 수정 시 version이 일치하면 성공한다")
    void updateEvent_success() {
        EventUpdateRequest request = createUpdateRequest("수정된 회의", testEvent.getVersion());

        EventResponse response = eventService.updateEvent(
                testUser.getId(), testEvent.getId(), request);

        assertThat(response.getTitle()).isEqualTo("수정된 회의");
    }

    @Test
    @DisplayName("일정 수정 시 version이 불일치하면 충돌 에러가 발생한다")
    void updateEvent_optimisticLockConflict() {
        Integer wrongVersion = testEvent.getVersion() + 999;
        EventUpdateRequest request = createUpdateRequest("충돌 테스트", wrongVersion);

        assertThatThrownBy(() ->
                eventService.updateEvent(testUser.getId(), testEvent.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("다른 사용자가 이미 수정했습니다");
    }

    @Test
    @DisplayName("캘린더 멤버가 아니면 일정을 수정할 수 없다")
    void updateEvent_nonMemberCannotUpdate() {
        User outsider = saveUser("outsider@test.com");

        EventUpdateRequest request = createUpdateRequest("비멤버 수정 시도", testEvent.getVersion());

        assertThatThrownBy(() ->
                eventService.updateEvent(outsider.getId(), testEvent.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("접근 권한이 없습니다");
    }

    @Test
    @DisplayName("EDITOR 권한은 일정을 수정할 수 있다")
        void updateEvent_editorCanUpdate() {
        User editor = saveUser("editor@test.com");

        calendarMemberRepository.save(
                CalendarMember.builder()
                        .calendar(testCalendar)
                        .user(editor)
                        .role(CalendarRole.EDITOR)
                        .build()
        );

        EventUpdateRequest request = createUpdateRequest("에디터 수정", testEvent.getVersion());

        EventResponse response = eventService.updateEvent(editor.getId(), testEvent.getId(), request);

        assertThat(response.getTitle()).isEqualTo("에디터 수정");
    }

    @Test
    @DisplayName("알림이 있는 일정도 삭제할 수 있고, 알림도 함께 삭제된다")
    void deleteEvent_withNotification() {
        Notification notification = notificationRepository.save(
                Notification.builder()
                        .user(testUser)
                        .event(testEvent)
                        .type(NotificationType.TEN_MINUTES_BEFORE)
                        .scheduledAt(testEvent.getStartAt().minusMinutes(10))
                        .build()
        );

        em.flush();
        em.clear();

        eventService.deleteEvent(testUser.getId(), testEvent.getId());
        eventRepository.flush();

        assertThat(eventRepository.existsById(testEvent.getId())).isFalse();
        assertThat(notificationRepository.existsById(notification.getId())).isFalse();
    }

    private EventUpdateRequest createUpdateRequest(String title, Integer version) {
        return new EventUpdateRequest(
                title, "설명", "#FF0000",
                LocalDateTime.of(2026, 6, 10, 10, 0),
                LocalDateTime.of(2026, 6, 10, 11, 0),
                false, version
        );
    }


    private User saveUser(String email) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .passwordHash("hashedpassword")
                        .name("테스트유저")
                        .build()
    );
}
}