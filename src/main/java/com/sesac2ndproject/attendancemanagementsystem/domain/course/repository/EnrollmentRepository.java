package com.sesac2ndproject.attendancemanagementsystem.domain.course.repository;

import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.dto.ResponseAttendanceFlatDTO;
import com.sesac2ndproject.attendancemanagementsystem.domain.course.entity.Enrollment;
import com.sesac2ndproject.attendancemanagementsystem.global.type.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Team D 쿼리 1 : 해당하는 courseId를 가진 Enrollment(Member 포함) 찾아오기
    // ACTIVE 상태의 수강생만 조회하도록 명시적 쿼리 추가
    @Query("SELECT e FROM Enrollment e " +
           "JOIN FETCH e.member m " +
           "JOIN FETCH e.course c " +
           "WHERE e.course.id = :courseId AND e.status = :status " +
           "ORDER BY m.name ASC")
    List<Enrollment> findMemberIdByCourseIdAndStatus(
            @Param("courseId") Long courseId,
            @Param("status") EnrollmentStatus status
    );
    
    // 기존 메서드 유지 (하위 호환성)
    @Query("SELECT e FROM Enrollment e " +
           "JOIN FETCH e.member m " +
           "JOIN FETCH e.course c " +
           "WHERE e.course.id = :courseId " +
           "ORDER BY m.name ASC")
    List<Enrollment> findMemberIdByCourseId(@Param("courseId") Long courseId);

    /**
     * Team D 쿼리 2 : 통합 출석부 조회 (날짜 + 과정ID)
     * [수정됨] ResponseByDateAndCourseIdDTO 삭제 -> ResponseAttendanceByDateDTO.FlatResponse 사용
     * 참고: JPQL에서 Inner Class 생성자를 호출할 때는 '$' 기호를 사용합니다.
     */
    @Query("SELECT new com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.dto.ResponseAttendanceFlatDTO(" +
            "   da.id, " +
            "   da.memberId, " +
            "   mem.name, " +
            "   enr.course.id, " +
            "   cor.courseName, " +
            "   da.date, " +
            "   da.status, " +
            "   dea" +
            ") " +
            " FROM DailyAttendance AS da" +
            " JOIN Enrollment AS enr ON da.memberId = enr.member.id" +
            " JOIN Member mem ON da.memberId = mem.id" +
            " JOIN Course cor ON enr.course.id = cor.id" +
            " LEFT JOIN DetailedAttendance AS dea ON da.id = dea.dailyAttendanceId" +
            " WHERE da.date = :workDate AND enr.course.id = :courseId")
    List<ResponseAttendanceFlatDTO> integratedAttendance(@Param("workDate") LocalDate workDate, @Param("courseId") Long courseId);

    /**
     * Team D 쿼리 3 : (쿼리 2와 기능 중복, 로직 통합 권장)
     * 기존에 잘 작성되어 있던 FlatResponse 반환 메서드입니다.
     */
    @Query("SELECT new com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.dto.ResponseAttendanceFlatDTO(" +
            "   da.id, " +
            "   da.memberId, " +
            "   mem.name, " +
            "   cor.id, " +
            "   cor.courseName, " +
            "   da.date, " +
            "   da.status, " +
            "   dea" +
            ") " +
            " FROM DailyAttendance AS da" +
            " JOIN Enrollment AS er ON da.memberId = er.member.id" +
            " JOIN Member AS mem ON da.memberId = mem.id" +
            " JOIN Course AS cor ON da.courseId = cor.id" +
            " LEFT JOIN DetailedAttendance AS dea ON da.id = dea.dailyAttendanceId" +
            " WHERE da.date = :workDate AND er.course.id = :courseId")
    List<ResponseAttendanceFlatDTO> findIntegratedAttendanceFlat(@Param("workDate") LocalDate workDate, @Param("courseId") Long courseId);

    /**
     * Team D 쿼리 4 : 전체 통합 출석부 조회 (다운로드용)
     * [수정됨] ResponseByDateAndCourseIdDTO 삭제 -> ResponseAttendanceByDateDTO.FlatResponse 사용
     */
    @Query("SELECT new com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.dto.ResponseAttendanceFlatDTO(" +
            "   da.id, " +
            "   da.memberId, " +
            "   mem.name, " +
            "   enr.course.id, " +
            "   cor.courseName," +
            "   da.date, " +
            "   da.status, " +
            "   dea" +
            ") " +
            " FROM DailyAttendance da" +
            " JOIN Enrollment enr ON da.memberId = enr.member.id" +
            " JOIN Member mem ON da.memberId = mem.id" +
            " JOIN Course cor ON enr.course.id = cor.id" +
            " LEFT JOIN DetailedAttendance dea ON da.id = dea.dailyAttendanceId" +
            " ORDER BY da.date DESC, da.memberId ASC")
    List<ResponseAttendanceFlatDTO> findAllIntegratedAttendance();

    // 특정 상태의 수강생 ID 목록 조회
    @Query("SELECT e.member.id FROM Enrollment e WHERE e.course.id = :courseId AND e.status = :status")
    List<Long> findMemberIdsByCourseIdAndStatus(
            @Param("courseId") Long courseId,
            @Param("status") EnrollmentStatus status
    );
}