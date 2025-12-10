package com.sesac2ndproject.attendancemanagementsystem.domain.leave.service;

import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.command.service.AttendanceCommandService;
import com.sesac2ndproject.attendancemanagementsystem.domain.leave.dto.LeaveRequestCreateDto;
import com.sesac2ndproject.attendancemanagementsystem.domain.leave.dto.LeaveRequestResponseDto;
import com.sesac2ndproject.attendancemanagementsystem.domain.leave.entity.LeaveRequest;
import com.sesac2ndproject.attendancemanagementsystem.domain.leave.repository.LeaveRequestRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.repository.MemberRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.notification.service.NotificationService;
import com.sesac2ndproject.attendancemanagementsystem.global.type.LeaveStatus;
import com.sesac2ndproject.attendancemanagementsystem.global.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final AttendanceCommandService attendanceCommandService;
    private final NotificationService notificationService;

    /**
     * 휴가 신청 생성 (파일 포함)
     */
    public LeaveRequestResponseDto createLeaveRequest(
            String studentLoginId,
            LeaveRequestCreateDto dto,
            MultipartFile file
    ) {
        try {
            // 1. 회원 조회
            Member member = memberRepository.findByLoginId(studentLoginId)
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

            // 2. 파일 업로드 (FileService 위임)
            String fileUrl = null;
            if (file != null && !file.isEmpty()) {
                fileUrl = fileService.upload(file);
            }

            // 3. 엔티티 생성 (DTO -> Entity)
            LeaveRequest leaveRequest = LeaveRequest.builder()
                    .member(member)
                    .type(dto.getLeaveType())
                    .startDate(dto.getStartDate()) // 하루짜리 휴가라고 가정
                    .endDate(dto.getEndDate())   // 시작일 = 종료일
                    .reason(dto.getReason())
                    .fileUrl(fileUrl) // 업로드된 URL 저장
                    .status(LeaveStatus.PENDING)
                    .build();

            // 4. 저장
            LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

            // 5. 관리자에게 알림 전송
            notificationService.notifyAdminAboutLeaveRequest(
                    saved.getId(),
                    member.getName(),
                    dto.getLeaveType().getDescription()
            );

            return LeaveRequestResponseDto.from(saved);

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 신청 취소 (본인, PENDING 상태만, 파일 삭제 포함)
     */
    public void cancelLeaveRequest(Long leaveId, String studentLoginId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다."));

        // 1. 본인 확인
        if (!leaveRequest.getMember().getLoginId().equals(studentLoginId)) {
            throw new IllegalArgumentException("본인의 신청만 취소할 수 있습니다.");
        }

        // 2. 상태 확인 (cancel 메서드 내부에서 체크하지만 이중 확인)
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("대기 중인 신청만 취소할 수 있습니다.");
        }

        // 3. 파일이 있다면 실제 파일 삭제 (물리적 삭제)
        if (leaveRequest.getFileUrl() != null) {
            fileService.delete(leaveRequest.getFileUrl());
            // (선택) DB상의 URL정보도 지우고 싶다면 -> leaveRequest.removeFileUrl(); 엔티티에 메서드 추가 필요
        }

        // 4. 상태 변경 (CANCELLED)
        leaveRequest.cancel();

        // 5. DB에 저장 (추가!)
        leaveRequestRepository.save(leaveRequest);
    }

    /**
     * [관리자] 휴가 승인
     */
    public LeaveRequestResponseDto approveLeaveRequest(Long leaveId, String adminName) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없습니다."));

        // 1. 상태 변경
        leaveRequest.approve(adminName);

        // 2. 출석 상태 변경 (연동)
        attendanceCommandService.applyLeaveEffect(leaveRequest.getMember().getId(), leaveRequest.getStartDate());

        // 3. 변경된 엔티티 -> DTO 변환 및 반환 (저장된 객체 사용)
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        
        // 4. 학생에게 승인 알림 전송
        notificationService.notifyStudentAboutLeaveResult(
                saved.getMember().getId(),
                NotificationType.LEAVE_APPROVED,
                "휴가 신청 승인",
                String.format("귀하의 %s 신청이 승인되었습니다.", saved.getType().getDescription()),
                saved.getId()
        );
        
        return LeaveRequestResponseDto.from(saved);
    }

    /**
     * [관리자] 휴가 반려
     */
    public LeaveRequestResponseDto rejectLeaveRequest(Long leaveId, String adminName, String rejectReason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없습니다."));

        // 1. 상태 변경 (반려)
        leaveRequest.reject(adminName, rejectReason);

        // 2. 저장 (변경 사항 DB 반영)
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        // 3. 학생에게 반려 알림 전송
        notificationService.notifyStudentAboutLeaveResult(
                saved.getMember().getId(),
                NotificationType.LEAVE_REJECTED,
                "휴가 신청 반려",
                String.format("귀하의 %s 신청이 반려되었습니다. 사유: %s", saved.getType().getDescription(), rejectReason),
                saved.getId()
        );

        // 4. DTO 변환 및 반환
        return LeaveRequestResponseDto.from(saved);
    }

    /**
     * 내 신청 목록 조회
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDto> getMyLeaveRequests(String studentLoginId) {
        return leaveRequestRepository.findByMember_LoginIdOrderByCreatedAtDesc(studentLoginId)
                .stream()
                .map(LeaveRequestResponseDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * [관리자] 모든 휴가 신청 조회
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDto> getAllLeaveRequests() {
        return leaveRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(LeaveRequestResponseDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * [관리자] 대기 중인 휴가 신청 조회
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDto> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatusOrderByCreatedAtDesc(LeaveStatus.PENDING)
                .stream()
                .map(LeaveRequestResponseDto::from)
                .collect(Collectors.toList());
    }
}