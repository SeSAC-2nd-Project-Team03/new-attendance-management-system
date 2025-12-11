package com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.dto;

import com.sesac2ndproject.attendancemanagementsystem.global.type.AttendanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceConfigCreateRequest {
    @NotNull(message = "과정 ID는 필수입니다.")
    private Long courseId;

    @NotNull(message = "출석 타입(MORNING, LUNCH, DINNER)은 필수입니다.")
    private AttendanceType type;

    @NotBlank(message = "인증번호는 필수입니다.")
    private String authNumber;

    @NotNull(message = "출석 대상 날짜는 필수입니다.")
    private LocalDate targetDate;

    @NotNull(message = "출석 기준 시간은 필수입니다.")
    private LocalTime standardTime;

    @NotNull(message = "유효 시간은 필수입니다.")
    @Builder.Default
    private Integer validMinutes = 20;
}
