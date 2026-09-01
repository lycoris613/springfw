package mylab.user.di.annot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * [Spring 실습2] 사용자 저장소.
 *
 * @Component : component-scan 대상이 되어 SpringBean 으로 자동 등록된다.
 * @Value("MySQL") : 지문 조건 - "UserRepository 클래스의 dbType 변수에 @Value(\"MySQL\") 선언".
 */
@Component
public class UserRepository {

	@Value("MySQL")
	private String dbType;

	public UserRepository() {}

	public String getDbType() { return dbType; }
	public void setDbType(String dbType) { this.dbType = dbType; }

	public boolean saveUser(String userId, String name) {
		System.out.println("사용자 저장: " + userId + ", " + name + " (DB: " + dbType + ")");
		return true;
	}

	@Override
	public String toString() {
		return "UserRepository [dbType=" + dbType + "]";
	}
}
