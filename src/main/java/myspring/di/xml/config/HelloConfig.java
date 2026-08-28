package myspring.di.xml.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import myspring.di.xml.ConsolePrinter;
import myspring.di.xml.Hello;
import myspring.di.xml.Printer;
import myspring.di.xml.StringPrinter;

@Configuration
@PropertySource(value="classpath:values.properties")
public class HelloConfig {
	@Autowired
	Environment env;
	
	// <bean id="strPrinter" class="myspring.di.xml.StringPrinter" />
	@Bean
	public Printer stringPrinter() {
		return new StringPrinter();
	}
	
	//<been id="conPrinter" class="myspring.di.xml.ConsolePrinter" />
	@Bean
	public Printer consolePrinter() {
		return new ConsolePrinter();
	}
	
	/*
	 * 	<bean id="hello" class="myspring.di.xml.Hello" scope="singleton">
			<!-- setName("스프링") 메서드 호출 -->
			<property name="name" value="스프링" />
			<!-- setPrinter(new StringPrinter()) 메서드 호출 -->
			<property name="printer" ref="strPrinter" />
	    </bean>
	*/
	
	@Bean @Scope(value = "singleton")
	public Hello hello() {
		Hello hello = new Hello();
		//자바컨피그
		String propName = env.getProperty("myName");
		hello.setName(propName);
		hello.setPrinter(stringPrinter());
		return hello;
	}

}
