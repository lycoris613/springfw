package mylab.notification.di.annot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mylab.notification.di.annot.EmailNotificationService;
import mylab.notification.di.annot.NotificationManager;
import mylab.notification.di.annot.SmsNotificationService;

/**
 * [Spring 실습3] DI 전략3 - Java Configuration 방식.
 *
 * 지문 5번 조건:
 *  - @Configuration 으로 스프링 설정 클래스임을 명시
 *  - @Bean 으로 EmailNotificationService / SmsNotificationService / NotificationManager 정의
 *  - EmailNotificationService : SMTP 서버 "smtp.gmail.com", 포트 587 로 생성
 *  - SmsNotificationService   : 제공업체 "SKT" 로 생성
 *  - NotificationManager      : 위 두 서비스를 주입하여 생성
 */
@Configuration
public class NotificationConfig {

	// EmailNotificationService(String smtpServer, int port) 생성자 주입
	@Bean
	public EmailNotificationService emailNotificationService() {
		return new EmailNotificationService("smtp.gmail.com", 587);
	}

	// SmsNotificationService(String provider) 생성자 주입
	@Bean
	public SmsNotificationService smsNotificationService() {
		return new SmsNotificationService("SKT");
	}

	// NotificationManager(NotificationService email, NotificationService sms) 생성자 주입
	// 첫 번째 인자 = 이메일 서비스, 두 번째 인자 = SMS 서비스
	@Bean
	public NotificationManager notificationManager() {
		return new NotificationManager(emailNotificationService(), smsNotificationService());
	}
}
