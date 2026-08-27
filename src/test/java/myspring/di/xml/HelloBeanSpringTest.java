package myspring.di.xml;

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
	
	@Test
	void helloBeanCons(){
		System.out.println(hello.sayHello());
	}
	
	
}
