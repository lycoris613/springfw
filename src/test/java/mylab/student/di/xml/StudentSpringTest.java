package mylab.student.di.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * [Spring 실습1-1] (선택 과제) DI 가 XML 설정대로 올바르게 동작하는지 검증하는 SpringTest.
 *
 * 실습1(OrderSpringTest)과 동일한 방식:
 *  - SpringExtension + @ContextConfiguration 으로 mylab-student-di.xml 로드
 *  - Course, GradeService 두 SpringBean 을 주입 받아 검증
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:mylab-student-di.xml")
public class StudentSpringTest {

	@Autowired
	Course course;

	@Autowired
	GradeService gradeService;

	/**
	 * Course SpringBean 테스트.
	 * course 빈의 students 리스트(setter injection + <list><ref/>)가
	 * student1, student2, student3 순서로 주입되었는지와 평균 점수 계산을 검증한다.
	 */
	@Test
	void courseBeanTest() {
		// course 객체가 Null 이 아닌지
		assertNotNull(course);

		// students 리스트에 3명의 Student 가 주입되었는지
		assertEquals(3, course.getStudents().size());

		// <ref bean="student1"/> => name="김철수", score=95
		assertEquals("김철수", course.getStudents().get(0).getName());
		assertEquals(95, course.getStudents().get(0).getScore());

		// getAverageScore() = (95 + 80 + 55) / 3 = 76.666...
		assertEquals(76.666, course.getAverageScore(), 0.01);
	}

	/**
	 * GradeService SpringBean 테스트.
	 * gradeService 빈에 course 가 setter injection 되었는지,
	 * 등급 계산과 고득점자 필터링 결과를 검증한다.
	 */
	@Test
	void gradeServiceBeanTest() {
		// gradeService 객체가 Null 이 아닌지
		assertNotNull(gradeService);

		// setCourse() 로 course 가 주입되었는지
		assertNotNull(gradeService.getCourse());

		// calculateGrade() : 95 -> "A", 80 -> "B", 55 -> "F"
		assertEquals("A", gradeService.calculateGrade("S001"));
		assertEquals("B", gradeService.calculateGrade("S002"));
		assertEquals("F", gradeService.calculateGrade("S003"));

		// getHighScoreStudents(80) : score >= 80 인 학생은 student1(95), student2(80) => 2명
		assertEquals(2, gradeService.getHighScoreStudents(80).size());
	}
}
