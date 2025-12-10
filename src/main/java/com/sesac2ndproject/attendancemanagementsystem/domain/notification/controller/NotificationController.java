package com.sesac2ndproject.attendancemanagementsystem.domain.notification.controller;

import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.domain.notification.entity.Notification;
import com.sesac2ndproject.attendancemanagementsystem.domain.notification.service.NotificationService;
import com.sesac2ndproject.attendancemanagementsystem.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 내 알림 목록 조회
     */
    @GetMapping("/me")
    @Operation(summary = "내 알림 목록 조회", description = "본인의 모든 알림을 조회합니다.")
    public ResponseEntity<ApiResponse<List<Notification>>> getMyNotifications(
            @AuthenticationPrincipal Member member
    ) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long memberId = member.getId();
        List<Notification> notifications = notificationService.getNotifications(memberId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * 읽지 않은 알림 조회
     */
    @GetMapping("/me/unread")
    @Operation(summary = "읽지 않은 알림 조회", description = "본인의 읽지 않은 알림만 조회합니다.")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications(
            @AuthenticationPrincipal Member member
    ) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long memberId = member.getId();
        List<Notification> notifications = notificationService.getUnreadNotifications(memberId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/me/unread-count")
    @Operation(summary = "읽지 않은 알림 개수", description = "본인의 읽지 않은 알림 개수를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal Member member
    ) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long memberId = member.getId();
        long count = notificationService.getUnreadCount(memberId);
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 알림 읽음 처리
     */
    @PatchMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal Member member
    ) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.markAsRead(id, member.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PatchMapping("/me/read-all")
    @Operation(summary = "모든 알림 읽음 처리", description = "본인의 모든 알림을 읽음 처리합니다.")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal Member member
    ) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.markAllAsRead(member.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

