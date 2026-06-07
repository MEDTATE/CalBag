package com.medtate.CalBag.event.service;

import com.medtate.CalBag.calendar.domain.Calendar;
import com.medtate.CalBag.calendar.domain.CalendarRole;
import com.medtate.CalBag.calendar.repository.CalendarMemberRepository;
import com.medtate.CalBag.calendar.repository.CalendarRepository;
import com.medtate.CalBag.event.domain.Event;
import com.medtate.CalBag.event.dto.EventCreateRequest;
import com.medtate.CalBag.event.dto.EventResponse;
import com.medtate.CalBag.event.dto.EventUpdateRequest;
import com.medtate.CalBag.event.repository.EventRepository;
import com.medtate.CalBag.global.exception.BusinessException;
import com.medtate.CalBag.user.domain.User;
import com.medtate.CalBag.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CalendarRepository calendarRepository;
    private final CalendarMemberRepository calendarMemberRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional
    public EventResponse createEvent(Integer userId, Integer calendarId, EventCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new BusinessException("캘린더를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        calendarMemberRepository.findByCalendarIdAndUserId(calendarId, userId)
                .orElseThrow(() -> new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN));

        Event event = Event.builder()
                .calendar(calendar)
                .createdBy(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .color(request.getColor())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .isAllDay(request.isAllDay())
                .build();

        eventRepository.save(event);
        return new EventResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEvents(Integer userId, Integer calendarId,
                                         LocalDateTime startAt, LocalDateTime endAt) {
        calendarMemberRepository.findByCalendarIdAndUserId(calendarId, userId)
                .orElseThrow(() -> new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN));

        return eventRepository.findByCalendarIdAndDateRange(calendarId, startAt, endAt)
                .stream()
                .map(EventResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(Integer userId, Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("일정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        calendarMemberRepository.findByCalendarIdAndUserId(event.getCalendar().getId(), userId)
                .orElseThrow(() -> new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN));

        return new EventResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(Integer userId, Integer eventId, EventUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("일정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        calendarMemberRepository.findByCalendarIdAndUserId(event.getCalendar().getId(), userId)
                .orElseThrow(() -> new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN));

        CalendarRole role = calendarMemberRepository
                .findByCalendarIdAndUserId(event.getCalendar().getId(), userId)
                .get().getRole();

        if (role == CalendarRole.VIEWER) {
            throw new BusinessException("뷰어는 일정을 수정할 수 없습니다.", HttpStatus.FORBIDDEN);
        }

        if (!event.getVersion().equals(request.getVersion())) {
            throw new BusinessException("다른 사용자가 이미 수정했습니다. 다시 시도해주세요.", HttpStatus.CONFLICT);
        }

        try {
            event.update(request.getTitle(), request.getDescription(), request.getColor(),
                    request.getStartAt(), request.getEndAt(), request.isAllDay());
            eventRepository.save(event);
            eventRepository.flush();
            Event updatedEvent = eventRepository.findById(eventId).get();
            return new EventResponse(updatedEvent);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException("다른 사용자가 이미 수정했습니다. 다시 시도해주세요.", HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public void deleteEvent(Integer userId, Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("일정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        calendarMemberRepository.findByCalendarIdAndUserId(event.getCalendar().getId(), userId)
                .orElseThrow(() -> new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN));

        CalendarRole role = calendarMemberRepository
                .findByCalendarIdAndUserId(event.getCalendar().getId(), userId)
                .get().getRole();

        if (role == CalendarRole.VIEWER) {
            throw new BusinessException("뷰어는 일정을 삭제할 수 없습니다.", HttpStatus.FORBIDDEN);
        }

        eventRepository.delete(event);
    }
}