package com.sesac2ndproject.attendancemanagementsystem.domain.notification.repository;

import com.sesac2ndproject.attendancemanagementsystem.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // 특정 회원의 알림 조회 (최신순)
    List<Notification> findByMember_IdOrderByCreatedAtDesc(Long memberId);
    
    // 특정 회원의 읽지 않은 알림 조회
    List<Notification> findByMember_IdAndIsReadFalseOrderByCreatedAtDesc(Long memberId);
    
    // 특정 회원의 읽지 않은 알림 개수
    long countByMember_IdAndIsReadFalse(Long memberId);
}

