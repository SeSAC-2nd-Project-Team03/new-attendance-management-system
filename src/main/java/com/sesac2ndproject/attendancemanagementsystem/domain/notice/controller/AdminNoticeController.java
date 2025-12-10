package com.sesac2ndproject.attendancemanagementsystem.domain.notice.controller;

import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.domain.notice.dto.NoticeRequestDTO;
import com.sesac2ndproject.attendancemanagementsystem.domain.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공지사항(notice) 생성/수정/삭제", description = "Admin만 가능 한 공지사항 CUD 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/notices")
public class AdminNoticeController {

    private final NoticeService noticeService;

    // 본 Controller는 Admin만 접근 가능하다.
    // 공지 등록	POST	/api/v1/admin/notices
    // 공지 수정	PUT	    /api/v1/admin/notices/{id}
    // 공지 삭제	DELETE	/api/v1/admin/notices/{id}

    // 4. 공지사항 생성(POST)
    @Operation(summary = "공지사항 작성", description = "관리자가 새로운 공지사항을 등록.")
    @PostMapping
    public ResponseEntity<Long> createNotice(@RequestBody NoticeRequestDTO noticeRequestDTO,
                                             @Parameter(hidden = true)  @AuthenticationPrincipal Member admin /*현재 로그인 한 관리자 정보 가져옴*/) {
        // 테스트를 대비하여 기본값은 1L로 설정
        Long adminId = (admin != null) ? admin.getId() : 1L;
//        Long adminId = 1L;
        Long createdId = noticeService.createNotice(noticeRequestDTO, adminId);
        return ResponseEntity.ok(createdId);
    }
    // 5. 공지사항 수정(PUT)
    @Operation(summary = "공지사항 수정", description = "기존 공지사항의 제목(title), 내용(content), 팝업(isPopup,start,end) 설정을 수정.")
    @PutMapping("/{id}")
    public ResponseEntity<Long> updateNotice(@PathVariable Long id, @RequestBody NoticeRequestDTO requestDTO){
        Long updatedId = noticeService.updateNotice(id, requestDTO);
        return ResponseEntity.ok(updatedId);
    }
    // 6. 공지사항 삭제(DELETE)
    @Operation(summary = "공지사항 삭제", description = "공지사항을 영구적으로 삭제.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok().build();
    }
}
