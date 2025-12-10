package com.sesac2ndproject.attendancemanagementsystem.domain.course.service;


import com.sesac2ndproject.attendancemanagementsystem.domain.course.entity.Enrollment;
import com.sesac2ndproject.attendancemanagementsystem.domain.course.repository.EnrollmentRepository;
import com.sesac2ndproject.attendancemanagementsystem.global.type.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final EnrollmentRepository enrollmentRepository;


    public List<Enrollment> findMemberIdsByCourseId(Long courseId) {
        // ACTIVE 상태의 수강생만 조회
        List<Enrollment> foundEnrollment = enrollmentRepository.findMemberIdByCourseIdAndStatus(
                courseId, EnrollmentStatus.ACTIVE);
        return foundEnrollment;
    }

}
