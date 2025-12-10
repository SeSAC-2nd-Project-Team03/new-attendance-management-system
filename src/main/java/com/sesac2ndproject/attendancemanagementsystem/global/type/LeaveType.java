package com.sesac2ndproject.attendancemanagementsystem.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeaveType {
    EARLY_LEAVE("조퇴"),
    ABSENCE("결석/공가"),
    SICK_LEAVE("병가"),
    SICK("병가"),  // 프론트엔드 호환성
    PERSONAL("개인 사유"),
    OFFICIAL("공가"),  // 프론트엔드 호환성
    OTHER("기타");  // 프론트엔드 호환성

    private final String description;

}
