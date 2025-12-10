package com.sesac2ndproject.attendancemanagementsystem.domain.notification.entity;

import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.global.entity.BaseTimeEntity;
import com.sesac2ndproject.attendancemanagementsystem.global.type.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림을 받을 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 알림 타입 (LEAVE_REQUEST, LEAVE_APPROVED, LEAVE_REJECTED 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // 알림 제목
    @Column(nullable = false, length = 200)
    private String title;

    // 알림 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 읽음 여부
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    // 관련 엔티티 ID (예: LeaveRequest ID)
    private Long relatedEntityId;

    // 읽은 시간
    private LocalDateTime readAt;

    // ================= 비즈니스 메서드 ================= //

    /**
     * 알림 읽음 처리
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }
}

