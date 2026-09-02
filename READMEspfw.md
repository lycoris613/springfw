# springfw — 스프링 프레임워크 학습 저장소 정리

스프링의 **DI(의존성 주입) → AOP → 트랜잭션 → MyBatis 연동 → MVC/REST** 를 단계별로 실습한 코드 모음이다.
이 문서는 **코드를 다시 읽을 때 옆에 두는 공부 자료**다. 개념마다

1. **정의 코드** — 그 개념이 실제로 어떻게 생겼는지
2. **쓰는 법** — 어디서 어떻게 호출/설정하는지
3. **응용 · 비교** — XML / 어노테이션 / Java Config 로 어떻게 바뀌는지

순서로 정리했다. 세 프로젝트가 **같은 내용을 설정 방식만 바꿔** 반복하므로, 중복은 비교표로 접고 개념은 한 번만 설명한다.

- 빌드 도구: Maven (`pom.xml`), Spring `5.2.15.RELEASE`
- 자바: 1.8
- 테스트: `MySpringFW` = JUnit 5(Jupiter) + `SpringExtension`, `SpringFWXml`·`SpringFWConfig` = JUnit 4 + `SpringJUnit4ClassRunner`

---

## 목차

- [1. 저장소 한눈에 보기](#1-저장소-한눈에-보기)
- [2. 관통하는 축 — 같은 객체 그래프, 세 가지 조립법](#2-관통하는-축--같은-객체-그래프-세-가지-조립법)
- [3. DI 기초 (MySpringFW)](#3-di-기초-myspringfw)
- [4. 제출 과제 (MySpringFW / mylab)](#4-제출-과제-myspringfw--mylab)
- [5. 웹 3계층 애플리케이션 (SpringFWXml = XML, SpringFWConfig = Java Config)](#5-웹-3계층-애플리케이션-springfwxml--xml-springfwconfig--java-config)
- [6. AOP](#6-aop)
- [7. 트랜잭션](#7-트랜잭션)
- [8. MyBatis 연동](#8-mybatis-연동)
- [9. Spring MVC / REST](#9-spring-mvc--rest)
- [10. XML ↔ 어노테이션 ↔ Java Config 정면 비교](#10-xml--어노테이션--java-config-정면-비교)
- [11. 자주 쓰는 패턴 모음](#11-자주-쓰는-패턴-모음)
- [12. 코드에서 눈에 띄는 점 · 심화 포인트](#12-코드에서-눈에-띄는-점--심화-포인트)
- [13. 실행 방법](#13-실행-방법)
- [14. 스스로 점검하는 질문](#14-스스로-점검하는-질문)

---

## 1. 저장소 한눈에 보기

| 프로젝트 | 다루는 것 | 설정 방식 | DB/웹 |
|---|---|---|---|
| **MySpringFW** | 순수 DI (컨테이너·주입·스코프·프로퍼티) + 제출 과제 | XML / 어노테이션 / Java Config 3종 | 없음 (spring-context + spring-test 만) |
| **SpringFWXml** | DI + AOP + 트랜잭션 + MyBatis + MVC 를 갖춘 사용자·학생 관리 웹앱 | **전부 XML** (`beans.xml`, `beans-web.xml`) | MariaDB + Tomcat |
| **SpringFWConfig** | 위와 **같은 앱** | **전부 Java `@Configuration`** (`AppConfig` 등) | MariaDB + Tomcat |

```
springfw/
├─ MySpringFW/          ← 개념 학습. 여기서 DI 3전략을 잡는다
│  └─ src/main/java/
│     ├─ myspring/di/xml/      Hello · Printer — XML 로 조립
│     ├─ myspring/di/annot/    HelloBean · PrinterBean — @Component 로 조립
│     ├─ myspring/di/*/config/ HelloConfig — @Configuration 으로 조립
│     └─ mylab/                제출 과제 4종 (order / user / notification / student)
│
├─ SpringFWXml/         ← 같은 웹앱을 XML 로
│  └─ src/main/resources/
│     ├─ beans.xml         루트 컨텍스트: DataSource · Tx · AOP · MyBatis · 서비스/DAO 스캔
│     ├─ beans-web.xml     서블릿 컨텍스트: Controller 스캔 · ViewResolver · <mvc:annotation-driven>
│     ├─ *Mapper.xml       MyBatis SQL
│     └─ SqlMapConfig.xml  MyBatis 전역 설정 (typeAlias)
│
└─ SpringFWConfig/      ← 같은 웹앱을 Java Config 로
   └─ src/main/java/myspring/config/
      ├─ AppConfig.java       @Import + @ComponentScan + @EnableAspectJAutoProxy
      ├─ DatabaseConfig.java  @Bean DataSource / TransactionManager, @EnableTransactionManagement
      ├─ MyBatisConfig.java   @Bean SqlSessionFactory, @MapperScan
      └─ MvcConfig.java       @EnableWebMvc, ViewResolver, 메시지 컨버터
```

읽는 순서: **`MySpringFW` 로 DI 3전략을 잡고 → `SpringFWXml` 로 웹앱 전체 흐름을 보고 → `SpringFWConfig` 에서 같은 설정을 Java 로 바꿔 읽는다.**

---

## 2. 관통하는 축 — 같은 객체 그래프, 세 가지 조립법

### IoC / DI 한 줄 정의

- **IoC (제어의 역전)**: 객체 생성·연결·생명주기의 제어권을 개발자 코드가 아니라 **컨테이너**가 가진다.
- **DI (의존성 주입)**: 어떤 객체가 필요로 하는 다른 객체(의존성)를 **컨테이너가 넣어준다**. `new` 를 직접 쓰지 않는다.

저장소 전역에서 반복되는 최소 예제는 **`Hello` 가 `Printer` 에 의존**하는 구조다. `Hello.print()` 는 직접 출력하지 않고 주입받은 `Printer` 에게 위임한다.

```java
// myspring/di/xml/Hello.java  (요약)
public class Hello {
    String name;
    Printer printer;                       // 의존성 — 구현체를 Hello 가 모른다

    public Hello() {}
    public Hello(String name, Printer printer) {   // ← Constructor Injection 통로
        this.name = name; this.printer = printer;
    }
    public void setName(String name)      { this.name = name; }       // ← Setter Injection 통로
    public void setPrinter(Printer p)     { this.printer = p; }

    public String sayHello() { return "Hello " + name; }
    public void print()      { this.printer.print(sayHello()); }      // 위임
}
```
```java
// Printer 구현 2종 — 같은 인터페이스, 다른 동작
public interface Printer { void print(String message); }
// ConsolePrinter : System.out.println 으로 즉시 출력
// StringPrinter  : StringBuffer 에 모아뒀다가 toString() 으로 확인 (테스트에서 결과 검증용)
```

### 세 가지 조립법 (같은 결과, 다른 설정)

| | 빈 정의 | 주입 | 프로퍼티 값 |
|---|---|---|---|
| **① XML** | `<bean id class>` | `<property>` / `<constructor-arg>` | `<context:property-placeholder>` + `${...}` |
| **② 어노테이션 + 스캔** | `@Component` + `<context:component-scan>` (또는 `@ComponentScan`) | `@Autowired` `@Qualifier` | `@Value("${...}")` |
| **③ Java `@Configuration`** | `@Bean` 메서드 | `@Bean` 메서드 안에서 직접 `set`/생성자 호출 | `@PropertySource` + `Environment` |

```xml
<!-- ① MySpringFW/src/main/resources/hello-bean.xml -->
<bean id="strPrinter" class="myspring.di.xml.StringPrinter" />
<bean id="hello" class="myspring.di.xml.Hello" scope="singleton">
    <property name="name" value="스프링" />          <!-- setName("스프링") 호출 -->
    <property name="printer" ref="strPrinter" />      <!-- setPrinter(strPrinter) 호출 -->
</bean>
<bean id="helloC" class="myspring.di.xml.Hello">      <!-- 같은 클래스, 생성자 주입 버전 -->
    <constructor-arg index="0" value="생성자" />
    <constructor-arg index="1" ref="conPrinter" />
</bean>
```
```java
// ② MySpringFW  myspring/di/annot/HelloBean.java
@Component("helloBean")
public class HelloBean {
    @Value("${myName}") String name;                 // values.properties 의 myName
    @Autowired @Qualifier("stringPrinter")           // 타입이 같은 빈이 여러 개일 때 이름으로 선택
    PrinterBean printer;
    @Value("#{'${myNameList}'.split(',')}")          // SpEL — "a,b,c" → List<String>
    List<String> names;
}
```
```java
// ③ MySpringFW  myspring/di/xml/config/HelloConfig.java
@Configuration
@PropertySource("classpath:values.properties")
public class HelloConfig {
    @Autowired Environment env;

    @Bean public Printer stringPrinter() { return new StringPrinter(); }

    @Bean @Scope("singleton")
    public Hello hello() {
        Hello hello = new Hello();
        hello.setName(env.getProperty("myName"));     // 프로퍼티는 Environment 로 읽는다
        hello.setPrinter(stringPrinter());            // ← @Bean 메서드 호출 = "그 빈을 달라"
        return hello;
    }
}
```

> **핵심 감각**: `@Configuration` 안에서 `stringPrinter()` 를 두 번 호출해도 **같은 싱글턴 빈**이 돌아온다(CGLIB 프록시). 일반 `new` 처럼 매번 새 객체가 아니다.

---

## 3. DI 기초 (MySpringFW)

### 3.1 Setter Injection vs Constructor Injection

| | Setter | Constructor |
|---|---|---|
| 통로 | `setXxx()` 메서드 | 생성자 파라미터 |
| XML | `<property name ref\|value>` | `<constructor-arg index\|value\|ref>` |
| 어노테이션 | 필드/세터에 `@Autowired` | 생성자에 `@Autowired` + 파라미터에 `@Value`/`@Qualifier` |
| 성격 | 선택적 의존성, 나중에 교체 가능 | 필수 의존성, 불변(final) 가능, 순환참조를 컴파일 단계에서 드러냄 |

```java
// 생성자 주입에서 파라미터별로 값·빈을 지정 — myspring/di/annot/HelloBeanCons.java
@Component("helloBean")
public class HelloBeanCons {
    @Autowired
    public HelloBeanCons(@Value("${myName2}") String name,
                         @Qualifier("consolePrinter") PrinterBean printer) { ... }
}
```

### 3.2 컨테이너 만드는 법 (테스트 3가지)

```java
// (a) 순수 자바 — 컨테이너를 손으로 만든다. myspring/di/xml/HelloBeanJunitTest.java
ApplicationContext ctx = new GenericXmlApplicationContext("classpath:hello-bean.xml");
Hello hello = ctx.getBean("hello", Hello.class);

// (b) SpringExtension + XML — 컨테이너 생성/주입을 프레임워크가. HelloBeanSpringTest.java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:hello-bean.xml")
class ... { @Autowired @Qualifier("helloC") Hello hello; }

// (c) SpringExtension + Java Config. HelloBeanConfigTest.java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HelloBeanConfig.class, loader = AnnotationConfigContextLoader.class)
```

`@Resource(name="hello")` (JSR-250) 는 **이름 우선** 주입, `@Autowired` 는 **타입 우선**(+`@Qualifier` 로 이름 보조) — [`HelloBeanSpringTest`](MySpringFW/src/test/java/myspring/di/xml/HelloBeanSpringTest.java) 가 둘을 나란히 쓴다.

### 3.3 어노테이션 스캔

```java
@Component("helloBean")     // 빈 id. 안 주면 클래스명 첫 글자 소문자(helloBean)
@Component / @Service / @Repository / @Controller   // 의미만 다르고 스캔 대상인 건 동일
```
```xml
<context:component-scan base-package="myspring.di.annot" />   <!-- 이 패키지 이하 @Component 자동 등록 -->
<context:property-placeholder location="classpath:values.properties" />  <!-- ${...} 치환 -->
```
Java 로는 `@ComponentScan(basePackages=...)` + `@PropertySource(...)` — [`HelloBeanConfig`](MySpringFW/src/main/java/myspring/di/annot/config/HelloBeanConfig.java).

### 3.4 스코프 · 프로퍼티

- `@Scope("singleton")`(기본) — 컨테이너당 1개. `assertSame(ctx.getBean("hello"), ctx.getBean("hello"))` 통과.
- `values.properties` 의 한글은 `\uXXXX` 로 이스케이프되어 있다(`myName=어노...`). properties 파일 인코딩 이슈 회피용.
- `Hello`·`Printer` 구현들의 **생성자마다 `System.out.println(...기본생성자 호출됨!)`** 이 박혀 있다. 콘솔에서 "빈이 언제 · 몇 번 만들어지는지"(싱글턴이면 1번, prototype 이면 요청마다) 눈으로 확인하는 학습 장치다.

---

## 4. 제출 과제 (MySpringFW / mylab)

DI 3전략을 그대로 적용한 미니 실습 4개. 상세 조건은 원 문제지 참고.

| 실습 | 패키지 | 전략 | 설정 파일 | 테스트 |
|---|---|---|---|---|
| 1. 주문–장바구니–상품 | [`mylab.order.di.xml`](MySpringFW/src/main/java/mylab/order/di/xml) | **XML** (setter + constructor 혼합) | [`mylab-order-di.xml`](MySpringFW/src/main/resources/mylab-order-di.xml) | [`OrderSpringTest`](MySpringFW/src/test/java/mylab/order/di/xml/OrderSpringTest.java) |
| 2. 사용자 등록 | [`mylab.user.di.annot`](MySpringFW/src/main/java/mylab/user/di/annot) | **어노테이션 + component-scan** | [`mylab-user-di.xml`](MySpringFW/src/main/resources/mylab-user-di.xml) | [`UserServiceTest`](MySpringFW/src/test/java/mylab/user/di/annot/UserServiceTest.java) |
| 3. 알림(이메일/SMS) | [`mylab.notification.di.annot`](MySpringFW/src/main/java/mylab/notification/di/annot) | **Java `@Configuration`** | [`NotificationConfig`](MySpringFW/src/main/java/mylab/notification/di/annot/config/NotificationConfig.java) | [`NotificationConfigTest`](MySpringFW/src/test/java/mylab/notification/di/annot/config/NotificationConfigTest.java) |
| 1-1. 성적–강좌–학생 (선택) | [`mylab.student.di.xml`](MySpringFW/src/main/java/mylab/student/di/xml) | **XML** | [`mylab-student-di.xml`](MySpringFW/src/main/resources/mylab-student-di.xml) | [`StudentSpringTest`](MySpringFW/src/test/java/mylab/student/di/xml/StudentSpringTest.java) |

각 실습이 강조하는 포인트:

- **실습1** — 같은 클래스를 `id="product1"` 은 `<property>`(setter), `id="product2"` 는 `<constructor-arg>`(생성자) 로 등록해 두 방식을 한 파일에서 대비. `<list><ref/></list>` 로 컬렉션 주입.
- **실습2** — `@Value("MySQL")` 로 상수 주입(프로퍼티 파일 없이), `@Autowired` 필드 주입, XML 은 `<context:component-scan>` 한 줄뿐.
- **실습3** — 생성자만 있는 클래스(`EmailNotificationService(smtpServer, port)`)를 `@Bean` 메서드에서 `new` 로 조립. XML/스캔 없이 순수 Java.
- **실습1-1** — 실습1과 구조가 같음. `Course.getAverageScore()`, `GradeService.calculateGrade()` 같은 **도메인 로직**이 주입된 그래프 위에서 동작하는지까지 검증.

---

## 5. 웹 3계층 애플리케이션 (SpringFWXml = XML, SpringFWConfig = Java Config)

두 프로젝트는 `myspring.user`·`myspring.student` 자바 코드를 **거의 그대로** 공유하고 설정만 다르다. 자바 쪽 차이는 딱 세 군데뿐이며, 그 자체가 학습 포인트다:

| 파일 | SpringFWConfig | SpringFWXml |
|---|---|---|
| `UserServiceImpl` | `@Transactional` 애노테이션으로 트랜잭션 선언 | 애노테이션 **없음** — `beans.xml` 의 `<tx:advice>`+`<aop:advisor>` 가 메서드 이름 규칙으로 건다 |
| `UserMapper` (인터페이스) | `@Mapper` | `@MyMapper` (자체 정의한 빈 마커) |
| `StudentMapper` (인터페이스) | `@Mapper` | 마커 없음 — `MapperScannerConfigurer` 가 패키지째 스캔 |

### 계층 구조

```
HTTP 요청
  │
  ▼
@RestController / @Controller      ← 요청 매핑, 파라미터 바인딩, 응답 형식
  │  RestfulUserController, UserController, RestfulStudentController
  ▼
@Service  UserServiceImpl          ← 업무 로직 + @Transactional 경계
  │
  ▼
@Repository  UserDaoImpl           ← 영속성. SqlSession / Mapper 호출
  │
  ▼
MyBatis Mapper (XML + 인터페이스)   ← SQL
  │
  ▼
DataSource → MariaDB (spring_db)
```

- 계층 간 연결은 전부 **인터페이스 타입 `@Autowired`**. `UserService` ← `UserServiceImpl`, `UserDao` ← `UserDaoImpl`.
- 스테레오타입만 계층별로 다르다: `@Controller` / `@Service` / `@Repository`. (실동작은 `@Component` 와 같지만 의도를 드러내고, `@Repository` 는 DataAccessException 변환 기능이 붙는다.)

### 도메인 클래스 (VO)

- [`UserVO`](SpringFWConfig/src/main/java/myspring/user/vo/UserVO.java) — `id`(PK, auto_increment) / `userId`(업무 키) / `name` / `gender` / `city`. 생성자 오버로딩 + `this(...)` 위임.
- [`StudentVO`](SpringFWConfig/src/main/java/myspring/student/vo/StudentVO.java) — `DeptVO` 1건(연관) + `List<CourseStatusVO>`(컬렉션) 을 품는다 → MyBatis `<association>` / `<collection>` 매핑 대상.
- `CourseVO`, `DeptVO`, `CourseStatusVO` — 학생-강좌-수강상태 관계.

### 두 컨텍스트 구조 (공통)

스프링 웹앱은 컨텍스트가 **2개**다.

| 컨텍스트 | 만드는 주체 | 담는 빈 | XML | Java |
|---|---|---|---|---|
| 루트(부모) | `ContextLoaderListener` | Service, DAO, DataSource, Tx, AOP, MyBatis | `classpath:beans.xml` | `myspring.config.AppConfig` |
| 서블릿(자식) | `DispatcherServlet` | Controller, ViewResolver, 메시지 컨버터 | `classpath:beans-web.xml` | `myspring.config.MvcConfig` |

```xml
<!-- SpringFWXml/src/main/webapp/WEB-INF/web.xml (요약) -->
<context-param><param-name>contextConfigLocation</param-name>
    <param-value>classpath:beans.xml</param-value></context-param>
<listener><listener-class>...ContextLoaderListener</listener-class></listener>

<servlet><servlet-name>dispatcher</servlet-name>
    <servlet-class>...DispatcherServlet</servlet-class>
    <init-param><param-name>contextConfigLocation</param-name>
        <param-value>classpath:beans-web.xml</param-value></init-param></servlet>
<servlet-mapping><servlet-name>dispatcher</servlet-name><url-pattern>/</url-pattern></servlet-mapping>
```
```xml
<!-- SpringFWConfig/src/main/webapp/WEB-INF/web.xml — 같은 구조를 Java 클래스로 -->
<context-param><param-name>contextClass</param-name>
    <param-value>...AnnotationConfigWebApplicationContext</param-value></context-param>
<context-param><param-name>contextConfigLocation</param-name>
    <param-value>myspring.config.AppConfig</param-value></context-param>
<!-- DispatcherServlet 쪽엔 myspring.config.MvcConfig -->
```

**Controller 를 어느 컨텍스트에 둘지**가 관건 — 스캔을 필터로 나눈다:

```xml
<!-- beans.xml : Controller 만 빼고 스캔 (루트) -->
<context:component-scan base-package="myspring.user,myspring.student,myspring.aop.annot">
    <context:exclude-filter type="annotation"
        expression="org.springframework.stereotype.Controller"/>
</context:component-scan>

<!-- beans-web.xml : Controller 만 스캔 (서블릿) -->
<context:component-scan base-package="myspring.user,myspring.student">
    <context:include-filter type="annotation"
        expression="org.springframework.stereotype.Controller"/>
</context:component-scan>
```
```java
// Java Config : 패키지 규칙으로 분리
@ComponentScan(basePackages = {"myspring.*.service","myspring.*.dao","myspring.aop.annot"})  // AppConfig
@ComponentScan(basePackages = {"myspring.*.controller"})                                     // MvcConfig
```

### AppConfig 가 나머지를 끌어모으는 법

```java
// SpringFWConfig/src/main/java/myspring/config/AppConfig.java
@Import({DatabaseConfig.class, MyBatisConfig.class})   // 설정 클래스 조합
@ComponentScan(basePackages = {"myspring.*.service","myspring.*.dao","myspring.aop.annot"})
@EnableAspectJAutoProxy                                 // = <aop:aspectj-autoproxy/>
public class AppConfig {}
```
`@Import` 로 `DatabaseConfig`(DataSource·Tx) + `MyBatisConfig`(SqlSessionFactory) 를 합친다. `MvcConfig` 는 서블릿 컨텍스트가 따로 로드하므로 `@Import` 에서 빠져 있다(주석 처리).

---

## 6. AOP

### 정의 · 용어

**AOP** = 로깅·트랜잭션·성능측정처럼 여러 계층에 흩어지는 **횡단 관심사(cross-cutting concern)** 를 비즈니스 코드에서 분리해 한곳에 모으는 기법.

| 용어 | 뜻 | 이 저장소에서 |
|---|---|---|
| Aspect | 횡단 관심사 모듈 | `LoggingAspect`, `PerformanceTraceAdvice` |
| Join Point | 끼어들 수 있는 지점 | 스프링에선 **메서드 실행** |
| Pointcut | 어느 Join Point에 적용할지 식 | `execution(public * myspring.user..*(..))` |
| Advice | 실제로 실행할 코드 + 시점 | `@Before`, `@Around` 등 |
| Weaving | Advice 를 대상에 엮는 것 | 스프링은 런타임 **프록시** 생성 |

### 방식 A — POJO advice + `<aop:config>` (XML 전용)

```java
// myspring/aop/xml/PerformanceTraceAdvice.java — 스프링/AspectJ 어노테이션 없음
public class PerformanceTraceAdvice {
    public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();               // ← 타깃 메서드 호출 (Around)
        } finally {
            System.out.println(joinPoint.getSignature().toShortString()
                + " 실행 시간 : " + (System.currentTimeMillis() - start) + " ms");
        }
    }
}
```
```xml
<!-- beans.xml -->
<bean id="performanceTraceAdvice" class="myspring.aop.xml.PerformanceTraceAdvice" />
<aop:config>
    <aop:aspect id="traceAspect" ref="performanceTraceAdvice">
        <aop:around pointcut="execution(public * myspring.user..*(..))" method="trace" />
    </aop:aspect>
</aop:config>
```

### 방식 B — `@Aspect` 어노테이션

```java
// myspring/aop/annot/LoggingAspect.java
@Component @Aspect
public class LoggingAspect {
    static final Logger logger = LogManager.getLogger();

    @Before("execution(public * myspring..*(..))")
    public void before(JoinPoint jp) { logger.debug(">>>> @Before [" + jp.getSignature().getName() + "] ..."); }

    @AfterReturning(pointcut="execution(public * myspring.user.service.*.*(..))", returning="ret")
    public void afterReturning(JoinPoint jp, Object ret) { ... }   // 정상 리턴 후, 리턴값 접근

    @AfterThrowing(pointcut="execution(* *..UserService*.*(..))", throwing="ex")
    public void afterThrowing(JoinPoint jp, Throwable ex) { ... }  // 예외 던졌을 때

    @After("execution(* *..*.*User(..))")
    public void afterFinally(JoinPoint jp) { ... }                 // 정상/예외 무관 finally
}
```
활성화: `<aop:aspectj-autoproxy />` (XML) 또는 `@EnableAspectJAutoProxy` (Java). `@Aspect` 이면서 **빈**이어야 하므로 `@Component` + 스캔이 필요하다.

- `SpringFWXml` : `beans.xml` 에서 방식 A·B 를 **동시에** 켠다.
- `SpringFWConfig` : `AppConfig` 스캔에 `myspring.aop.annot` 이 들어가 방식 B 만 살아 있다. 방식 A(`aop.xml` 패키지)는 스캔/등록 대상이 아니라 **비활성**.

`LoggingAspect` 는 `logger.debug(...)` 로 찍으므로 [`log4j2.xml`](SpringFWXml/src/main/resources/log4j2.xml) 에서 `<Logger name="myspring" level="DEBUG">` 라야 콘솔·`logs/logfile.log` 에 보인다. 같은 파일이 MyBatis SQL 로그(`SqlMapConfig.xml` 의 `logImpl=LOG4J2`)도 함께 출력한다.

### `execution()` 식 읽는 법

```
execution( public * myspring.user..*(..) )
           │      │ │            │  │  └ 파라미터 임의 개수/타입
           │      │ │            │  └ 임의 메서드명
           │      │ │            └ myspring.user 및 하위 패키지 전부(..)
           │      │ └ 임의 반환타입
           │      └ (수식어 생략 가능)
           └ 접근제어자
```

---

## 7. 트랜잭션

### 선언적 트랜잭션 = AOP 의 특수한 경우

`@Transactional`(또는 `<tx:advice>`) 은 메서드 앞뒤에 "커밋/롤백" advice 를 두르는 것.

```java
// SpringFWConfig  myspring/user/service/UserServiceImpl.java
@Service("userService")
@Transactional(readOnly = true)                       // 클래스 기본값: 읽기 전용
public class UserServiceImpl implements UserService {
    @Autowired UserDao userdao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)   // 쓰기 메서드는 개별 재정의
    public void insertUser(UserVO user) { userdao.insert(user); }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void deleteUser(String id) { userdao.delete(id); }
}
```

활성화 · TxManager 등록:

```java
// DatabaseConfig.java
@Configuration
@EnableTransactionManagement                          // = <tx:annotation-driven>
public class DatabaseConfig {
    @Bean public DataSource dataSource() {
        BasicDataSource ds = new BasicDataSource();    // tomcat-dbcp 커넥션 풀
        ds.setDriverClassName(env.getProperty("db.driverClass"));
        ds.setUrl(env.getProperty("db.url")); ...
        return ds;
    }
    @Bean public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
}
```

XML 은 `@Transactional` 대신 **메서드 이름 규칙 + 포인트컷**으로 트랜잭션을 건다:

```xml
<!-- beans.xml -->
<bean id="transactionManager"
      class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <property name="dataSource" ref="dataSource" />
</bean>

<tx:advice id="txAdvice" transaction-manager="transactionManager">
    <tx:attributes>
        <tx:method name="get*" read-only="true"/>
        <tx:method name="update*" propagation="NOT_SUPPORTED"/>   <!-- 실습 흔적 -->
        <tx:method name="update"  propagation="MANDATORY"/>
    </tx:attributes>
</tx:advice>

<aop:config>
    <aop:pointcut id="txPointCut" expression="execution(public * myspring.user..*(..))"/>
    <aop:advisor advice-ref="txAdvice" pointcut-ref="txPointCut"/>
</aop:config>
```

- `DataSource` 구현이 두 프로젝트에서 다르다: XML=`SimpleDriverDataSource`(풀 없음, 학습용), Java=`BasicDataSource`(DBCP 풀).
- `propagation` 값(`REQUIRED`, `MANDATORY`, `NOT_SUPPORTED`, `NEVER` …) 실습 주석이 `beans.xml` 에 남아 있다 — "이미 트랜잭션이 있는데 `NEVER` 면 예외" 같은 동작을 눈으로 확인한 흔적.

---

## 8. MyBatis 연동

### 조립 (3개 빈)

```
DataSource ──▶ SqlSessionFactoryBean ──▶ SqlSessionTemplate(SqlSession)
                     │  (+ SqlMapConfig.xml, *Mapper.xml, typeAliasesPackage)
                     ▼
              Mapper 인터페이스 스캔 (@Mapper)
```

```java
// SpringFWConfig  MyBatisConfig.java
@Configuration @EnableTransactionManagement
@MapperScan(basePackages = {"myspring.user.dao.mapper","myspring.student.dao.mapper"},
            sqlSessionFactoryRef = "sqlSessionFactoryBean")
public class MyBatisConfig {
    @Bean
    public SqlSessionFactory sqlSessionFactoryBean(DataSource ds, ApplicationContext ctx) throws Exception {
        SqlSessionFactoryBean f = new SqlSessionFactoryBean();
        f.setDataSource(ds);
        f.setConfigLocation(ctx.getResource("classpath:SqlMapConfig.xml"));
        f.setTypeAliasesPackage("myspring.user.vo, myspring.student.vo");     // VO 짧은 별칭
        f.setMapperLocations(ctx.getResources("classpath:mapper/**/*Mapper.xml"));
        return f.getObject();
    }
    @Bean public SqlSessionTemplate sqlSession(SqlSessionFactory f) { return new SqlSessionTemplate(f); }
}
```
```xml
<!-- beans.xml — 같은 것을 XML 로 -->
<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
    <property name="basePackage" value="myspring.**.dao.mapper"/>
    <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory" />
</bean>
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource" />
    <property name="configLocation" value="classpath:SqlMapConfig.xml" />
    <property name="mapperLocations">
        <list><value>classpath:UserMapper.xml</value><value>classpath:StudentMapper.xml</value></list>
    </property>
</bean>
<bean id="sqlSession" class="org.mybatis.spring.SqlSessionTemplate">
    <constructor-arg ref="sqlSessionFactory" />
</bean>
```

`SqlMapConfig.xml` — MyBatis 전역 설정. 여기선 주로 **typeAlias**(매퍼 XML 에서 `type="Student"` 처럼 짧게 쓰기)와 `logImpl=LOG4J2`, `defaultStatementTimeout`.

### DAO 를 쓰는 두 가지 스타일

| 스타일 | 코드 | 특징 |
|---|---|---|
| **SqlSession 직접** | `session.selectOne("userNS.selectUserById", id)` | 네임스페이스+id 문자열. 컴파일 체크 없음 | 
| **Mapper 인터페이스** | `userMapper.selectUserById(id)` | `@Mapper` 인터페이스 메서드 = SQL id. 타입 안전 |

```java
@Repository("userDao")
public class UserDaoImpl implements UserDao {           // ← 활성 (스캔됨)
    @Autowired private SqlSession session;
    public UserVO read(String id) { return session.selectOne("userNS.selectUserById", id); }
}

// UserDaoImplMapper (Mapper 인터페이스 방식), UserDaoImplJDBC (JdbcTemplate 방식)
//   → @Repository 주석 처리되어 비활성. 같은 UserDao 를 3가지로 구현해 비교용으로 남김.
```

### 매퍼 XML — 연관/컬렉션/동적 SQL

```xml
<!-- StudentMapper.xml -->
<resultMap id="studentCourseStatusResultMap" type="Student">
    <id     property="id"   column="stu_id"/>
    <result property="name" column="stu_name"/>
    <association property="dept" javaType="Dept" resultMap="deptResultMap"/>          <!-- 1:1 -->
    <collection  property="courseStatus" ofType="CourseStatus"
                 resultMap="coursestatusResultMap"/>                                  <!-- 1:N -->
</resultMap>

<sql id="selectStudent">select * from student</sql>                                   <!-- 조각 재사용 -->

<select id="selectStudentByGradeOrDay" parameterType="Student" resultMap="studentResultMap">
    <include refid="selectStudent"/>
    <where>
        <if test="grade != null">   stu_grade = #{grade} </if>
        <if test="daynight != null"> and stu_daynight = #{daynight} </if>            <!-- 동적 조건 -->
    </where>
</select>
```

- **인터페이스 어노테이션 + XML 혼용**: `StudentMapper.java` 는 간단한 쿼리에 `@Select` + `@ResultMap("studentResultMap")`(XML 의 resultMap 재사용), 복잡한 쿼리는 XML 에.
- **네임스페이스 불일치 주의**: `UserMapper.xml` 은 `namespace="userNS"`(임의 문자열), `StudentMapper.xml` 은 `namespace="myspring.student.dao.mapper.StudentMapper"`(인터페이스 FQN). 후자만 `@Mapper` 인터페이스 호출과 자동으로 이어진다. 전자는 `session.selectOne("userNS...")` 문자열 호출 전제.

---

## 9. Spring MVC / REST

### 매핑 · 바인딩

```java
@RestController                                     // = @Controller + 모든 메서드에 @ResponseBody
public class RestfulUserController {
    @Autowired private UserService userService;

    @RequestMapping(value="/users/{id}", method=GET, produces={"application/json"})
    public UserVO getUser(@PathVariable String id) { return userService.getUser(id); }

    @RequestMapping(value="/users", method=POST, headers={"Content-type=application/json"})
    public Boolean insertUser(@RequestBody UserVO user) { ... }   // JSON 바디 → 객체
}
```

| 애너테이션 | 역할 |
|---|---|
| `@PathVariable` | URL 경로 조각 (`/users/{id}`) |
| `@RequestParam` | 쿼리스트링/폼 파라미터 |
| `@RequestBody` | 요청 바디(JSON) → 객체 역직렬화 |
| `@ModelAttribute` | 폼 필드 → 객체 바인딩 (`UserController` 의 화면 흐름) |

- [`UserController`](SpringFWConfig/src/main/java/myspring/user/controller/UserController.java) = 전통적 화면(JSP) 방식: `Model`/`ModelAndView` 반환 → `viewName` → ViewResolver 가 `/userList.jsp` 로. `redirect:/getUserList.do` 패턴, `@ExceptionHandler` 로 에러 페이지.
- [`RestfulUserController`](SpringFWConfig/src/main/java/myspring/user/controller/RestfulUserController.java) = JSON/XML 반환. 같은 자원(`/users`)을 GET/POST/PUT/DELETE 로.
- **뷰 파일** (`src/main/webapp/`): `userList.jsp`·`userInfo.jsp`·`userInsert.jsp`·`userUpdate.jsp` 는 JSTL/EL 로 `model` 속성을 출력·폼 바인딩, `viewError.jsp` 는 `@ExceptionHandler` 대상. `userList_Json.html`·`userList_Xml.html` 는 REST 엔드포인트를 브라우저에서 호출해보는 정적 테스트 페이지.
- **한글 인코딩**: 두 `web.xml` 모두 `CharacterEncodingFilter` 를 `*.do` 에 매핑한다. POST 파라미터 한글 깨짐을 막는 표준 설정 — 빠지면 폼 입력이 `???` 로 저장된다.

### View / 메시지 컨버터

```java
// MvcConfig.java
@Configuration @EnableWebMvc                        // = <mvc:annotation-driven>
@ComponentScan(basePackages = {"myspring.*.controller"})
public class MvcConfig extends WebMvcConfigurerAdapter {
    @Override public void configureDefaultServletHandling(DefaultServletHandlerConfigurer c) { c.enable(); }
    @Bean public static InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver v = new InternalResourceViewResolver();
        v.setPrefix("/"); v.setSuffix(".jsp"); return v;                 // viewName "userList" → /userList.jsp
    }
    @Override public void configureMessageConverters(List<HttpMessageConverter<?>> conv) {
        conv.add(new MappingJackson2HttpMessageConverter());            // 객체 ↔ JSON
    }
}
```
```xml
<!-- beans-web.xml -->
<mvc:annotation-driven />
<mvc:default-servlet-handler/>                     <!-- 정적 리소스는 서블릿 컨테이너로 -->
<bean id="viewResolver" class="...InternalResourceViewResolver">
    <property name="prefix" value="/" /><property name="suffix" value=".jsp" />
</bean>
```

### JSON + XML 동시 제공

```java
// 같은 목록을 XML 로도 — JAXB
@XmlRootElement(name = "users")
public class UserVOXML {
    private String status;
    @XmlElement(name="user") private List<UserVO> userList;
}
```
```java
@RequestMapping(value="/usersXml", method=GET, produces={"application/xml"})
public UserVOXML getUserListXml() { return new UserVOXML("success", userService.getUserList()); }
```
`ContentNegotiatingViewResolver`(MvcConfig) + `produces` 로 요청이 원하는 표현형식을 고른다.

---

## 10. XML ↔ 어노테이션 ↔ Java Config 정면 비교

| 하고 싶은 일 | XML | 순수 어노테이션 | Java `@Configuration` |
|---|---|---|---|
| 빈 등록 | `<bean id class>` | `@Component`/`@Service`/… + 스캔 | `@Bean` 메서드 |
| 스캔 켜기 | `<context:component-scan base-package>` | `@ComponentScan` | `@ComponentScan` |
| 세터 주입 | `<property name ref\|value>` | 필드/세터 `@Autowired` | `@Bean` 안 `obj.setX(...)` |
| 생성자 주입 | `<constructor-arg>` | 생성자 `@Autowired` | `@Bean` 안 `new X(...)` |
| 여러 후보 중 선택 | `ref="id"` | `@Qualifier("id")` | 해당 `@Bean` 메서드 호출 |
| 프로퍼티 파일 | `<context:property-placeholder location>` | `@PropertySource` + `@Value("${k}")` | `@PropertySource` + `Environment` |
| 값 주입 | `value="..."` | `@Value("...")` / SpEL `#{...}` | 코드로 직접 |
| 설정 조합 | `<import resource>` | — | `@Import({A.class, B.class})` |
| AOP 프록시 | `<aop:aspectj-autoproxy/>` | — | `@EnableAspectJAutoProxy` |
| 트랜잭션 | `<tx:advice>` + `<aop:advisor>` | `@Transactional` + `<tx:annotation-driven>` | `@Transactional` + `@EnableTransactionManagement` |
| MyBatis 매퍼 스캔 | `<bean MapperScannerConfigurer>` | — | `@MapperScan` |
| MVC 기본 설정 | `<mvc:annotation-driven/>` | — | `@EnableWebMvc` |
| 웹 컨텍스트 로딩 | `contextConfigLocation = classpath:beans.xml` | — | `AnnotationConfigWebApplicationContext` + `AppConfig` |

트레이드오프:

- **XML** — 코드 재컴파일 없이 배선 변경, 전체 배선을 한 파일에서 조망. 대신 오타가 런타임에야 터지고 리팩터링 도구가 못 따라온다.
- **어노테이션 + 스캔** — 가장 간결. 대신 배선이 여러 파일에 흩어지고, 외부 라이브러리 클래스(내가 `@Component` 못 붙이는)엔 못 쓴다.
- **Java Config** — 타입 안전 + IDE 지원 + 조건 로직 가능. 외부 클래스도 `@Bean` 으로 등록. 대신 설정 코드가 늘어난다.
- 실무는 보통 **내 코드 = 어노테이션 스캔, 외부 라이브러리 빈(DataSource 등) = Java Config `@Bean`** 조합.

---

## 11. 자주 쓰는 패턴 모음

```java
// (1) 컨테이너 직접 생성 (프로덕션 부트스트랩 / 간단 실행)
ApplicationContext ctx = new GenericXmlApplicationContext("classpath:beans.xml");
UserService svc = ctx.getBean("userService", UserService.class);

// (2) 스프링 통합 테스트 — 컨테이너 재사용(캐시)됨
@ExtendWith(SpringExtension.class)                       // JUnit5
@ContextConfiguration(locations = "classpath:beans.xml") // 또는 classes = AppConfig.class
class XxxTest { @Autowired UserService svc; }

@RunWith(SpringJUnit4ClassRunner.class)                  // JUnit4 (이 저장소 웹 프로젝트)
@ContextConfiguration(classes = AppConfig.class, loader = AnnotationConfigContextLoader.class)

// (3) 같은 타입 빈이 여러 개 → 이름으로 특정
@Autowired @Qualifier("stringPrinter") Printer printer;  // 타입우선 + 이름보조
@Resource(name = "hello") Hello hello;                   // 이름우선

// (4) 값/SpEL 주입
@Value("${db.url}") String url;                          // 프로퍼티
@Value("#{'${myNameList}'.split(',')}") List<String> names;  // SpEL 로 List

// (5) @Bean 메서드끼리 참조 = 그 싱글턴을 주입
@Bean Hello hello() { Hello h = new Hello(); h.setPrinter(stringPrinter()); return h; }

// (6) 설정 클래스 조합
@Import({DatabaseConfig.class, MyBatisConfig.class})

// (7) 컴포넌트 스캔을 필터로 쪼개기 (Controller 를 서블릿 컨텍스트로)
<context:exclude-filter type="annotation" expression="org.springframework.stereotype.Controller"/>

// (8) 트랜잭션 경계는 Service 에, 클래스 기본값 + 메서드 재정의
@Transactional(readOnly = true)                          // class
@Transactional(propagation = Propagation.REQUIRED)       // method override

// (9) MyBatis 연관/컬렉션
<association property="dept" resultMap="deptResultMap"/>     // 1:1
<collection property="courseStatus" ofType="CourseStatus"/>  // 1:N

// (10) MyBatis 동적 SQL
<where><if test="grade != null">stu_grade = #{grade}</if></where>

// (11) REST 메서드 시그니처
@RequestMapping(value="/users/{id}", method=GET, produces={"application/json"})
public UserVO getUser(@PathVariable String id) { ... }
public Boolean insertUser(@RequestBody UserVO user) { ... }
```

---

## 12. 코드에서 눈에 띄는 점 · 심화 포인트

"왜 이렇게 했을까 / 어떻게 하면 나을까" 를 고민하기 좋은 지점.

1. **주입 방식 혼용** — [`UserServiceImpl`](SpringFWConfig/src/main/java/myspring/user/service/UserServiceImpl.java) 은 `@Autowired` **필드**와 `setUserdao()` **세터**가 둘 다 있다. 하나로 통일하는 게 낫다(요즘 권장은 생성자 주입).

2. **매퍼 네임스페이스 불일치** — `UserMapper.xml`=`userNS`, `StudentMapper.xml`=인터페이스 FQN. 규칙이 갈려서 User 쪽은 `@Mapper` 인터페이스와 XML 이 자동 연결되지 않는다(그래서 `UserDaoImpl` 이 `session.selectOne("userNS...")` 문자열 방식). 전부 FQN 으로 통일하면 `UserDaoImplMapper` 를 바로 쓸 수 있다.

3. **DAO 3구현** — `UserDaoImpl`(SqlSession) / `UserDaoImplMapper`(Mapper 인터페이스) / `UserDaoImplJDBC`(JdbcTemplate). 활성은 `@Repository` 붙은 하나뿐, 나머지는 주석. 같은 인터페이스를 세 기술로 구현한 비교 표본이므로 지우지 말고 읽어볼 것.

4. **프로퍼티 파일 경로 오타** — `SpringFWXml/annot.xml` 의 `<context:property-placeholder location="classpath:config/value.properties"/>` 가 가리키는 `config/` 디렉터리는 없다(실제 파일은 [`src/main/resources/value.properties`](SpringFWXml/src/main/resources/value.properties)). 이 `annot.xml` 을 로드하는 [`di/annot/HelloBeanSpringTest`](SpringFWXml/src/test/java/myspring/di/annot/HelloBeanSpringTest.java) 의 `Hello` 는 `@Value("어노테이션")` 처럼 **리터럴**만 써서 `${...}` 치환이 필요 없다 — 경로가 틀려도 어설션은 통과할 수 있으니, placeholder 를 실제로 쓰는 코드를 추가하기 전에 경로부터 고쳐야 한다. `beans.xml` 쪽 경로(`classpath:value.properties`)는 정상.

5. **파일명 불일치** — `MySpringFW` 는 `values.properties`, 웹 두 프로젝트는 `value.properties`. 복붙 시 헷갈리는 지점.

6. **deprecated API** — `MvcConfig extends WebMvcConfigurerAdapter`. 스프링 5 부터 `WebMvcConfigurer`(default 메서드 인터페이스)를 직접 구현하는 방식으로 대체됐다.

7. **`@Aspect` 인데 포인트컷이 광범위** — `LoggingAspect.before` 의 `execution(public * myspring..*(..))` 는 컨트롤러·서비스·DAO 전부에 걸린다. 로그가 매우 많아진다 — `myspring.*.service` 정도로 좁히는 연습.

8. **XML 프로젝트는 AOP 3종을 동시에** — `beans.xml` 에 `<tx:advice>`+`<aop:advisor>`, `<aop:aspect>`(around trace), `<aop:aspectj-autoproxy>`(@Aspect) 가 전부 켜져 있다. 한 메서드에 advice 가 여러 개 걸릴 때 실행 순서를 추적해볼 것.

9. **아무 일도 안 하는 마커** — [`MyMapper.java`](SpringFWXml/src/main/java/myspring/user/dao/mapper/MyMapper.java) 는 `@Retention`·`@Target` 도 없는 빈 애노테이션인데 `SpringFWXml` 의 `UserMapper` 인터페이스에 `@MyMapper` 로 붙어 있다. `MapperScannerConfigurer` 가 패키지(`myspring.**.dao.mapper`)째 스캔하므로 이 마커는 동작에 영향이 없다 — `@Mapper` 로 바꾸거나 제거해도 똑같다.

9-2. **빈 이름 충돌 소지** — `MySpringFW` 의 `HelloBean` 과 `HelloBeanCons` 는 **둘 다 `@Component("helloBean")`** 이고 같은 패키지(`myspring.di.annot`)라, 이 패키지를 스캔하면 빈 이름이 겹친다 — 한쪽 이름을 바꾸거나 스캔에서 제외해야 안전하다.

10. **오타 그대로** — `CosnolePrinterBean`(Console), 과제 지문의 `Cource`(Course). 클래스명이라 고치려면 참조 전부 수정 필요.

11. **`SimpleDriverDataSource` vs 커넥션 풀** — `SpringFWXml` 은 매 요청 커넥션을 새로 여는 학습용 DataSource. 실제 서비스는 `SpringFWConfig` 처럼 풀(`BasicDataSource`/HikariCP)을 써야 한다.

12. **JUnit 4 테스트가 대부분 `@Ignore`** — DB(MariaDB `spring_db`) 가 떠 있어야 도는 통합 테스트라서. 실행 전 `sql/` 스크립트로 스키마+데이터를 넣어야 한다.

---

## 13. 실행 방법

### DI 학습 (MySpringFW) — DB 불필요

```bash
cd MySpringFW
mvn test                       # 전체
mvn test -Dtest=OrderSpringTest # 개별
```
Eclipse: 테스트 클래스 우클릭 → **Run As → JUnit Test**.

### 웹 프로젝트 (SpringFWXml / SpringFWConfig) — DB + 서버 필요

1. MariaDB 에 DB/계정 준비 (`sql/user생성.txt` 참고) 후 스크립트 실행:
   ```bash
   mysql -u spring -p spring_db < sql/user.sql
   mysql -u spring -p spring_db < sql/student.sql
   ```
   접속 정보는 `src/main/resources/value.properties` (`jdbc:mariadb://127.0.0.1:3306/spring_db`, `spring`/`spring`).
2. 통합 테스트: 원하는 메서드의 `@Ignore` 를 떼고 `mvn test` 또는 Eclipse 에서 실행.
   - XML: [`UserClient`](SpringFWXml/src/test/java/myspring/user/UserClient.java), [`StudentClient`](SpringFWXml/src/test/java/myspring/student/StudentClient.java)
   - Java Config: [`AppConfigTest`](SpringFWConfig/src/test/java/myspring/config/AppConfigTest.java)
3. 웹 구동: `mvn package` → WAR 를 Tomcat 에 배포하거나, Eclipse 의 **Run on Server**. REST 확인은 `sql/postman_student.txt` 참고.

---

## 14. 스스로 점검하는 질문

**DI 기초**
- IoC 와 DI 는 같은 말인가? 다르다면 무엇의 부분집합인가?
- Setter 주입과 생성자 주입을 각각 언제 쓰나? 순환 참조는 어느 쪽에서 더 빨리 드러나나?
- `@Autowired` 와 `@Resource` 의 기본 탐색 기준(타입/이름) 차이는? `@Qualifier` 는 어느 쪽을 보조하나?
- `@Configuration` 클래스에서 `stringPrinter()` 를 두 번 호출하면 객체가 몇 개 생기나? 왜?
- `<bean scope="singleton">` 과 `prototype` 은 무엇이 다른가? 이 저장소 테스트의 `assertSame` 이 통과하는 이유는?

**설정 방식**
- Controller 를 루트 컨텍스트가 아니라 서블릿 컨텍스트에 두는 이유는? 루트에 두면 무슨 일이 생기나?
- `beans.xml` 의 `exclude-filter` 와 `beans-web.xml` 의 `include-filter` 가 하는 일을 Java Config 는 어떻게 대신하나?
- 외부 라이브러리의 `DataSource` 를 `@Component` 로 등록할 수 없는 이유는? 그럼 어떻게 등록하나?

**AOP / 트랜잭션**
- Aspect / Pointcut / Advice / Weaving 을 `PerformanceTraceAdvice` 예로 각각 지목해보라.
- `@Around` advice 에서 `joinPoint.proceed()` 를 호출하지 않으면 어떻게 되나?
- `@Transactional` 이 붙은 메서드를 **같은 클래스의 다른 메서드가 직접 호출**하면 트랜잭션이 걸리나? (프록시 self-invocation)
- `<tx:method name="get*" read-only="true"/>` 는 무엇을 기준으로 메서드를 고르나? `@Transactional` 방식과 무엇이 다른가?

**MyBatis / MVC**
- `SqlSession` 직접 호출과 `@Mapper` 인터페이스 호출의 장단점은? 이 저장소에서 User/Student 가 왜 갈렸나?
- `<association>` 과 `<collection>` 은 각각 어떤 관계를 매핑하나? `StudentVO` 에서 예를 들어보라.
- `@RequestBody` 와 `@ModelAttribute` 는 각각 어떤 요청을 처리하나?
- 같은 URL `/users` 에 GET/POST/PUT/DELETE 를 매핑하는 REST 스타일의 장점은?
