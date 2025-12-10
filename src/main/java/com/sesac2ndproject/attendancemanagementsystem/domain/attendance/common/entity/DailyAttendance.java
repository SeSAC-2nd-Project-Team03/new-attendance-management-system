package com.sesac2ndproject.attendancemanagementsystem.domain.attendance.common.entity;

import com.sesac2ndproject.attendancemanagementsystem.global.entity.BaseTimeEntity;
import com.sesac2ndproject.attendancemanagementsystem.global.type.AttendanceStatus;
import com.sesac2ndproject.attendancemanagementsystem.global.type.AttendanceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 일별 출석 현황 엔티티
 * 한 학생의 하루 출석 상태를 시간대별로 관리합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "daily_attendance",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_course_date",
                        columnNames = {"memberId", "courseId", "date"}
                )
        }
)
public class DailyAttendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private LocalDate date;

    // 시간대별 출석 상태 (PRESENT, LATE, ABSENT 등)
    @Enumerated(EnumType.STRING)
    private AttendanceStatus morningStatus;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus lunchStatus;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus dinnerStatus;

    // 전체 상태 (레거시 호환 또는 요약용)
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    /**
     * 생성자: 새로운 일별 출석 현황 생성
     */
    public DailyAttendance(Long memberId, Long courseId, LocalDate date) {
        this.memberId = memberId;
        this.courseId = courseId;
        this.date = date;
    }

    /**
     * 시간대별 출석 상태 업데이트
     * @param type 출석 타입 (MORNING, LUNCH, DINNER)
     * @param status 출석 상태 (PRESENT, LATE, ABSENT)
     */
    public void markPeriod(AttendanceType type, AttendanceStatus status) {
        switch (type) {
            case MORNING -> this.morningStatus = status;
            case LUNCH -> this.lunchStatus = status;
            case DINNER -> this.dinnerStatus = status;
        }
        updateOverallStatus();
    }

    /**
     * 특정 시간대의 출석 상태 조회
     */
    public AttendanceStatus getStatusByType(AttendanceType type) {
        return switch (type) {
            case MORNING -> this.morningStatus;
            case LUNCH -> this.lunchStatus;
            case DINNER -> this.dinnerStatus;
        };
    }

    /**
     * 전체 출석 상태 업데이트 (3개 시간대 기준)
     * - 모두 PRESENT → PRESENT
     * - 하나라도 ABSENT → ABSENT
     * - 하나라도 LATE (ABSENT 없음) → LATE
     */
    private void updateOverallStatus() {
        recalculateOverallStatus();
    }
    
    /**
     * 전체 출석 상태 재계산 (외부에서 호출 가능)
     */
    public void recalculateOverallStatus() {
        if (morningStatus == null || lunchStatus == null || dinnerStatus == null) {
            // 아직 모든 시간대가 기록되지 않음
            return;
        }

        boolean hasAbsent = morningStatus == AttendanceStatus.ABSENT
                || lunchStatus == AttendanceStatus.ABSENT
                || dinnerStatus == AttendanceStatus.ABSENT;

        boolean hasLate = morningStatus == AttendanceStatus.LATE
                || lunchStatus == AttendanceStatus.LATE
                || dinnerStatus == AttendanceStatus.LATE;

        if (hasAbsent) {
            this.status = AttendanceStatus.ABSENT;
        } else if (hasLate) {
            this.status = AttendanceStatus.LATE;
        } else {
            this.status = AttendanceStatus.PRESENT;
        }
    }

    // 출석 상태를 PRESENT로 변경 (모든 시간대도 PRESENT로 변경)
    public void changeStatusPresent() {
        this.status = AttendanceStatus.PRESENT;
        this.morningStatus = AttendanceStatus.PRESENT;
        this.lunchStatus = AttendanceStatus.PRESENT;
        this.dinnerStatus = AttendanceStatus.PRESENT;
    }

    // DailyAttendance.java 안에 추가
    public void changeStatusToOfficialLeave() {
        this.status = AttendanceStatus.OFFICIAL_LEAVE;
        this.morningStatus = AttendanceStatus.OFFICIAL_LEAVE;
        this.lunchStatus = AttendanceStatus.OFFICIAL_LEAVE;
        this.dinnerStatus = AttendanceStatus.OFFICIAL_LEAVE;
    }

    /**
     * 개별 시간대 상태 변경 (관리자용)
     */
    public void updateMorningStatus(AttendanceStatus status) {
        this.morningStatus = status;
        recalculateOverallStatus();
    }

    public void updateLunchStatus(AttendanceStatus status) {
        this.lunchStatus = status;
        recalculateOverallStatus();
    }

    public void updateDinnerStatus(AttendanceStatus status) {
        this.dinnerStatus = status;
        recalculateOverallStatus();
    }

    /**
     * 전체 상태 직접 변경 (관리자용)
     */
    public void updateOverallStatusDirect(AttendanceStatus status) {
        this.status = status;
    }
}
