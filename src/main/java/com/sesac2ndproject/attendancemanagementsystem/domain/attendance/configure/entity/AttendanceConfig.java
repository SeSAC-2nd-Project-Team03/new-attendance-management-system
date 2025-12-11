package com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.entity;

import com.sesac2ndproject.attendancemanagementsystem.global.entity.BaseTimeEntity;
import com.sesac2ndproject.attendancemanagementsystem.global.type.AttendanceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 출석 설정 엔티티
 * 관리자가 설정하는 출석 인증번호와 기준시간 정보
 *
 * Person 1은 이 테이블을 READ ONLY로 사용합니다.
 * (DataInitializer 또는 관리자가 설정 데이터를 INSERT/UPDATE)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "attendance_config",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_date_session",
                        columnNames = {"courseId", "targetDate", "type"}
                )
        }
)
public class AttendanceConfig extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long courseId; // 어떤 과정(반)의 출석인지

    @Column(nullable = false)
    private Long adminId; // 누가 이 번호를 만들었는지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceType type; // MORNING(아침), LUNCH(점심), DINNER(저녁)

    @Column(nullable = false)
    private String authNumber; // 정답 번호 (예: "1234")

    @Column(nullable = false)
    private LocalDate targetDate; // 출석 날짜 (예: 2025-11-28)

    @Column(nullable = false)
    private LocalTime standardTime; // 기준 시간 (예: 08:50, 13:10)

    @Column(nullable = false)
    private LocalTime deadline; // 마감 시간 (예: 09:10, 13:30)

    @Column(nullable = false)
    private Integer validMinutes ; // 유효 시간 (분 단위, 예: 20분)


    public void updateTime(LocalTime standardTime, Integer validMinutes) {
        this.standardTime = standardTime;
        this.validMinutes = validMinutes;
        this.deadline = calculateDeadline(standardTime, validMinutes);
    }

    /**
     * 출석 시간 전체 업데이트 (시작 시간, 지각 기준 시간, 마감 시간)
     */
   /* public void updateTimeConfig(LocalTime startTime, LocalTime standardTime, LocalTime deadline) {
        if (standardTime != null) {
            this.standardTime = standardTime;
        }
        if (deadline != null) {
            this.deadline = deadline;
        }
        // startTime과 standardTime으로 validMinutes 자동 계산
        if (startTime != null && this.standardTime != null) {
            int minutes = (int) java.time.Duration.between(startTime, this.standardTime).toMinutes();
            this.validMinutes = Math.abs(minutes);
        }
    }*/

    public void updateAuthNumber(String authNumber) {
        this.authNumber = authNumber;
    }

    public static AttendanceConfig create(Long adminId, Long courseId, AttendanceType type, String authNumber, LocalDate targetDate, LocalTime standardTime, Integer validMinutes) {
        // 기본값 처리
        int actualValidMinutes = (validMinutes != null) ? validMinutes : 20;

        return AttendanceConfig.builder()
                .adminId(adminId)
                .courseId(courseId)
                .type(type)
                .authNumber(authNumber)
                .targetDate(targetDate)
                .standardTime(standardTime)
                .validMinutes(actualValidMinutes) // 처리된 값 사용
                .deadline(calculateDeadline(standardTime, actualValidMinutes)) // 마감시간 계산
                .build();
    }


    // 내부 계산 로직
    private static LocalTime calculateDeadline(LocalTime standard, Integer minutes) {
        return standard.plusMinutes(minutes);
    }
}