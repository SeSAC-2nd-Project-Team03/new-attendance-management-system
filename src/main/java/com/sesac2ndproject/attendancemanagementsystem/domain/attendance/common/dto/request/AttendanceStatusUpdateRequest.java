package com.sesac2ndproject.attendancemanagementsystem.domain.attendance.common.dto.request;

import com.sesac2ndproject.attendancemanagementsystem.global.type.AttendanceStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 관리자용 출석 상태 개별 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class AttendanceStatusUpdateRequest {
    
    private Long memberId;
    private Long courseId;
    private LocalDate date;
    
    private AttendanceStatus morningStatus;   // null이면 변경 안 함
    private AttendanceStatus lunchStatus;     // null이면 변경 안 함
    private AttendanceStatus dinnerStatus;    // null이면 변경 안 함
    private AttendanceStatus overallStatus;   // null이면 자동 계산
}

