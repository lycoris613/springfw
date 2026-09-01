package mylab.order.di.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * [Spring 실습1] DI(Dependency Injection) 이 XML 설정대로 올바르게 동작하는지 검증하는 SpringTest.
 *
 * - SpringExtension + @ContextConfiguration 으로 mylab-order-di.xml 을 로드한다.
 * - ShoppingCart, OrderService 두 SpringBean 을 주입 받아 검증한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:mylab-order-di.xml")
public class OrderSpringTest {

	// 지문: "ShoppingCart 클래스와 OrderService 클래스를 Injection 받으세요"
	@Autowired
	ShoppingCart shoppingCart;

	@Autowired
	OrderService orderService;

	/**
	 * ShoppingCart SpringBean 테스트.
	 * shoppingCart 빈의 products 리스트(setter injection + <list><ref/>)가
	 * product1, product2 순서로 주입되었는지 검증한다.
	 */
	@Test
	void shoppingCartBeanTest() {
		// shoppingCart 객체가 Null 이 아닌지
		assertNotNull(shoppingCart);

		// products 리스트에 2개의 Product 가 주입되었는지
		assertEquals(2, shoppingCart.getProducts().size());

		// <ref bean="product1"/> => name="노트북"
		assertEquals("노트북", shoppingCart.getProducts().get(0).getName());
		// <ref bean="product2"/> => name="스마트폰"
		assertEquals("스마트폰", shoppingCart.getProducts().get(1).getName());
	}

	/**
	 * OrderService SpringBean 테스트.
	 * orderService 빈에 shoppingCart 가 setter injection 되었는지,
	 * 주문 총액 계산 결과가 두 상품 가격의 합과 같은지 검증한다.
	 */
	@Test
	void orderServiceBeanTest() {
		// orderService 객체가 Null 이 아닌지
		assertNotNull(orderService);

		// setShoppingCart() 로 shoppingCart 가 주입되었는지
		assertNotNull(orderService.getShoppingCart());

		// calculateOrderTotal() = product1(150000) + product2(800000) = 950000
		assertEquals(950000.0, orderService.calculateOrderTotal());
	}
}
