package com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.service;

import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.dto.ResponseAttendanceByDateDTO;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.dto.ResponseAttendanceFlatDTO;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.common.dto.response.AttendanceDetailResponse;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.dto.MyAttendanceResponse;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.common.entity.DailyAttendance;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.common.entity.DetailedAttendance;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.common.repository.DailyAttendanceRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.common.repository.DetailedAttendanceRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.course.repository.EnrollmentRepository;
import com.sesac2ndproject.attendancemanagementsystem.global.type.AttendanceRule;
import com.sesac2ndproject.attendancemanagementsystem.global.type.AttendanceStatus;
import com.sesac2ndproject.attendancemanagementsystem.global.util.CsvUtil;
import com.sesac2ndproject.attendancemanagementsystem.global.util.ExcelUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceQueryService {

    private final DailyAttendanceRepository dailyAttendanceRepository;
    private final DetailedAttendanceRepository detailedAttendanceRepository;
    private final EnrollmentRepository enrollmentRepository;



    /**
     * 내 출석 조회
     */
    public MyAttendanceResponse getMyAttendance(Long memberId, Long courseId, LocalDate date) {
        log.info("🔍 출석 조회 - memberId: {}, date: {}", memberId, date);

        // 1. DailyAttendance (일일 요약) 조회
        DailyAttendance daily = dailyAttendanceRepository
                .findByMemberIdAndCourseIdAndDate(memberId, courseId, date)
                .orElse(null);

        // 2. DetailedAttendance (상세 로그) 조회
        List<DetailedAttendance> detailedList = detailedAttendanceRepository
                .findByDate(memberId, courseId, date.atStartOfDay(), date.atTime(23, 59, 59));

        List<AttendanceDetailResponse> details = detailedList.stream()
                .map(AttendanceDetailResponse::from)
                .toList();

        // 3. 응답 생성
        if (daily == null) {
            return MyAttendanceResponse.builder()
                    .memberId(memberId).courseId(courseId).date(date)
                    .overallStatus(AttendanceStatus.NONE)
                    .overallStatusDescription("출석 기록 없음")
                    .details(details)
                    .build();
        }

        AttendanceStatus overallStatus = calculateDailyStatus(daily);
        return MyAttendanceResponse.of(daily, overallStatus, details);
    }

    /**
     * 일일 출석 상태 계산 로직 (순수 로직)
     */
    public AttendanceStatus calculateDailyStatus(DailyAttendance daily) {
        if (daily == null) return AttendanceStatus.NONE;
        return AttendanceRule.calculate(
                daily.getMorningStatus(),
                daily.getLunchStatus(),
                daily.getDinnerStatus()
        );
    }

    //- **API 개발 (관리자용)**
    //    - [ ]  **전체 출석 현황 조회 API** (`GET /api/v1/admin/attendances`): 날짜별, 과정별 전체 학생의 출석 상태 리스트 반환14.
    // 쿼리: "특정 날짜(date)에, 이 학생들(memberIds)의 출석부 다 가져와"
    // (Team D가 Enrollment에서 학생 ID 목록을 먼저 구해오면, 여기서 그 ID들로 조회함)
    // 날짜 -> DAILY_ATTENDANCE 반환. COURSE의 start_date와 end_date 사이에 있는 코스를 반환.
    public List<ResponseAttendanceByDateDTO> getDailyAttendance(LocalDate workDate, Long courseId) {

        // 1. DB에서 납작한 데이터 가져오기 (FlatResponse 리스트)
        List<ResponseAttendanceFlatDTO> flatList =
                enrollmentRepository.findIntegratedAttendanceFlat(workDate, courseId);

        // 2. 조립을 위한 Map 생성 (Key: 일일출석부 ID)
        Map<Long, ResponseAttendanceByDateDTO> resultMap = new HashMap<>();

        for (ResponseAttendanceFlatDTO flat : flatList) {
            Long id = flat.getDailyAttendanceId();

            // 2-1. Map에 해당 출석부(Key)가 없으면 바구니(DTO) 새로 만들기
            if (!resultMap.containsKey(id)) {
                ResponseAttendanceByDateDTO dto = ResponseAttendanceByDateDTO.builder()
                        .dailyAttendanceId(flat.getDailyAttendanceId())
                        .memberId(flat.getMemberId())
                        .memberName(flat.getMemberName())
                        .courseId(flat.getCourseId())
                        .courseName(flat.getCourseName())
                        .workDate(flat.getWorkDate())
                        .totalStatus(flat.getTotalStatus())
                        .detailedAttendanceList(new ArrayList<>()) // 리스트 초기화
                        .build();
                resultMap.put(id, dto);
            }

            // 2-2. 상세 출석 기록(detail)이 존재하면 리스트에 추가
            // (LEFT JOIN이라 null일 수도 있으므로 체크)
            if (flat.getDetailedAttendance() != null) {
                resultMap.get(id).addDetail(flat.getDetailedAttendance());
            }
        }

        // 3. Map의 값들(Value)만 리스트로 변환하여 반환
        return new ArrayList<>(resultMap.values());
    }


    //    - [ ]  **CSV/Excel 다운로드 API**: 현재 조회된 출석부 데이터를 파일로 변환하여 응답.
    // 파일 다운로드 로직
    public byte[] downloadAttendanceStats(String type, LocalDate date, Long courseId) {
        List<ResponseAttendanceFlatDTO> dataList;

        // 1. 데이터 조회 분기 처리 ( 전체 조회 or 부분 조회 )
        if(date == null && courseId == null) {
            // date와 courseId의 입력이 없으면 -> 전체 조회
            dataList = enrollmentRepository.findAllIntegratedAttendance();
        } else if (date != null && courseId != null) {
            // date와 courseId의 입력이 있으면 -> 부분 조회
            dataList = enrollmentRepository.integratedAttendance(date, courseId);
        } else {
            // 둘 중 하나만 들어온 경우 예외 처리
            throw new IllegalArgumentException("날짜와 과정 ID는 둘 다 입력하거나, 둘 다 없어야 합니다.");
        }

        // 2. 파일 생성 (CSV or Excel)
        try{
            if ("excel".equalsIgnoreCase(type)) {
                return ExcelUtil.createExcelFile(dataList);
            } else {
                return CsvUtil.createCsvFile(dataList);
            }
        } catch (Exception e) {
            throw new RuntimeException("파일 생성 중 오류가 발생했습니다.", e);
        }
    }

}
