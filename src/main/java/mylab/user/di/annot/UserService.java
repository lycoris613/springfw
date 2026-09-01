package mylab.user.di.annot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * [Spring 실습2] DI 전략2 - 어노테이션 + XML(component-scan) 혼합.
 *
 * @Component : mylab-user-di.xml 의 <context:component-scan base-package="mylab.user.di.annot"/>
 *              에 의해 자동으로 SpringBean 으로 등록된다.
 */
@Component
public class UserService {

	// 지문: "UserService 는 UserRepository 와 SecurityService 를 자동 주입 받음"
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SecurityService securityService;

	public UserRepository getUserRepository() { return userRepository; }
	public SecurityService getSecurityService() { return securityService; }

	public boolean registerUser(String userId, String name, String password) {
		if (securityService.authenticate(userId, password)) {
			return userRepository.saveUser(userId, name);
		}
		return false;
	}
}
