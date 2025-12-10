package com.sesac2ndproject.attendancemanagementsystem.domain.notice.dto;

import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeRequestDTO {
    private String title;       // 공지사항 제목
    private String content;     // 공지사항 내용
    private Boolean isPopup;    // 메인 페이지에 팝업으로 띄울 중요한 공지인지 여부 (페이지에 들어가면 팝업으로 나오고 '오늘 하루 그만보기' 있는 창 같은 것)
    private LocalDateTime popupStartDate;   // 메인 페이지에 팝업으로 띄울 시작 날짜
    private LocalDateTime popupEndDate;     // 팝업 종료 날짜
}
