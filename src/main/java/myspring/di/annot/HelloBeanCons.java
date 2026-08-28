package myspring.di.annot;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component("helloBean")
public class HelloBeanCons {
	String name;

	PrinterBean printer;
	
	List<String> names;

	public HelloBeanCons() {
		System.out.println(this.getClass().getName() + "기본생성자 호출됨!");
	}
	
    @Autowired
	public HelloBeanCons(@Value("${myName2}") String name, 
			             @Qualifier("consolePrinter") PrinterBean printer) {
		System.out.println(this.getClass().getName() + "오버로딩 생성자 호출됨!");
		this.name = name;
		this.printer = printer;
//		setName(name);
//		setPrinter(printer);
	}

	public List<String> getNames() {
		return this.names;
	}

	public void setNames(List<String> list) {
		this.names = list;
	}

//	public void setName(String name) {
//		System.out.println("setName() 메서드 호출됨" + name);
//		this.name = name;
//	}

//	public void setPrinter(PrinterBean printer) {
//		System.out.println("SetPrinter() 메서드 호출됨" + printer.getClass().getName());
//		this.printer = printer;
//	}

	public String sayHello() {
		return "Hello " + name;
	}

	public void print() {
		this.printer.print(sayHello());
	}

}
