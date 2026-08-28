package myspring.di.annot;

import static org.junit.jupiter.api.Assertions.*;

import javax.annotation.Resource;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:hello-annot.xml")
public class AnnotatedHelloBeanTest {
	@Autowired
	HelloBean hello;
	
	@Resource(name = "stringPrinter")
	PrinterBean printer;
	
	@Autowired
	HelloBeanCons helloCons;
	
	// 전략2 어노테이션 방식 -> Constructor Injection 테스트
	@Test
	void helloBeansCons() {
		assertEquals("Hello 어노테이션생성자", helloCons.sayHello());
		helloCons.print();
	}
	
	// 전략2 어노테이션 방식 -> Setter Injection Test
	@Test @Disabled
	void helloBeanAnnot() {
		assertEquals("어노테이션", hello.sayHello());
		hello.print();
		assertEquals("어노테이션", printer.toString());
		
		assertEquals(3, hello.getNames().size());
		
		for(String name:hello.getNames()){
			System.out.println(name);
		}
	}
}
