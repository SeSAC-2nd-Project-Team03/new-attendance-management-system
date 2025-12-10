package com.sesac2ndproject.attendancemanagementsystem.domain.notice.service;

import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.repository.MemberRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.notice.dto.NoticeRequestDTO;
import com.sesac2ndproject.attendancemanagementsystem.domain.notice.dto.NoticeResponseDTO;
import com.sesac2ndproject.attendancemanagementsystem.domain.notice.entity.Notice;
import com.sesac2ndproject.attendancemanagementsystem.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    // 1. 공지사항 목록을 조회하는 로직(Pageable 사용, 본문은 제외)
    // (페이징 사용 + DTO 변환)
    public Page<NoticeResponseDTO.ListDTO> getAllNotices(Pageable pageable) {
        Page<Notice> noticePage = noticeRepository.findAll(pageable);

        // Entity -> ListDTO로 변환(본문 제외)
        return noticePage.map(notice -> NoticeResponseDTO.ListDTO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .writerName(notice.getWriter().getName())
                .viewCount(notice.getViewCount())
                .isPopup(notice.getIsPopup())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build());
    }
    // 2. 공지사항을 Id로 조회 + 조회수 1 증가 로직
    @Transactional  // DirtyCheck로 조회수 변경을 위해 readOnly 설정 해제
    public NoticeResponseDTO.DetailDTO getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항이 존재하지 않습니다."));
        // 조회수 1 증가
        notice.increaseViewCount();

        // Entity -> DetailDTO로 변환
        return toDetailDTO(notice);
    }

    // 3. 접속한 날짜가 'popupStartDate'와 'popupEndDate'의 범위안에 들 경우 홈페이지에 접속하면 자동으로 해당하는 공지사항을 찾아서 보여줌.
    public List<NoticeResponseDTO.DetailDTO> getPopupNotices() {
        LocalDateTime now = LocalDateTime.now();
        List<Notice> popups = noticeRepository.findActivePopups(now);

        return popups.stream()
                .map(this::toDetailDTO)
                .collect(Collectors.toList());
    }

    // 4. 공지사항 생성(POST)
    @Transactional
    public Long createNotice(NoticeRequestDTO noticeRequestDTO, Long adminId) {
        // 로그인 된 ID로 해당 멤버 정보를 찾아옴.
        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다. adminId=" + adminId));
        // 받아온 RequestDTO를 엔티티로 조립
        Notice notice = Notice.builder()
                .title(noticeRequestDTO.getTitle())
                .content(noticeRequestDTO.getContent())
                .writer(admin)
                .viewCount(0L)
                .isPopup(noticeRequestDTO.getIsPopup())
                .popupStartDate(noticeRequestDTO.getPopupStartDate())
                .popupEndDate(noticeRequestDTO.getPopupEndDate())
                .build();
        // 조립한 엔티티를 DB에 저장
        return noticeRepository.save(notice).getId();
    }
    // 5. 공지사항 수정(PUT)
    @Transactional
    public Long updateNotice(Long id, NoticeRequestDTO noticeRequestDTO) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항이 존재하지 않습니다."));
        // 엔티티 수정 메서드 호출
        notice.update(
                noticeRequestDTO.getTitle(),
                noticeRequestDTO.getContent(),
                noticeRequestDTO.getIsPopup(),
                noticeRequestDTO.getPopupStartDate(),
                noticeRequestDTO.getPopupEndDate());

        return id;
    }
    // 6. 공지사항 삭제(DELETE)
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항이 존재하지 않습니다."));

        noticeRepository.delete(notice);
    }

    // 공통 메서드. Entity -> DetailDTO 변환기
    private NoticeResponseDTO.DetailDTO toDetailDTO (Notice notice) {
        return NoticeResponseDTO.DetailDTO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .writerName(notice.getWriter().getName())
                .viewCount(notice.getViewCount())
                .isPopup(notice.getIsPopup())
                .popupStartDate(notice.getPopupStartDate())
                .popupEndDate(notice.getPopupEndDate())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}
