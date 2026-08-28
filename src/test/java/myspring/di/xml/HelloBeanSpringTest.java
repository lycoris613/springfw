package myspring.di.xml;

import static org.junit.jupiter.api.Assertions.*;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:hello-bean.xml")
public class HelloBeanSpringTest {
	@Autowired
	@Qualifier("helloC")
	Hello hello;
	
	@Resource(name = "hello")
	Hello helloSetter;
	
	@Test
	void helloBeanCons(){
		//생성자
		System.out.println(hello.sayHello());
		//스프링
		System.out.println(helloSetter.sayHello());
		
		assertEquals("Hello 생성자", hello.sayHello());
		assertEquals("Hello 스프링", helloSetter.sayHello());
		
		hello.print();
	}
	
	
}
