package com.sesac2ndproject.attendancemanagementsystem.domain.leave.controller;

import com.sesac2ndproject.attendancemanagementsystem.domain.leave.dto.LeaveRequestCreateDto;
import com.sesac2ndproject.attendancemanagementsystem.domain.leave.dto.LeaveRequestResponseDto;
import com.sesac2ndproject.attendancemanagementsystem.domain.leave.service.LeaveRequestService;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.global.response.ApiResponse;
import com.sesac2ndproject.attendancemanagementsystem.global.type.LeaveType;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    /**
     * [사용자] 휴가 신청
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "휴가 신청 생성", description = "새로운 휴가 신청을 생성합니다 (파일 첨부 가능)")
    public ResponseEntity<LeaveRequestResponseDto> createLeaveRequest(
            @RequestParam("leaveDate") String leaveDateStr,
            @RequestParam("reason") String reason,
            @RequestParam("leaveType") LeaveType leaveType,
            @RequestPart(value = "evidenceFile", required = false) MultipartFile evidenceFile,
            @RequestHeader("Student-Login-Id") String studentLoginId
    ) {
        // 1. 파일 검증 (파일이 있을 경우에만)
        if (evidenceFile != null && !evidenceFile.isEmpty()) {
            if (!isValidFileType(evidenceFile.getContentType())) {
                throw new IllegalArgumentException("이미지(jpg, png) 또는 PDF 파일만 첨부 가능합니다.");
            }
        }

        // 2. 날짜 파싱
        LocalDate leaveDate;
        if (leaveDateStr.contains("-")) {
            leaveDate = LocalDate.parse(leaveDateStr);
        } else {
            leaveDate = LocalDate.parse(leaveDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        // 3. DTO 생성
        LeaveRequestCreateDto dto = LeaveRequestCreateDto.builder()
                .startDate(leaveDate) // 시작일 설정
                .endDate(leaveDate)   // 종료일도 같은 날짜로 설정 (1일 휴가인 경우)
                .leaveType(leaveType)
                .reason(reason)
                .build();

        LeaveRequestResponseDto response =
                leaveRequestService.createLeaveRequest(studentLoginId, dto, evidenceFile);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * [사용자] 내 신청 내역 조회
     */
    @GetMapping("/me")
    @Operation(summary = "내 휴가 신청 목록 조회", description = "본인이 신청한 휴가 신청 목록을 조회합니다")
    public ResponseEntity<List<LeaveRequestResponseDto>> getMyLeaveRequests(
            @RequestHeader("Student-Login-Id") String studentLoginId
    ) {
        List<LeaveRequestResponseDto> requests =
                leaveRequestService.getMyLeaveRequests(studentLoginId);
        return ResponseEntity.ok(requests);
    }

    /**
     * [사용자] 신청 취소 (파일 삭제 포함)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "휴가 신청 취소", description = "PENDING 상태의 자신의 휴가 신청만 취소할 수 있습니다")
    public ResponseEntity<Void> cancelLeaveRequest(
            @PathVariable Long id,
            @RequestHeader("Student-Login-Id") String studentLoginId
    ) {
        leaveRequestService.cancelLeaveRequest(id, studentLoginId);
        return ResponseEntity.noContent().build();
    }

    // ================= [관리자 기능] ================= //

    /**
     * [관리자] 신청 승인
     * PATCH /api/v1/leave-requests/admin/{id}/approve
     */
    @PatchMapping("/admin/{id}/approve")
    @Operation(summary = "조퇴/결석 신청 승인", description = "신청 상태를 APPROVED로 변경합니다.")
    public ResponseEntity<ApiResponse<LeaveRequestResponseDto>> approveLeaveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal Member admin // 현재 로그인한 관리자 정보 가져오기
    ) {
        // 1. 관리자 이름 추출 (보안 컨텍스트에서 가져옴)
        // admin이 null인 경우(테스트 등) 대비하여 기본값 처리
        String adminName = (admin != null) ? admin.getName() : "ADMIN";

        // 2. 서비스 호출 (신청서 ID + 관리자 이름 전달)
        LeaveRequestResponseDto result = leaveRequestService.approveLeaveRequest(id, adminName);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * [관리자] 신청 반려
     * PATCH /api/v1/leave-requests/admin/{id}/reject
     */
    @PatchMapping("/admin/{id}/reject")
    @Operation(summary = "휴가 신청 반려", description = "관리자가 휴가 신청을 반려 처리합니다.")
    public ResponseEntity<ApiResponse<LeaveRequestResponseDto>> rejectLeaveRequest(
            @PathVariable Long id,
            @RequestParam("reason") String rejectionReason,
            @AuthenticationPrincipal Member admin // @RequestHeader 대신 사용
    ) {
        // 1. 관리자 이름 추출
        String adminName = (admin != null) ? admin.getName() : "Unknown Admin";

        // 2. 서비스 호출 (DTO 반환)
        LeaveRequestResponseDto result = leaveRequestService.rejectLeaveRequest(id, adminName, rejectionReason);

        // 3. 응답 반환
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * [관리자] 모든 휴가 신청 조회
     * GET /api/v1/leave-requests/admin
     */
    @GetMapping("/admin")
    @Operation(summary = "모든 휴가 신청 조회", description = "관리자가 모든 휴가 신청 내역을 조회합니다.")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponseDto>>> getAllLeaveRequests() {
        List<LeaveRequestResponseDto> requests = leaveRequestService.getAllLeaveRequests();
        return ResponseEntity.ok(ApiResponse.success(requests));
    }
    
    /**
     * [관리자] 대기 중인 휴가 신청 조회
     * GET /api/v1/leave-requests/admin/pending
     */
    @GetMapping("/admin/pending")
    @Operation(summary = "대기 중인 휴가 신청 조회", description = "관리자가 승인 대기 중인 휴가 신청만 조회합니다.")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponseDto>>> getPendingLeaveRequests() {
        List<LeaveRequestResponseDto> requests = leaveRequestService.getPendingLeaveRequests();
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    // ================= [내부 유틸 메서드] ================= //

    /**
     * 파일 타입 검증 로직
     */
    private boolean isValidFileType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.startsWith("image/") ||
                contentType.equals("application/pdf");
    }
}