package com.medtate.CalBag.calendar.service;

import com.medtate.CalBag.calendar.domain.Calendar;
import com.medtate.CalBag.calendar.domain.CalendarMember;
import com.medtate.CalBag.calendar.domain.CalendarRole;
import com.medtate.CalBag.calendar.dto.CalendarCreateRequest;
import com.medtate.CalBag.calendar.dto.CalendarResponse;
import com.medtate.CalBag.calendar.dto.CalendarUpdateRequest;
import com.medtate.CalBag.calendar.repository.CalendarMemberRepository;
import com.medtate.CalBag.calendar.repository.CalendarRepository;
import com.medtate.CalBag.global.exception.BusinessException;
import com.medtate.CalBag.user.domain.User;
import com.medtate.CalBag.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final CalendarMemberRepository calendarMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public CalendarResponse createCalendar(Integer userId, CalendarCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Calendar calendar = Calendar.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .color(request.getColor())
                .build();

        calendarRepository.save(calendar);

        CalendarMember ownerMember = CalendarMember.builder()
                .calendar(calendar)
                .user(user)
                .role(CalendarRole.OWNER)
                .build();

        calendarMemberRepository.save(ownerMember);

        return new CalendarResponse(calendar);
    }

    @Transactional(readOnly = true)
    public List<CalendarResponse> getCalendars(Integer userId) {
        return calendarRepository.findAllByUserId(userId)
                .stream()
                .map(CalendarResponse::new)
                .toList();
    }

    @Transactional
    public CalendarResponse updateCalendar(Integer userId, Integer calendarId, CalendarUpdateRequest request) {
        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new BusinessException("캘린더를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        CalendarMember member = calendarMemberRepository.findByCalendarIdAndUserId(calendarId, userId)
                .orElseThrow(() -> new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN));

        if (member.getRole() != CalendarRole.OWNER) {
            throw new BusinessException("캘린더 수정은 오너만 가능합니다.", HttpStatus.FORBIDDEN);
        }

        calendar.update(request.getTitle(), request.getDescription(), request.getColor());

        return new CalendarResponse(calendar);
    }

    @Transactional
    public void deleteCalendar(Integer userId, Integer calendarId) {
        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new BusinessException("캘린더를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        CalendarMember member = calendarMemberRepository.findByCalendarIdAndUserId(calendarId, userId)
                .orElseThrow(() -> new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN));

        if (member.getRole() != CalendarRole.OWNER) {
            throw new BusinessException("캘린더 삭제는 오너만 가능합니다.", HttpStatus.FORBIDDEN);
        }

        calendarRepository.delete(calendar);
    }
}