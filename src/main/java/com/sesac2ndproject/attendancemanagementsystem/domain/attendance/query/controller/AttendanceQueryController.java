package com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.controller;

import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.dto.ResponseAttendanceByDateDTO;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.dto.MyAttendanceResponse;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.service.AttendanceQueryService;
import com.sesac2ndproject.attendancemanagementsystem.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendance (Query)", description = "출석 현황 조회 API")
public class AttendanceQueryController {

    private final AttendanceQueryService attendanceQueryService;

    /**
     * 내 출석 조회 API
     */
    @GetMapping("/me")
    @Operation(
            summary = "내 출석 조회",
            description = """
            특정 날짜의 출석 현황을 조회합니다.
            
            **상태 계산 규칙:**
            - O + O + O → 출석
            - △ + O + O → 지각
            - X + O + O → 지각
            - O + O + X → 조퇴
            - X + X + X → 결석
            """
    )
    public ResponseEntity<MyAttendanceResponse> getMyAttendance(
            @Parameter(description = "회원 ID") @RequestParam Long memberId,
            @Parameter(description = "강의 ID") @RequestParam Long courseId,
            @Parameter(description = "조회 날짜 (기본: 오늘)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date == null) {
            date = LocalDate.now();
        }
        log.info("GET /api/v1/attendances/me - memberId: {}, courseId: {}, date: {}", memberId, courseId, date);

        MyAttendanceResponse response = attendanceQueryService.getMyAttendance(memberId, courseId, date);
        return ResponseEntity.ok(response);
    }

    //- **API 개발 (관리자용)**
    //    - [ ]  **전체 출석 현황 조회 API** (`GET /api/v1/admin/attendances`): 날짜별, 과정별 전체 학생의 출석 상태 리스트 반환14
    // 날짜 -> DAILY_ATTENDANCE 반환. COURSE의 start_date와 end_date 사이에 있는 코스를 반환.
    @GetMapping("/admin/daily-status")
    @Operation(summary = "전체 출석 현황 조회", description = "Date와 courseId로 조회")
    public ResponseEntity<ApiResponse<List<ResponseAttendanceByDateDTO>>> getDailyAttendanceList(
            //날짜 형식을 "yyyy-MM-dd"로 받기 위해 어노테이션 사용
            @RequestParam("workDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
            @RequestParam("courseId") Long courseId
    ) {
        List<ResponseAttendanceByDateDTO> result = attendanceQueryService.getDailyAttendance(workDate, courseId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    //    - [ ]  **CSV/Excel 다운로드 API**: 현재 조회된 출석부 데이터를 파일로 변환하여 응답.
    @GetMapping("/admin/export")
    @Operation(summary = "출석부 다운로드", description = "출석부를 다운 받을 수 있습니다.[필수 입력] type(csv/excel)[선택 입력]date & courseId (미입력시 전체 다운로드)")
    public ResponseEntity<ByteArrayResource> downloadAttendance(@RequestParam String downloadType,
                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
                                                                @RequestParam(required = false) Long courseId) {
        // 1. downloadType을 소문자로 변환
        downloadType = downloadType.toLowerCase();

        // 2. downloadType에 따른 파일 데이터 생성
        byte[] fileData = attendanceQueryService.downloadAttendanceStats(downloadType, workDate, courseId);

        // 3. 파일명 및 헤더 설정
        String fileName = "attendance_stats_" + LocalDate.now() + ("excel".equals(downloadType) ? ".xlsx" : ".csv");
        MediaType mediaType = "excel".equals(downloadType)
                ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                : MediaType.parseMediaType("text/csv");

        // 4. 파일 응답 반환
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(mediaType)
                .body(new ByteArrayResource(fileData));
    }
}
