package com.sesac2ndproject.attendancemanagementsystem.domain.course.controller;

import com.sesac2ndproject.attendancemanagementsystem.domain.course.entity.Enrollment;
import com.sesac2ndproject.attendancemanagementsystem.domain.course.service.CourseService;
import com.sesac2ndproject.attendancemanagementsystem.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1") // 기본 prefix만 두고, 메서드에서 세부 경로를 다중 매핑
@RequiredArgsConstructor
@Tag(name = "Attendance (Query)", description = "출석 현황 조회 API")
public class CourseController {

    private final CourseService courseService;


    // - **조회 로직 (Query)**
    //    - [ ]  **과정별 수강생 조회:** `Enrollment`를 통해 특정 과정(Course)을 듣는 `memberId` 목록 추출.
    @GetMapping({"/attendances/admin/enrollment", "/admin/enrollment"})
    @Operation(summary ="과정별 수강생 조회", description = "courseId를 입력하여 Enrollment(강좌의 수강생 목록에서 해당하는 것들을 반환")
    public ResponseEntity<ApiResponse<List<Enrollment>>> findMemberIdsByCourseId(@RequestParam Long courseId) {

        List<Enrollment> memberList = courseService.findMemberIdsByCourseId(courseId);

        return ResponseEntity.ok(ApiResponse.success(memberList));
    }
}
