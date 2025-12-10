package com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.dto;

import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.entity.AttendanceConfig;
import com.sesac2ndproject.attendancemanagementsystem.global.type.AttendanceType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class AttendanceConfigResponse {

    private Long id;
    private Long courseId;
    private Long adminId;
    private AttendanceType type;
    private String authNumber;
    private LocalDate targetDate;
    private LocalTime startTime;      // 출석 시작 시간 (계산값: standardTime - validMinutes)
    private LocalTime standardTime;   // 지각 기준 시간 (이 시간 이후는 지각)
    private LocalTime deadline;       // 마감 시간 (이 시간 이후는 출석 불가)
    private Integer validMinutes;

    public static AttendanceConfigResponse from(AttendanceConfig entity) {
        // 출석 시작 시간 = 지각 기준 시간 - 유효 시간
        LocalTime calculatedStartTime = entity.getStandardTime().minusMinutes(entity.getValidMinutes());
        
        return AttendanceConfigResponse.builder()
                .id(entity.getId())
                .courseId(entity.getCourseId())
                .adminId(entity.getAdminId())
                .type(entity.getType())
                .authNumber(entity.getAuthNumber())
                .targetDate(entity.getTargetDate())
                .startTime(calculatedStartTime)
                .standardTime(entity.getStandardTime())
                .deadline(entity.getDeadline())
                .validMinutes(entity.getValidMinutes())
                .build();
    }
}
