package com.sesac2ndproject.attendancemanagementsystem.domain.notice.entity;

import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.global.entity.BaseTimeEntity;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Notice extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;       // 공지사항 제목
    private String content;     // 공지사항 내용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member writer;
    private Long viewCount;     // 조회수
    private Boolean isPopup;    // 메인 페이지에 팝업으로 띄울 중요한 공지인지 여부 (페이지에 들어가면 팝업으로 나오고 '오늘 하루 그만보기' 있는 창 같은 것)
    private LocalDateTime popupStartDate;   // 메인 페이지에 팝업으로 띄울 시작 날짜
    private LocalDateTime popupEndDate;     // 팝업 종료 날짜

    // ViewCount를 +1 하는 로직
    public void increaseViewCount() {
        this.viewCount++;
    }

    // Setter 대신 사용하는 메서드
    public void update(String title, String content, Boolean isPopup, LocalDateTime start, LocalDateTime end) {
        this.title = title;
        this.content = content;
        this.isPopup = isPopup;
        this.popupStartDate = start;
        this.popupEndDate = end;
    }

}
