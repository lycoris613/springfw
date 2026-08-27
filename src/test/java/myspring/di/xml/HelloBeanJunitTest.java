package myspring.di.xml;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class HelloBeanJunitTest {
	
	ApplicationContext context;
	
	@BeforeEach
	void setup( ) {
		System.out.println("==> setup");
		//1. Spring Container 객체생성
		context = new GenericXmlApplicationContext("classpath:hello-bean.xml");
	}
	
	@Test
	void helloBeanSetter() {
		System.out.println("==> helloBeanSetter");
		Hello hello1 = (Hello)context.getBean("hello");
		Hello hello2 = context.getBean("hello", Hello.class);
		
		//주소 비교
		System.out.println(hello1 == hello2);
		assertSame(hello1, hello2);
		
		//값 비교
		assertEquals("Hello 스프링", hello1.sayHello());
		
		hello1.print();
		
		Printer printer = context.getBean("strPrinter", Printer.class);
		assertEquals("Hello 스프링", printer.toString());
	}
	
	

}
