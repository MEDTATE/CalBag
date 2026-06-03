package com.medtate.CalBag.calendar.domain;

import com.medtate.CalBag.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_members")
@Getter
@NoArgsConstructor
public class CalendarMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CalendarRole role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    public CalendarMember(Calendar calendar, User user, CalendarRole role) {
        this.calendar = calendar;
        this.user = user;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }
}