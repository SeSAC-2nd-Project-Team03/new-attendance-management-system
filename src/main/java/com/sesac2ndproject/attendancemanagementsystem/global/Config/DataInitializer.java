package com.sesac2ndproject.attendancemanagementsystem.global.Config;

import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.entity.AttendanceConfig;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.common.entity.DailyAttendance;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.common.entity.DetailedAttendance;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.configure.repository.AttendanceConfigRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.common.repository.DailyAttendanceRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.common.repository.DetailedAttendanceRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.course.entity.Course;
import com.sesac2ndproject.attendancemanagementsystem.domain.course.entity.Enrollment;
import com.sesac2ndproject.attendancemanagementsystem.domain.course.repository.CourseRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.course.repository.EnrollmentRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.repository.MemberRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.notice.entity.Notice;
import com.sesac2ndproject.attendancemanagementsystem.domain.notice.repository.NoticeRepository;
import com.sesac2ndproject.attendancemanagementsystem.global.type.AttendanceStatus;
import com.sesac2ndproject.attendancemanagementsystem.global.type.AttendanceType;
import com.sesac2ndproject.attendancemanagementsystem.global.type.EnrollmentStatus;
import com.sesac2ndproject.attendancemanagementsystem.global.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final DetailedAttendanceRepository detailedAttendanceRepository;
    private final AttendanceConfigRepository attendanceConfigRepository;
    private final DailyAttendanceRepository dailyAttendanceRepository;
    private final NoticeRepository noticeRepository;

    @Override
    public void run(String... args) throws Exception {

        // 1. 관리자 계정 생성
        if (memberRepository.findByLoginId("admin").isEmpty()) {
            Member admin = Member.builder()
                    .loginId("admin")
                    .password(passwordEncoder.encode("1234"))
                    .name("관리자")
                    .role(RoleType.ADMIN)
                    .build();
            memberRepository.save(admin);
            System.out.println("관리자 계정 생성 완료");
        }

        // 2. 학생 계정 생성 (student1, student2, student3)
        createStudentIfAbsent("student1", "김철수", "010-1111-2222");
        createStudentIfAbsent("student2", "이영희", "010-3333-4444");
        createStudentIfAbsent("student3", "박조퇴", "010-5555-6666");

        // 추가 학생 더미 데이터 (10명)
        createStudentIfAbsent("student4", "최민수", "010-7777-8888");
        createStudentIfAbsent("student5", "정수진", "010-9999-0000");
        createStudentIfAbsent("student6", "한지훈", "010-1111-3333");
        createStudentIfAbsent("student7", "윤서연", "010-2222-4444");
        createStudentIfAbsent("student8", "오동현", "010-3333-5555");
        createStudentIfAbsent("student9", "강미영", "010-4444-6666");
        createStudentIfAbsent("student10", "임태준", "010-5555-7777");
        createStudentIfAbsent("student11", "배혜진", "010-6666-8888");
        createStudentIfAbsent("student12", "신우진", "010-7777-9999");
        createStudentIfAbsent("student13", "조은서", "010-8888-1111");
        
        // 3. 과정(Course) 생성 및 가져오기
        Course javaCourse;
        if (courseRepository.count() == 0) {
            javaCourse = Course.builder()
                    .courseName("자바 백엔드 개발자 양성과정")
                    .description("Spring Boot 중심의 백엔드 과정")
                    .startDate(LocalDate.of(2025, 11, 1))
                    .endDate(LocalDate.of(2026, 5, 1))
                    .build();
            courseRepository.save(javaCourse);
            System.out.println("과정(Course) 데이터 생성 완료");
        } else {
            javaCourse = courseRepository.findAll().get(0);
        }

        // 4. 수강신청 (모든 학생에 대해 진행)
        if (enrollmentRepository.count() == 0) {
            for (int i = 1; i <= 13; i++) {
                String loginId = "student" + i;
                Member student = memberRepository.findByLoginId(loginId).orElseThrow();
                enrollmentRepository.save(createEnrollment(student, javaCourse));
            }
            System.out.println("수강신청 데이터 초기화 완료 (13명)");
        }

        // ============================================
        // 공통 변수 선언 (admin, today)
        // ============================================
        Member admin = memberRepository.findByLoginId("admin").orElseThrow();
        LocalDate today = LocalDate.now();

        // 5. 과거 데이터 대량 생성 (어제부터 7일 전까지, 모든 학생 대상)
        // 목표: 13명 * 7일 = 91개의 DailyAttendance 데이터 만들기
        // =====================================================================
        // 항상 과거 데이터 생성 시도 (중복은 createPastDataRandom 내부에서 체크)
        System.out.println("🔄 [테스트용] 과거 7일치 출석 데이터 생성 시작 (모든 학생 대상)...");
        
        // 모든 학생 조회 (student1 ~ student13)
        List<Member> allStudents = new java.util.ArrayList<>();
        for (int i = 1; i <= 13; i++) {
            String loginId = "student" + i;
            memberRepository.findByLoginId(loginId).ifPresent(allStudents::add);
        }

        LocalDate yesterday = LocalDate.now().minusDays(1);
        int createdDays = 0;
        int skippedWeekends = 0;
        int totalCreated = 0;

        // 어제부터 과거 7일간 반복 (주말 제외)
        for (int i = 0; i < 14; i++) { // 주말 제외를 위해 더 넓은 범위 탐색
            LocalDate targetDate = yesterday.minusDays(i);
            
            // 주말 제외 (토요일, 일요일)
            java.time.DayOfWeek dayOfWeek = targetDate.getDayOfWeek();
            if (dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                skippedWeekends++;
                continue;
            }
            
            // 최대 7일치만 생성
            if (createdDays >= 7) {
                break;
            }

            // 과거 날짜에 대한 AttendanceConfig 생성
            createAttendanceConfigForDate(javaCourse, admin, targetDate);
            
            // 각 학생의 출석 데이터 생성 (중복 체크 포함)
            for (Member student : allStudents) {
                boolean created = createPastDataRandomIfNotExists(student, javaCourse, targetDate);
                if (created) totalCreated++;
            }
            createdDays++;
        }
        System.out.println("✅ [테스트용] 과거 데이터 생성 완료 (신규 생성: " + totalCreated + "개, 주말 " + skippedWeekends + "일 제외)");

        // ============================================
        // ✅ Person 1: 출석 설정(AttendanceConfig) 생성 (오늘 날짜)
        // ============================================
        // 오늘 날짜의 출석 설정이 없으면 생성
        boolean todayConfigExists = attendanceConfigRepository.findByCourseIdAndTargetDateAndType(
                javaCourse.getId(), today, AttendanceType.MORNING).isPresent();
        
        if (!todayConfigExists) {
            createAttendanceConfigForDate(javaCourse, admin, today);
            System.out.println("✅ [Person 1] 오늘 날짜 출석 설정(AttendanceConfig) 생성 완료");
        }

        // ============================================
        // 5. 오늘 출석 상세 기록 (모든 학생 대상)
        // ============================================
        if (detailedAttendanceRepository.count() == 0) {
            java.util.Random random = new java.util.Random();
            
            System.out.println("🔄 [테스트용] 오늘 출석 데이터 생성 시작 (모든 학생 대상)...");
            
            for (int i = 1; i <= 13; i++) {
                String loginId = "student" + i;
                Member student = memberRepository.findByLoginId(loginId).orElse(null);
                if (student == null) continue;
                
                // 랜덤 출석 패턴 (70% 출석, 15% 지각, 10% 조퇴, 5% 결석)
                int pattern = random.nextInt(100);
                String ipSuffix = String.valueOf(100 + i);
                
                if (pattern < 70) {
                    // 🔵 정상 출석: 아침(O) + 점심(O) + 저녁(O)
                    createTodayAttendance(student, javaCourse, today, "192.168.1." + ipSuffix,
                            true, true, true, false, false, false);
                    System.out.println("🔵 [" + loginId + "] 정상 출석: 아침(O) + 점심(O) + 저녁(O)");
                } else if (pattern < 85) {
                    // 🟡 지각: 아침(늦음) + 점심(O) + 저녁(O)
                    createTodayAttendance(student, javaCourse, today, "192.168.1." + ipSuffix,
                            true, true, true, true, false, false);
                    System.out.println("🟡 [" + loginId + "] 지각: 아침(늦음) + 점심(O) + 저녁(O)");
                } else if (pattern < 95) {
                    // 🟠 조퇴: 아침(O) + 점심(O) + 저녁(X)
                    createTodayAttendance(student, javaCourse, today, "192.168.1." + ipSuffix,
                            true, true, false, false, false, false);
                    System.out.println("🟠 [" + loginId + "] 조퇴: 아침(O) + 점심(O) + 저녁(X)");
                } else {
                    // 🔴 결석: 모두 결석
                    System.out.println("🔴 [" + loginId + "] 결석: 아침(X) + 점심(X) + 저녁(X)");
                }
            }
            System.out.println("✅ [테스트용] 오늘 출석 데이터 생성 완료");
        }
        // 6. 공지사항(notice) 데이터 생성 (총 30개)
        if (noticeRepository.count() == 0) {
            System.out.println("🔄 [테스트용] 공지사항 데이터 생성 시작...");
            LocalDateTime now = LocalDateTime.now();

            for (int i = 1; i <= 30; i++) {
                boolean isPopup = false;
                LocalDateTime startDate = null;
                LocalDateTime endDate = null;
                String titlePrefix = "[공지] ";

                // 25번 ~ 30번은 팝업 공지로 설정
                if (i > 20) {
                    isPopup = true;
                    if (i <= 25) {
                        // 21~25번: [진행중] 오늘 날짜 포함 (어제 ~ 내일) -> 화면에 보여야 함
                        titlePrefix = "[팝업/중요] ";
                        startDate = now.minusDays(1);
                        endDate = now.plusDays(1);
                    } else {
                        // 26~30번: [만료됨] 지난 날짜 (지난달 ~ 어제) -> 화면에 안 보여야 함
                        titlePrefix = "[팝업/만료] ";
                        startDate = now.minusMonths(1);
                        endDate = now.minusDays(1);
                    }
                }

                Notice notice = Notice.builder()
                        .title(titlePrefix + "테스트 공지사항 " + i + "입니다.")
                        .content("안녕하세요. 테스트 공지사항 내용입니다. <br> 번호: " + i + "<br> 화이팅하세요!")
                        .writer(admin) // 관리자 작성
                        .viewCount((long) (Math.random() * 10001))
                        .isPopup(isPopup)
                        .popupStartDate(startDate)
                        .popupEndDate(endDate)
                        .build();

                noticeRepository.save(notice);
            }
            System.out.println("✅ [테스트용] 공지사항 30개 생성 완료 (팝업 10개 포함)");
        }

    }
    /* [ 헬퍼 메서드 ] */
    
    // 랜덤 출석 상태 생성 메서드 (중복 체크 포함, 생성 여부 반환)
    private boolean createPastDataRandomIfNotExists(Member student, Course course, LocalDate date) {
        // 이미 존재하는 DailyAttendance가 있는지 확인
        java.util.Optional<DailyAttendance> existingDaily = dailyAttendanceRepository
                .findByMemberIdAndCourseIdAndDate(student.getId(), course.getId(), date);
        
        if (existingDaily.isPresent()) {
            // 이미 존재하면 건너뛰기
            return false;
        }
        
        createPastDataRandomInternal(student, course, date);
        return true;
    }
    
    // 랜덤 출석 상태 생성 메서드 (내부 구현)
    private void createPastDataRandomInternal(Member student, Course course, LocalDate date) {
        java.util.Random random = new java.util.Random(student.getId().hashCode() + date.hashCode());
        
        // 랜덤 출석 상태 결정 (55% 출석, 15% 지각, 15% 조퇴, 15% 결석)
        int randomValue = random.nextInt(100);
        AttendanceStatus status;
        AttendanceStatus morningStatus;
        AttendanceStatus lunchStatus;
        AttendanceStatus dinnerStatus;
        
        if (randomValue < 55) {
            // 55% - 정상 출석
            status = AttendanceStatus.PRESENT;
            morningStatus = AttendanceStatus.PRESENT;
            lunchStatus = AttendanceStatus.PRESENT;
            dinnerStatus = AttendanceStatus.PRESENT;
        } else if (randomValue < 70) {
            // 15% - 지각 (아침만 늦음)
            status = AttendanceStatus.LATE;
            morningStatus = AttendanceStatus.LATE;
            lunchStatus = AttendanceStatus.PRESENT;
            dinnerStatus = AttendanceStatus.PRESENT;
        } else if (randomValue < 85) {
            // 15% - 조퇴 (저녁 결석)
            status = AttendanceStatus.LEAVE;
            morningStatus = AttendanceStatus.PRESENT;
            lunchStatus = AttendanceStatus.PRESENT;
            dinnerStatus = AttendanceStatus.ABSENT;
        } else {
            // 15% - 결석
            status = AttendanceStatus.ABSENT;
            morningStatus = AttendanceStatus.ABSENT;
            lunchStatus = AttendanceStatus.ABSENT;
            dinnerStatus = AttendanceStatus.ABSENT;
        }

        // 1. DailyAttendance 저장
        DailyAttendance daily = DailyAttendance.builder()
                .memberId(student.getId())
                .courseId(course.getId())
                .date(date)
                .status(status)
                .morningStatus(morningStatus)
                .lunchStatus(lunchStatus)
                .dinnerStatus(dinnerStatus)
                .build();
        
        // 전체 상태 업데이트 (시간대별 상태 기반으로 재계산)
        daily.recalculateOverallStatus();

        DailyAttendance savedDaily = dailyAttendanceRepository.save(daily);

        // 2. DetailedAttendance 저장 (결석이 아닌 경우에만)
        if (status != AttendanceStatus.ABSENT) {
            // 아침 출석
            if (morningStatus != AttendanceStatus.ABSENT) {
                LocalTime morningTime = (morningStatus == AttendanceStatus.LATE) 
                    ? LocalTime.of(9, 30) : LocalTime.of(8, 50 + random.nextInt(10));
                createDetail(student, course, savedDaily.getId(), AttendanceType.MORNING, date,
                        morningTime, morningStatus == AttendanceStatus.PRESENT);
            }
            // 점심 출석
            if (lunchStatus != AttendanceStatus.ABSENT) {
                createDetail(student, course, savedDaily.getId(), AttendanceType.LUNCH, date,
                        LocalTime.of(12, random.nextInt(30)), true);  // 12:00 ~ 12:29
            }
            // 저녁 출석
            if (dinnerStatus != AttendanceStatus.ABSENT) {
                createDetail(student, course, savedDaily.getId(), AttendanceType.DINNER, date,
                        LocalTime.of(17, 50 + random.nextInt(10)), true);
            }
        }
    }
    
    // 랜덤 출석 상태 생성 메서드 (모든 학생 대상) - 레거시
    private void createPastDataRandom(Member student, Course course, LocalDate date) {
        // 이미 존재하는 DailyAttendance가 있는지 확인
        java.util.Optional<DailyAttendance> existingDaily = dailyAttendanceRepository
                .findByMemberIdAndCourseIdAndDate(student.getId(), course.getId(), date);
        
        if (existingDaily.isPresent()) {
            // 이미 존재하면 건너뛰기
            return;
        }
        
        java.util.Random random = new java.util.Random(student.getId().hashCode() + date.hashCode());
        
        // 랜덤 출석 상태 결정 (70% 출석, 15% 지각, 10% 조퇴, 5% 결석)
        int randomValue = random.nextInt(100);
        AttendanceStatus status;
        AttendanceStatus morningStatus;
        AttendanceStatus lunchStatus;
        AttendanceStatus dinnerStatus;
        
        if (randomValue < 55) {
            // 55% - 정상 출석
            status = AttendanceStatus.PRESENT;
            morningStatus = AttendanceStatus.PRESENT;
            lunchStatus = AttendanceStatus.PRESENT;
            dinnerStatus = AttendanceStatus.PRESENT;
        } else if (randomValue < 70) {
            // 15% - 지각 (아침만 늦음)
            status = AttendanceStatus.LATE;
            morningStatus = AttendanceStatus.LATE;
            lunchStatus = AttendanceStatus.PRESENT;
            dinnerStatus = AttendanceStatus.PRESENT;
        } else if (randomValue < 85) {
            // 15% - 조퇴 (저녁 결석)
            status = AttendanceStatus.LEAVE;
            morningStatus = AttendanceStatus.PRESENT;
            lunchStatus = AttendanceStatus.PRESENT;
            dinnerStatus = AttendanceStatus.ABSENT;
        } else {
            // 15% - 결석
            status = AttendanceStatus.ABSENT;
            morningStatus = AttendanceStatus.ABSENT;
            lunchStatus = AttendanceStatus.ABSENT;
            dinnerStatus = AttendanceStatus.ABSENT;
        }

        // 1. DailyAttendance 저장
        DailyAttendance daily = DailyAttendance.builder()
                .memberId(student.getId())
                .courseId(course.getId())
                .date(date)
                .status(status)
                .morningStatus(morningStatus)
                .lunchStatus(lunchStatus)
                .dinnerStatus(dinnerStatus)
                .build();
        
        // 전체 상태 업데이트 (시간대별 상태 기반으로 재계산)
        daily.recalculateOverallStatus();

        DailyAttendance savedDaily = dailyAttendanceRepository.save(daily);

        // 2. DetailedAttendance 저장 (결석이 아닌 경우에만)
        if (status != AttendanceStatus.ABSENT) {
            // 아침 출석
            if (morningStatus != AttendanceStatus.ABSENT) {
                LocalTime morningTime = (morningStatus == AttendanceStatus.LATE) 
                    ? LocalTime.of(9, 30) : LocalTime.of(8, 50 + random.nextInt(10));
                createDetail(student, course, savedDaily.getId(), AttendanceType.MORNING, date,
                        morningTime, morningStatus == AttendanceStatus.PRESENT);
            }
            // 점심 출석
            if (lunchStatus != AttendanceStatus.ABSENT) {
                createDetail(student, course, savedDaily.getId(), AttendanceType.LUNCH, date,
                        LocalTime.of(12, random.nextInt(30)), true);  // 12:00 ~ 12:29
            }
            // 저녁 출석
            if (dinnerStatus != AttendanceStatus.ABSENT) {
                createDetail(student, course, savedDaily.getId(), AttendanceType.DINNER, date,
                        LocalTime.of(17, 50 + random.nextInt(10)), true);
            }
        }
    }
    
    private void createPastData(Member student, Course course, LocalDate date) {
        // 학생별/날짜별 랜덤 시나리오
        AttendanceStatus status;
        boolean forceLate = false;
        boolean forceAbsent = false;

        if (student.getLoginId().equals("student1")) {
            status = AttendanceStatus.PRESENT; // 김철수: 개근
        } else if (student.getLoginId().equals("student2")) {
            // 이영희: 짝수 날짜 지각
            forceLate = (date.getDayOfMonth() % 2 == 0);
            status = forceLate ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
        } else {
            // 박조퇴: 3의 배수 날짜 결석
            forceAbsent = (date.getDayOfMonth() % 3 == 0);
            status = forceAbsent ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT;
        }

        // 1. DailyAttendance 저장 (ID 생성을 위해 먼저 저장)
        DailyAttendance daily = DailyAttendance.builder()
                .memberId(student.getId())
                .courseId(course.getId())
                .date(date)
                .status(status)
                .morningStatus(forceAbsent ? AttendanceStatus.ABSENT : (forceLate ? AttendanceStatus.LATE : AttendanceStatus.PRESENT))
                .lunchStatus(forceAbsent ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT)
                .dinnerStatus(forceAbsent ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT)
                .build();

        DailyAttendance savedDaily = dailyAttendanceRepository.save(daily);

        // 2. DetailedAttendance 저장 (Daily ID 연결)
        if (!forceAbsent) {
            // 아침 (지각이면 09:30, 아니면 08:50)
            createDetail(student, course, savedDaily.getId(), AttendanceType.MORNING, date,
                    forceLate ? LocalTime.of(9, 30) : LocalTime.of(8, 50), !forceLate);
            // 점심 (12:00)
            createDetail(student, course, savedDaily.getId(), AttendanceType.LUNCH, date,
                    LocalTime.of(12, 0), true);
            // 저녁 (18:00)
            createDetail(student, course, savedDaily.getId(), AttendanceType.DINNER, date,
                    LocalTime.of(18, 0), true);
        }
    }
    private void createDetail(Member m, Course c, Long dailyId, AttendanceType type, LocalDate date, LocalTime time, boolean verified) {
        detailedAttendanceRepository.save(DetailedAttendance.builder()
                .memberId(m.getId())
                .courseId(c.getId())
                .dailyAttendanceId(dailyId) // ✅ 연결!
                .type(type)
                .inputNumber("1234")
                .checkTime(LocalDateTime.of(date, time))
                .connectionIp("127.0.0.1")
                .isVerified(verified)
                .failReason(verified ? null : "지각 또는 인증 실패")
                .build());
    }
    
    // 오늘 출석 데이터 생성 헬퍼 메서드
    private void createTodayAttendance(Member student, Course course, LocalDate today, String ip,
                                        boolean hasMorning, boolean hasLunch, boolean hasDinner,
                                        boolean isLate, boolean isLunchLate, boolean isDinnerLate) {
        // 아침 출석
        if (hasMorning) {
            LocalTime morningTime = isLate ? LocalTime.of(9, 30) : LocalTime.of(8, 55);
            detailedAttendanceRepository.save(DetailedAttendance.builder()
                    .memberId(student.getId())
                    .courseId(course.getId())
                    .dailyAttendanceId(null)
                    .type(AttendanceType.MORNING)
                    .inputNumber("1234")
                    .checkTime(LocalDateTime.of(today, morningTime))
                    .connectionIp(ip)
                    .isVerified(!isLate)
                    .failReason(isLate ? "출석 가능 시간이 아닙니다. (출석 가능: 08:50 ~ 09:10)" : null)
                    .build());
        }
        
        // 점심 출석
        if (hasLunch) {
            LocalTime lunchTime = isLunchLate ? LocalTime.of(12, 45) : LocalTime.of(12, 0);
            detailedAttendanceRepository.save(DetailedAttendance.builder()
                    .memberId(student.getId())
                    .courseId(course.getId())
                    .dailyAttendanceId(null)
                    .type(AttendanceType.LUNCH)
                    .inputNumber("5678")
                    .checkTime(LocalDateTime.of(today, lunchTime))
                    .connectionIp(ip)
                    .isVerified(!isLunchLate)
                    .failReason(isLunchLate ? "출석 가능 시간이 아닙니다. (출석 가능: 11:20 ~ 13:00)" : null)
                    .build());
        }
        
        // 저녁 출석
        if (hasDinner) {
            LocalTime dinnerTime = isDinnerLate ? LocalTime.of(18, 30) : LocalTime.of(17, 55);
            detailedAttendanceRepository.save(DetailedAttendance.builder()
                    .memberId(student.getId())
                    .courseId(course.getId())
                    .dailyAttendanceId(null)
                    .type(AttendanceType.DINNER)
                    .inputNumber("9999")
                    .checkTime(LocalDateTime.of(today, dinnerTime))
                    .connectionIp(ip)
                    .isVerified(!isDinnerLate)
                    .failReason(isDinnerLate ? "출석 가능 시간이 아닙니다. (출석 가능: 17:50 ~ 18:10)" : null)
                    .build());
        }
    }

    // 학생 생성 헬퍼 메서드
    private void createStudentIfAbsent(String loginId, String name, String phone) {
        if (memberRepository.findByLoginId(loginId).isEmpty()) {
            Member student = Member.builder()
                    .loginId(loginId)
                    .password(passwordEncoder.encode("1234"))
                    .name(name)
                    .phoneNumber(phone)
                    .role(RoleType.USER)
                    .build();
            memberRepository.save(student);
            System.out.println("학생 계정(" + loginId + ") 생성 완료");
        }
    }

    // 수강신청 생성 헬퍼 메서드
    private Enrollment createEnrollment(Member member, Course course) {
        return Enrollment.builder()
                .member(member)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .statusChangedAt(LocalDateTime.now())
                .build();
    }
    
    // 특정 날짜에 대한 출석 설정 생성 헬퍼 메서드
    private void createAttendanceConfigForDate(Course course, Member admin, LocalDate targetDate) {
        // 이미 해당 날짜의 설정이 있는지 각 타입별로 확인
        boolean morningExists = attendanceConfigRepository.findByCourseIdAndTargetDateAndType(
                course.getId(), targetDate, AttendanceType.MORNING).isPresent();
        boolean lunchExists = attendanceConfigRepository.findByCourseIdAndTargetDateAndType(
                course.getId(), targetDate, AttendanceType.LUNCH).isPresent();
        boolean dinnerExists = attendanceConfigRepository.findByCourseIdAndTargetDateAndType(
                course.getId(), targetDate, AttendanceType.DINNER).isPresent();
        
        // 아침 출석 설정 (08:50~09:10, 인증번호: 1234)
        if (!morningExists) {
            attendanceConfigRepository.save(AttendanceConfig.builder()
                    .courseId(course.getId())
                    .adminId(admin.getId())
                    .targetDate(targetDate)
                    .type(AttendanceType.MORNING)
                    .authNumber("1234")
                    .standardTime(LocalTime.of(8, 50))
                    .deadline(LocalTime.of(9, 10))
                    .validMinutes(20)
                    .build());
        }

        // 점심 출석 설정 (11:20~12:30 출석, 12:30~13:00 지각, 인증번호: 5678)
        if (!lunchExists) {
            attendanceConfigRepository.save(AttendanceConfig.builder()
                    .courseId(course.getId())
                    .adminId(admin.getId())
                    .targetDate(targetDate)
                    .type(AttendanceType.LUNCH)
                    .authNumber("5678")
                    .standardTime(LocalTime.of(12, 30))  // 12:30 이전 출석, 이후 지각
                    .deadline(LocalTime.of(13, 0))       // 13:00까지 출석 가능
                    .validMinutes(70)                     // 11:20부터 시작 (12:30 - 70분)
                    .build());
        }

        // 저녁 출석 설정 (17:50~18:10, 인증번호: 9012)
        if (!dinnerExists) {
            attendanceConfigRepository.save(AttendanceConfig.builder()
                    .courseId(course.getId())
                    .adminId(admin.getId())
                    .targetDate(targetDate)
                    .type(AttendanceType.DINNER)
                    .authNumber("9012")
                    .standardTime(LocalTime.of(17, 50))
                    .deadline(LocalTime.of(18, 10))
                    .validMinutes(20)
                    .build());
        }
    }
}