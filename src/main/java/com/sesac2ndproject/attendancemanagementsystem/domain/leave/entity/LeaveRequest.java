package com.sesac2ndproject.attendancemanagementsystem.domain.leave.entity;

import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.global.entity.BaseTimeEntity;
import com.sesac2ndproject.attendancemanagementsystem.global.type.LeaveStatus;
import com.sesac2ndproject.attendancemanagementsystem.global.type.LeaveType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 신청했는지 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 어떤 종류의 휴가인지
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;

    // 언제부터
    @Column(nullable = false)
    private LocalDate startDate;

    // 언제까지 (당일이면 startDate와 동일)
    @Column(nullable = false)
    private LocalDate endDate;

    // 사유
    @Column(nullable = false, length = 500)
    private String reason;

    // 증빙 서류 파일 경로
    @Column
    private String fileUrl;

    // 상태 (기본값 PENDING)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    // 처리자 (승인/반려한 사람) ID 또는 이름
    @Column
    private String processedBy;

    // 처리 일시
    @Column
    private LocalDateTime processedAt;

    // 관리자 코멘트 (반려 사유 등)
    @Column
    private String adminComment;

    // ================= 비즈니스 메서드 ================= //

    /**
     * 휴가 신청 승인 처리
     */
    public void approve(String adminName) {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException("대기 상태(PENDING)인 요청만 승인할 수 있습니다.");
        }
        this.status = LeaveStatus.APPROVED;
        this.processedBy = adminName;
        this.processedAt = LocalDateTime.now();
        this.adminComment = "승인되었습니다.";
    }

    /**
     * 휴가 신청 반려 처리
     */
    public void reject(String adminName, String reason) {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException("대기 상태(PENDING)인 요청만 반려할 수 있습니다.");
        }
        this.status = LeaveStatus.REJECTED;
        this.processedBy = adminName;
        this.processedAt = LocalDateTime.now();
        this.adminComment = reason;
    }

    // LeaveRequest.java 엔티티에서 cancel() 메서드 개선
    // (기존 코드와 유사하지만 취소 시간 기록 추가)

    /**
     * 본인 취소
     */
    public void cancel() {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 내역은 취소할 수 없습니다.");
        }
        this.status = LeaveStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();  // 취소 시간 기록
        this.processedBy = "SELF_CANCELLED";      // 본인 취소임을 표시 (선택사항)
    }
}