package com.sesac2ndproject.attendancemanagementsystem.domain.leave.repository;

import com.sesac2ndproject.attendancemanagementsystem.domain.leave.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByMember_LoginIdOrderByCreatedAtDesc(String loginId);
    
    // 관리자용: 모든 휴가 신청 조회 (최신순)
    List<LeaveRequest> findAllByOrderByCreatedAtDesc();
    
    // 관리자용: PENDING 상태의 신청만 조회
    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(com.sesac2ndproject.attendancemanagementsystem.global.type.LeaveStatus status);
}