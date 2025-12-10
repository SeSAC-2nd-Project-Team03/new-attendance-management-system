package com.sesac2ndproject.attendancemanagementsystem.global.type;

import lombok.Getter;

@Getter
public enum NotificationType {
    LEAVE_REQUEST("휴가 신청", "새로운 휴가 신청이 접수되었습니다."),
    LEAVE_APPROVED("휴가 승인", "휴가 신청이 승인되었습니다."),
    LEAVE_REJECTED("휴가 반려", "휴가 신청이 반려되었습니다."),
    ATTENDANCE_CHECK("출석 체크", "출석 체크가 완료되었습니다."),
    SYSTEM_NOTICE("시스템 공지", "시스템 공지사항이 있습니다.");

    private final String title;
    private final String description;

    NotificationType(String title, String description) {
        this.title = title;
        this.description = description;
    }
}

