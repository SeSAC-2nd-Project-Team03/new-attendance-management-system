package com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.service;

import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.dto.AttendanceConfigCreateRequest;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.dto.AttendanceConfigResponse;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.dto.AttendanceConfigUpdateRequest;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.repository.AttendanceConfigRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.entity.AttendanceConfig;
import com.sesac2ndproject.attendancemanagementsystem.global.error.CustomException;
import com.sesac2ndproject.attendancemanagementsystem.global.error.ErrorCode;
import com.sesac2ndproject.attendancemanagementsystem.global.error.exception.AttendanceConfigNotFoundException;
import com.sesac2ndproject.attendancemanagementsystem.global.error.exception.DuplicateAttendanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceConfigService {

    private final AttendanceConfigRepository attendanceConfigRepository;

    // 출석 설정 생성하기
    @Transactional
    public Long createAttendanceConfig(AttendanceConfigCreateRequest request) {
        // 1. 중복 설정 검증
        if (attendanceConfigRepository.findByCourseIdAndTargetDateAndType(
                request.getCourseId(), request.getTargetDate(), request.getType()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_ATTENDANCE_CONFIG);
        }

        // 2. 엔티티 생성
        AttendanceConfig config = AttendanceConfig.create(
                request.getCourseId(),
                request.getType(),
                request.getAuthNumber(),
                request.getTargetDate(),
                request.getStandardTime(),
                request.getValidMinutes()
        );

        // 3. 저장
        AttendanceConfig savedConfig = attendanceConfigRepository.save(config);
        return savedConfig.getId();
    }

    // 단건 조회
    @Transactional(readOnly = true)
    public AttendanceConfigResponse getAttendanceConfig(Long id) {
        AttendanceConfig config = attendanceConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 설정을 찾을 수 없습니다. id=" + id));
        return AttendanceConfigResponse.from(config);
    }

    // 전체 조회
    @Transactional(readOnly = true)
    public List<AttendanceConfigResponse> getAttendanceConfigList(Long courseId) {
        return attendanceConfigRepository.findAllByCourseIdOrderByTargetDateDesc(courseId)
                .stream()
                .map(AttendanceConfigResponse::from)
                .collect(Collectors.toList());
    }

    // 시간 설정 변경
    @Transactional
    public AttendanceConfigResponse updateAttendanceConfig(Long id, AttendanceConfigUpdateRequest request) {
        AttendanceConfig config = attendanceConfigRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ATTENDANCE_CONFIG_NOT_FOUND));

        // 새로운 방식: startTime, standardTime, deadline 직접 설정
        if (request.getStartTime() != null || request.getDeadline() != null) {
            config.updateTimeConfig(request.getStartTime(), request.getStandardTime(), request.getDeadline());
        }
        // 기존 방식: standardTime, validMinutes 설정
        else if (request.getStandardTime() != null || request.getValidMinutes() != null) {
            LocalTime newStandardTime = (request.getStandardTime() != null)
                    ? request.getStandardTime()
                    : config.getStandardTime();

            Integer newValidMinutes = (request.getValidMinutes() != null)
                    ? request.getValidMinutes()
                    : config.getValidMinutes();

            config.updateTime(newStandardTime, newValidMinutes);
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return AttendanceConfigResponse.from(config);
    }

    // 인증번호 수정
    @Transactional
    public void updateAuthNumber(Long configId, AttendanceConfigUpdateRequest request) {
        // 1. 설정 조회
        AttendanceConfig config = attendanceConfigRepository.findById(configId)
                .orElseThrow(() -> new CustomException(ErrorCode.ATTENDANCE_CONFIG_NOT_FOUND)); // 또는 IllegalArgumentException

        // 2. 엔티티 메서드 호출 (상태 변경)
        config.updateAuthNumber(request.getAuthNumber());
    }

    // 설정 삭제
    public void deleteAttendanceConfig(Long id) {
        AttendanceConfig config = attendanceConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 설정을 찾을 수 없습니다. id=" + id));

        attendanceConfigRepository.delete(config);
    }
}
