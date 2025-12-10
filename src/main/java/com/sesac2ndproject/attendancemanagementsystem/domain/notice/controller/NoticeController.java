package com.sesac2ndproject.attendancemanagementsystem.domain.notice.controller;

import com.sesac2ndproject.attendancemanagementsystem.domain.notice.dto.NoticeResponseDTO;
import com.sesac2ndproject.attendancemanagementsystem.domain.notice.repository.NoticeRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "공지사항(notice) 조회", description = "Admin과 Member 둘 다 가능한 공지사항 조회 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notices")
public class NoticeController {
    private final NoticeService noticeService;
    // 만들어야 할 것(Member,Admin 용)
    // Controller는 Admin, Member 둘 다 접근 가능하다.
    // 공지사항 리스트는 공지사항id 기준 내림차순으로 한다.
    // Member가 할 수 있는건 조회뿐이다. 생성/수정/삭제 권한은 Admin만 가질 수 있다.
    //공지 목록 조회	GET	/api/v1/notices
    //공지 상세 조회	GET	/api/v1/notices/{id}
    //팝업 공지 조회	GET	/api/v1/notices/popups

    // 1. 공지사항 리스트 조회(Pageable 사용)
    @Operation(summary = "공지사항 목록 조회", description = "모든 공지사항을 페이징하여 조회. (기본: 최신순 10개), sort의 []대괄호는 지우세요. sort 안에 들어갈 수 있는 값 : id, title, writerName viewCounts, isPopup, createdAt, updatedAt")
    @GetMapping
    public ResponseEntity<Page<NoticeResponseDTO.ListDTO>> getAllNotices(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(noticeService.getAllNotices(pageable));
    }
    // 2. 공지사항 1개 상세 조회
    @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항의 상세 내용(본문 포함)을 조회하고 조회수를 1 증가.")
    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponseDTO.DetailDTO> getNotice (@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNotice(id));
    }
    // 3. 접속한 날짜가 'popupStartDate'와 'popupEndDate'의 범위안에 들 경우 홈페이지에 접속하면 자동으로 해당하는 공지사항을 찾아서 보여줌
    @Operation(summary = "팝업 공지 조회", description = "현재 날짜를 기준으로 메인 페이지에 띄워야 할 팝업 공지사항 목록을 조회.")
    @GetMapping("/popups")
    public ResponseEntity<List<NoticeResponseDTO.DetailDTO>> getPopups() {
        return ResponseEntity.ok(noticeService.getPopupNotices());
    }

}
