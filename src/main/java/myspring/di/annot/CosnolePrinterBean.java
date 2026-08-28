package myspring.di.annot;

import org.springframework.stereotype.Component;

@Component("consolePrinter")
public class CosnolePrinterBean implements PrinterBean {
	public CosnolePrinterBean() {
		System.out.println(this.getClass().getName() + "기본생성자 호출됨!");
	}
	
	public void print(String message) {
		System.out.println(message);
	}
}
