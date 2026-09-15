package com.medtate.CalBag.calendar.service;

import com.medtate.CalBag.calendar.domain.Calendar;
import com.medtate.CalBag.calendar.domain.CalendarMember;
import com.medtate.CalBag.calendar.domain.CalendarRole;
import com.medtate.CalBag.calendar.dto.CalendarCreateRequest;
import com.medtate.CalBag.calendar.dto.CalendarResponse;
import com.medtate.CalBag.calendar.dto.CalendarUpdateRequest;
import com.medtate.CalBag.calendar.dto.InviteCodeResponse;
import com.medtate.CalBag.calendar.dto.MemberResponse;
import com.medtate.CalBag.calendar.repository.CalendarMemberRepository;
import com.medtate.CalBag.calendar.repository.CalendarRepository;
import com.medtate.CalBag.event.repository.EventRepository;
import com.medtate.CalBag.global.exception.BusinessException;
import com.medtate.CalBag.notification.repository.NotificationRepository;
import com.medtate.CalBag.user.domain.User;
import com.medtate.CalBag.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 0/O, 1/I 제외
    private static final int CODE_LENGTH = 8;

    private final CalendarRepository calendarRepository;
    private final CalendarMemberRepository calendarMemberRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;

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

        deleteCalendarWithEvents(calendar);
    }

    @Transactional
    public InviteCodeResponse issueInviteCode(Integer userId, Integer calendarId) {
        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new BusinessException("캘린더를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        CalendarMember me = calendarMemberRepository.findByCalendarIdAndUserId(calendarId, userId)
                .orElseThrow(() -> new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN));
        if (me.getRole() != CalendarRole.OWNER) {
            throw new BusinessException("초대 코드 발급은 오너만 가능합니다.", HttpStatus.FORBIDDEN);
        }

        calendar.issueInviteCode(generateCode(), LocalDateTime.now().plusDays(7));
        return new InviteCodeResponse(calendar);
    }

    @Transactional
    public CalendarResponse joinByInviteCode(Integer userId, String code) {
        Calendar calendar = calendarRepository.findByInviteCode(code)
                .filter(c -> c.getInviteCodeExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new BusinessException("유효하지 않은 초대 코드입니다.", HttpStatus.NOT_FOUND));

        if (calendarMemberRepository.existsByCalendarIdAndUserId(calendar.getId(), userId)) {
            throw new BusinessException("이미 캘린더 멤버입니다.", HttpStatus.CONFLICT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        calendarMemberRepository.save(CalendarMember.builder()
                .calendar(calendar)
                .user(user)
                .role(CalendarRole.EDITOR)
                .build());

        return new CalendarResponse(calendar);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(Integer userId, Integer calendarId) {
        if (!calendarMemberRepository.existsByCalendarIdAndUserId(calendarId, userId)) {
            throw new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        return calendarMemberRepository.findAllWithUserByCalendarId(calendarId)
                .stream()
                .map(MemberResponse::new)
                .toList();
    }

    @Transactional
    public void removeMember(Integer userId, Integer calendarId, Integer targetUserId) {
        CalendarMember me = calendarMemberRepository.findByCalendarIdAndUserId(calendarId, userId)
                .orElseThrow(() -> new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN));

        boolean leaving = userId.equals(targetUserId);
        if (!leaving && me.getRole() != CalendarRole.OWNER) {
            throw new BusinessException("멤버 내보내기는 오너만 가능합니다.", HttpStatus.FORBIDDEN);
        }

        CalendarMember target = leaving ? me
                : calendarMemberRepository.findByCalendarIdAndUserId(calendarId, targetUserId)
                        .orElseThrow(() -> new BusinessException("멤버를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (target.getRole() == CalendarRole.OWNER) { // 오너가 나가는 경우
            Optional<CalendarMember> successor = calendarMemberRepository
                    .findFirstByCalendarIdAndRoleNotOrderByJoinedAtAsc(calendarId, CalendarRole.OWNER);

            if (successor.isEmpty()) {
                deleteCalendarWithEvents(target.getCalendar());
                return;
            }
            successor.get().promoteToOwner();
        }

        notificationRepository.deleteByUserIdAndCalendarId(targetUserId, calendarId);
        calendarMemberRepository.delete(target);
    }

    private void deleteCalendarWithEvents(Calendar calendar) {
        notificationRepository.deleteByCalendarId(calendar.getId());
        eventRepository.deleteByCalendarId(calendar.getId());
        calendarRepository.delete(calendar);
    }
}