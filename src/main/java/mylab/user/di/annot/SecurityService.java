package mylab.user.di.annot;

import org.springframework.stereotype.Component;

/**
 * [Spring 실습2] 인증/인가 담당 서비스.
 *
 * @Component : component-scan 대상이 되어 SpringBean 으로 자동 등록되고,
 *              UserService 에 @Autowired 로 주입된다.
 */
@Component
public class SecurityService {

	public boolean authenticate(String userId, String password) {
		System.out.println("인증: " + userId);
		return password != null && !password.isEmpty();
	}

	public boolean authorize(String userId, String resource) {
		System.out.println("권한 부여: " + userId + " for " + resource);
		return true;
	}
}
