package com.medtate.CalBag.calendar.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "calendars")
@Getter
@NoArgsConstructor
public class Calendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String color;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalendarMember> members = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(unique = true)
    private String inviteCode;

    private LocalDateTime inviteCodeExpiresAt;

    @Builder
    public Calendar(String title, String description, String color) {
        this.title = title;
        this.description = description;
        this.color = color;
        this.createdAt = LocalDateTime.now();
    }

    public void update(String title, String description, String color) {
        this.title = title;
        this.description = description;
        this.color = color;
    }

    public void issueInviteCode(String code, LocalDateTime expiresAt) {
        this.inviteCode = code;
        this.inviteCodeExpiresAt = expiresAt;
    }
}