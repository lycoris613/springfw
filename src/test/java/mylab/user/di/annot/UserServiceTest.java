package mylab.user.di.annot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * [Spring 실습2] DI 전략2 (어노테이션 + XML component-scan) 검증 SpringTest.
 *
 * mylab-user-di.xml 의 <context:component-scan/> 이 @Component 클래스들을 빈으로 등록하고,
 * @Autowired / @Value 가 정상적으로 주입되었는지, 그리고 registerUser() 동작을 검증한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:mylab-user-di.xml")
public class UserServiceTest {

	// 지문: "UserService 를 주입 받기"
	@Autowired
	UserService userService;

	@Test
	void diInjectionTest() {
		// UserService 레퍼런스가 Not Null
		assertNotNull(userService);

		// @Autowired 로 UserRepository 가 주입되었는지
		assertNotNull(userService.getUserRepository());

		// @Value("MySQL") 로 dbType 이 세팅되었는지
		assertEquals("MySQL", userService.getUserRepository().getDbType());

		// @Autowired 로 SecurityService 가 주입되었는지
		assertNotNull(userService.getSecurityService());
	}

	@Test
	void registerUserTest() {
		// password 가 전달되면 SecurityService.authenticate() 통과 -> true
		assertTrue(userService.registerUser("user01", "홍길동", "pass1234"));

		// password 가 전달되지 않으면(null) 인증 실패 -> false
		assertFalse(userService.registerUser("user02", "김철수", null));
	}
}
