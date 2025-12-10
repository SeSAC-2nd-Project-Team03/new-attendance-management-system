package com.sesac2ndproject.attendancemanagementsystem.domain.member.controller;

import com.sesac2ndproject.attendancemanagementsystem.domain.member.dto.MemberCreateRequest;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.dto.MemberResponse;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.dto.MemberUpdateRequest;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Member (Admin)", description = "관리자용 회원 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/members")
public class AdminMemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 생성", description = "관리자가 새로운 회원(관리자/회원)을 생성합니다")
    @PostMapping
    public ResponseEntity<Void> createMember(@Valid @RequestBody MemberCreateRequest request) {
        Long memberId = memberService.createMember(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/members" + memberId)).build();
    }

    @Operation(summary = "전체 회원 조회", description = "시스템에 등록된 모든 회원의 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        List<MemberResponse> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    @Operation(summary = "회원 정보 수정", description = "관리자가 회원의 정보를 수정합니다")
    @PatchMapping("/{loginId}")
    public ResponseEntity<Void> updateMemberInfo(
            @PathVariable String loginId,
            @RequestBody MemberUpdateRequest request
    ) {
        memberService.updateMemberByAdmin(loginId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 삭제", description = "관리자가 회원을 강제 탈퇴시킵니다.")
    @DeleteMapping("/{loginId}")
    public ResponseEntity<Void> deleteMember(@PathVariable String loginId) {
        memberService.deleteMember(loginId); // Service에도 메서드 추가 필요
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "개인 회원 조회", description = "관리자가 회원 개인을 조회합니다")
    @GetMapping("/{loginId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable String loginId) {
        MemberResponse member = memberService.getMember(loginId);
        return ResponseEntity.ok(member);
    }
}
