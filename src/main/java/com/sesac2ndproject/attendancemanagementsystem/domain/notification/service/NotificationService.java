package com.sesac2ndproject.attendancemanagementsystem.domain.notification.service;

import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.repository.MemberRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.notification.entity.Notification;
import com.sesac2ndproject.attendancemanagementsystem.domain.notification.repository.NotificationRepository;
import com.sesac2ndproject.attendancemanagementsystem.global.type.NotificationType;
import com.sesac2ndproject.attendancemanagementsystem.global.type.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    /**
     * 알림 생성
     */
    public Notification createNotification(Long memberId, NotificationType type, String title, String content, Long relatedEntityId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Notification notification = Notification.builder()
                .member(member)
                .type(type)
                .title(title)
                .content(content)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * 관리자에게 알림 생성 (휴가 신청 시)
     */
    public void notifyAdminAboutLeaveRequest(Long leaveRequestId, String studentName, String leaveType) {
        // 모든 관리자 조회
        List<Member> admins = memberRepository.findByRole(RoleType.ADMIN);

        for (Member admin : admins) {
            createNotification(
                    admin.getId(),
                    NotificationType.LEAVE_REQUEST,
                    "휴가 신청 알림",
                    String.format("%s님이 %s 신청을 하였습니다.", studentName, leaveType),
                    leaveRequestId
            );
        }
        log.info("관리자들에게 휴가 신청 알림 전송 완료 (신청 ID: {})", leaveRequestId);
    }

    /**
     * 학생에게 알림 생성 (승인/반려 시)
     */
    public void notifyStudentAboutLeaveResult(Long studentId, NotificationType type, String title, String content, Long leaveRequestId) {
        createNotification(studentId, type, title, content, leaveRequestId);
        log.info("학생에게 휴가 처리 결과 알림 전송 완료 (학생 ID: {}, 신청 ID: {})", studentId, leaveRequestId);
    }

    /**
     * 알림 조회 (특정 회원)
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(Long memberId) {
        return notificationRepository.findByMember_IdOrderByCreatedAtDesc(memberId);
    }

    /**
     * 읽지 않은 알림 조회
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long memberId) {
        return notificationRepository.findByMember_IdAndIsReadFalseOrderByCreatedAtDesc(memberId);
    }

    /**
     * 읽지 않은 알림 개수
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long memberId) {
        return notificationRepository.countByMember_IdAndIsReadFalse(memberId);
    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead(Long notificationId, Long memberId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        // 본인의 알림만 읽을 수 있도록 검증
        if (!notification.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 알림만 읽을 수 있습니다.");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /**
     * 모든 알림 읽음 처리
     */
    public void markAllAsRead(Long memberId) {
        List<Notification> unreadNotifications = getUnreadNotifications(memberId);
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
        notificationRepository.saveAll(unreadNotifications);
    }
}

