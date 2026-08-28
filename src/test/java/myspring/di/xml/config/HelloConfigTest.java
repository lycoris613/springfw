package myspring.di.xml.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import myspring.di.xml.Hello;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes= HelloConfig.class)
public class HelloConfigTest {
	@Autowired
	Hello hello;
	
	@Test
	void helloConfig() {
		System.out.println(hello.sayHello());
	}
	
	
}
