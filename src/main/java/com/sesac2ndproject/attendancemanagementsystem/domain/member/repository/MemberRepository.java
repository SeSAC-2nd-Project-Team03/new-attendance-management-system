package com.sesac2ndproject.attendancemanagementsystem.domain.member.repository;

import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.global.type.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 로그인 id로 회원 정보 찾기
    Optional<Member> findByLoginId(String loginId);

    // 중복 id 방지
    boolean existsByLoginId(String loginId);
    
    // 역할로 회원 조회
    List<Member> findByRole(RoleType role);
}
