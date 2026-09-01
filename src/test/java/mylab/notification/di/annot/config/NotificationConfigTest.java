package mylab.notification.di.annot.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import mylab.notification.di.annot.EmailNotificationService;
import mylab.notification.di.annot.NotificationManager;
import mylab.notification.di.annot.SmsNotificationService;

/**
 * [Spring 실습3] Java Configuration(NotificationConfig) 기반 DI 검증 SpringTest.
 *
 * AnnotationConfigContextLoader 로 NotificationConfig 를 로드하여
 * NotificationManager 와 그 안에 주입된 이메일/SMS 서비스가 올바른지 검증한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = NotificationConfig.class,
		loader = AnnotationConfigContextLoader.class)
public class NotificationConfigTest {

	// 지문: "NotificationManager 를 주입 받기"
	@Autowired
	NotificationManager notificationManager;

	@Test
	void notificationManagerTest() {
		// a. NotificationManager 레퍼런스가 Not Null
		assertNotNull(notificationManager);

		// c. 이메일 서비스 검증
		assertNotNull(notificationManager.getEmailService());
		EmailNotificationService emailService =
				(EmailNotificationService) notificationManager.getEmailService();
		assertEquals("smtp.gmail.com", emailService.getSmtpServer());
		assertEquals(587, emailService.getPort());

		// d. SMS 서비스 검증
		assertNotNull(notificationManager.getSmsService());
		SmsNotificationService smsService =
				(SmsNotificationService) notificationManager.getSmsService();
		assertEquals("SKT", smsService.getProvider());

		// e. NotificationManager 의 메서드 실행
		notificationManager.sendNotificationByEmail("테스트 이메일");
		notificationManager.sendNotificationBySms("테스트 SMS");
	}
}
