package com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.controller;

import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.dto.AttendanceConfigCreateRequest;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.dto.AttendanceConfigResponse;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.dto.AttendanceConfigUpdateRequest;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.entity.AttendanceConfig;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.service.AttendanceConfigService;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.global.error.CustomException;
import com.sesac2ndproject.attendancemanagementsystem.global.error.ErrorCode;
import com.sesac2ndproject.attendancemanagementsystem.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Attendance (Admin)", description = "출석 설정 관리 API")
@RestController
@RequestMapping("/api/v1/admin/attendance-configs")
@RequiredArgsConstructor
public class AttendanceConfigController {
    

    private final AttendanceConfigService attendanceConfigService;

    /**
     * 출석 설정 생성
     * POST /api/v1/admin/attendance-configs
     */
    @Operation(summary = "출석 설정 생성", description = "관리자가 반, 날짜, 시간별 출석 인증번호를 설정합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createAttendanceConfig(
            @Valid @RequestBody AttendanceConfigCreateRequest request,
            @AuthenticationPrincipal Member admin
    ) {

        if (admin == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Long adminId = admin.getId();
        Long configId = attendanceConfigService.createAttendanceConfig(request, adminId);
        return ResponseEntity
                .created(URI.create("/api/v1/admin/attendance-configs/" + configId))
                .body(ApiResponse.success(configId));
    }

    /**
     * 출석 설정 목록 조회 (과정별)
     * GET /api/v1/admin/attendance-configs?courseId={id}
     */
    @Operation(summary = "출석 설정 목록 조회", description = "특정 과정(Course)의 모든 출석 설정을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceConfigResponse>>> getAttendanceConfigs(
            @Parameter(description = "과정 ID", required = true) @RequestParam Long courseId
    ) {
        List<AttendanceConfigResponse> responses = attendanceConfigService.getAttendanceConfigList(courseId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 출석 설정 상세 조회
     * GET /api/v1/admin/attendance-configs/{id}
     */
    @Operation(summary = "출석 설정 상세 조회", description = "ID로 특정 출석 설정을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceConfigResponse>> getAttendanceConfig(
            @PathVariable Long id
    ) {
        AttendanceConfigResponse response = attendanceConfigService.getAttendanceConfig(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 출석 설정 수정 (시간 규칙 변경)
     * PATCH /api/v1/admin/attendance-configs/{id}
     */
    @Operation(summary = "출석 시간 설정 수정", description = "기준 시간 및 유효 시간을 수정합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceConfigResponse>> updateAttendanceConfig(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceConfigUpdateRequest request
    ) {
        AttendanceConfigResponse response = attendanceConfigService.updateAttendanceConfig(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 인증번호 수정
     * PATCH /api/v1/admin/attendance-configs/{id}/auth-number
     */
    @Operation(summary = "인증번호 수정", description = "인증번호만 별도로 수정합니다.")
    @PatchMapping("/{id}/auth-number")
    public ResponseEntity<ApiResponse<Void>> updateAuthNumber(
            @PathVariable Long id,
            @RequestBody AttendanceConfigUpdateRequest request
            // 주의: DTO에 시간 필드에 @NotNull이 있다면, 인증번호만 보낼 때 에러가 날 수 있음.
            // 별도의 AuthUpdateDTO를 만드는 것을 권장하지만, 현재 서비스 구조에 맞춤.
    ) {
        attendanceConfigService.updateAuthNumber(id, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 출석 설정 삭제
     * DELETE /api/v1/admin/attendance-configs/{id}
     */
    @Operation(summary = "출석 설정 삭제", description = "특정 출석 설정을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendanceConfig(
            @PathVariable Long id
    ) {
        attendanceConfigService.deleteAttendanceConfig(id);
        return ResponseEntity.noContent().build();
    }
}
