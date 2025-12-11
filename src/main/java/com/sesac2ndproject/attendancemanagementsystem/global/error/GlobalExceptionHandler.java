package com.sesac2ndproject.attendancemanagementsystem.global.error;

import com.sesac2ndproject.attendancemanagementsystem.global.error.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 인증번호 오류 예외 처리
     */
    @ExceptionHandler(InvalidAuthNumberException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAuthNumberException(InvalidAuthNumberException e) {
        log.warn("InvalidAuthNumberException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.INVALID_AUTH_NUMBER, e.getMessage()));
    }

    /**
     * 출석 시간 만료 예외 처리
     */
    @ExceptionHandler(AttendanceTimeExpiredException.class)
    public ResponseEntity<ErrorResponse> handleAttendanceTimeExpiredException(AttendanceTimeExpiredException e) {
        log.warn("AttendanceTimeExpiredException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.ATTENDANCE_TIME_EXPIRED, e.getMessage()));
    }

    /**
     * 중복 출석 예외 처리
     */
    @ExceptionHandler(DuplicateAttendanceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateAttendanceException(DuplicateAttendanceException e) {
        log.warn("DuplicateAttendanceException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ErrorCode.DUPLICATE_ATTENDANCE, e.getMessage()));
    }

    /**
     * 출석 설정 미존재 예외 처리
     */
    @ExceptionHandler(AttendanceConfigNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAttendanceConfigNotFoundException(AttendanceConfigNotFoundException e) {
        log.warn("AttendanceConfigNotFoundException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ErrorCode.ATTENDANCE_CONFIG_NOT_FOUND, e.getMessage()));
    }

    /**
     * 출석 관련 기본 예외 처리
     */
    @ExceptionHandler(AttendanceException.class)
    public ResponseEntity<ErrorResponse> handleAttendanceException(AttendanceException e) {
        log.warn("AttendanceException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("A000", e.getMessage()));
    }

    /**
     * Validation 예외 처리 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("잘못된 입력값입니다.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    /**
     * BindException 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.warn("BindException: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("잘못된 입력값입니다.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    /**
     * IllegalArgumentException 예외 처리 (기존 코드 호환)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getMessage()));
    }

    /**
     * IllegalStateException 예외 처리 (기존 코드 호환)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        log.warn("IllegalStateException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ErrorCode.DUPLICATE_ATTENDANCE, e.getMessage()));
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    // 500 -> 409
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.warn("CustomException: {}", e.getErrorCode().getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }
}
