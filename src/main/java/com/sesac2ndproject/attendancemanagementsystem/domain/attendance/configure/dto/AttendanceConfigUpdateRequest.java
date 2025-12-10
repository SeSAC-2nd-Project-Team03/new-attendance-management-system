package com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class AttendanceConfigUpdateRequest {

    private String authNumber; // 인증번호 변경

    private LocalTime standardTime;  // 지각 기준 시간 (이후 지각 처리)

    private LocalTime deadline;      // 마감 시간

    private LocalTime startTime;     // 출석 시작 시간 (프론트엔드에서 직접 입력)

    private Integer validMinutes;    // 유효 시간 (분) - startTime이 있으면 자동 계산
}