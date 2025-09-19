## Spring-Cloud-Gateway

- [Docs](https://spring.io/projects/spring-cloud-gateway)
- Spring-Cloud-Gateway는 Spring 기반으로 API Gateway를 구축할 수 있도록 API 라우팅, 보안, 모니터링/메트릭, 복원력을 제공해주는 라이브러리이다.

- 특징:
  - Spring Framework 기반으로 구축
  - Spring WebFlux와 Spring Web MVC 호환 가능
  - 모든 요청 속성에 대한 경로를 일치시킬 수 있음
  - 술어와 필터는 경로에 따라 다름
  - Spring Cloud Circuit Breaker 와 통합가능
  - Spring Cloud DiscoveryClient 와 통합가능
  - 요청 속도 제한
  - 경로 재작성

### 요청량 제한 필터 실습해보기

- 백엔드 서비스
- 게이트웨이 서비스

위와 같은 서비스 모듈로 나누어 프로젝트를 생성하기 위해서, 멀티모듈 구조로 프로젝트를 생성한다.

<img width="457" height="697" alt="image" src="https://github.com/user-attachments/assets/8c4fa40b-9ed5-4906-9774-2f16a49ec1a9" />

- 새로운 프로젝트를 생성하고, 기존에 생성되어있는 src 디렉토리를 삭제한다.

<img width="548" height="775" alt="image" src="https://github.com/user-attachments/assets/0eac31ba-8928-4634-b3b0-e343670fba79" />

- New -> Module 을 누른다.

<img width="801" height="631" alt="image" src="https://github.com/user-attachments/assets/d9628c4c-381e-43e0-9323-4663fcf395f1" />

- 이름으로 `backend-service`를 입력한다.
- Next -> Create 한다.

<img width="442" height="104" alt="image" src="https://github.com/user-attachments/assets/087c26fa-7902-4bd3-996d-7f6f1e47dd78" />

- src 디렉토리, build.gradle을 제외한 모든 파일을 삭제한다.

##### build.gradle
```
plugins {
    id 'org.springframework.boot'
    id 'java'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```
build.gradle에 덮어쓴다.

<img width="302" height="95" alt="image" src="https://github.com/user-attachments/assets/678d6de4-db60-4385-a968-c0883f200923" />

- resources 디렉토리에 `application.yml`을 생성한다.

```yml
server:
  port: 8081
  instance-id: abc2

spring:
  application:
    name: backend-service

management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: always
```

- server.port는 8081으로 설정한다.
- instance-id: abc2
  - 인스턴스 고유 식별자를 명시한다. 여러 인스턴스를 띄울 때마다 구분할 수 있다.
- spring.application.name: backend-service
  - 스프링 애플리케이션의 이름을 설정한다. eureka의 서비스 레지스트리와 같은 연동이나 로그에 표시될 때 쓰인다.
- management.endpoints.web.exposure.includeㅁ
  - health : Actuator 엔드포인트 중 웹(HTTP)으로 노출할 목록을 제한한다. health 엔드포인트만 외부에서 호출 가능하도록 한다   
- management.endpoint.health.show-details
  - always : health 엔드포인트에서 전체 세부 정보를 항상 보여주도록 한다.

```java
@RestController
public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    @Value("${server.instance-id}")
    private String instanceId;

    @GetMapping("/request")
    public String response() {
        log.info("server id is : {} ", instanceId);
        return instanceId;
    }
}
```

- Controller 클래스를 생성하여 요청을 받을 엔드포인트를 작성한다.

<img width="369" height="71" alt="image" src="https://github.com/user-attachments/assets/d2b4e702-7010-4273-b331-392807e90d12" />

- gateway-service라는 이름으로 새로운 모듈을 생성한다.


```
plugins {
    id 'org.springframework.boot'
    id 'java'
}

dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway-server-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.cloud:spring-cloud-starter-loadbalancer'
    implementation 'io.github.resilience4j:resilience4j-ratelimiter:2.0.0'
    implementation 'io.github.resilience4j:resilience4j-reactor:2.0.0'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

- `gateway-service`의 build.gradle 에 위와 같이 작성한다.

```
include 'gateway-service'
include 'backend-service'
```
- root 디렉토리의 settings.gradle 에 모듈들을 include 한다.

```
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.4' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}

group = 'org.example'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

ext {
    set('springCloudVersion', "2025.0.0")
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    dependencyManagement {
        imports {
            mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
        }
    }
}
```

- root 디렉토리의 build.gradle 에 위와 같이 작성한다.

#### RateLimiterFilter

gateway-service에 ratelimiter를 작성하여 api 요청량을 제한해볼 것이다.
작성 전에, RateLimiter 종류에 대해서 알아보도록 하자.

##### Guava RateLimiter
- Google의 Guava 라이브러리 안에 포함된 RateLimiter이다. 표준처럼 많이 사용되었다.
- Token Bucket 알고리즘 기반
  - 토큰이 없으면 대기하거나 요청을 거부


##### Bucket4j

- Token Bucket 알고리즘 기반
- 특징 : Redis 연동으로 분산 Rate Limit 제어 가능, 다양한 refill 전략(고정, 점진적) 지원.

##### Netflix Hystrix

- Hystrix 자체는 Circuit Breaker 중심이지만, 내부적으로 Concurrency 제한이나 Thread Pool/Queue 제한을 통해 Rate Limiting 비슷한 효과를 낸다.
- 공식적으로 deprecated 된 상태 이 자리를 Resilience4j가 대체

##### Resilience4j 등장 배경

- Guava : 심플하지만 확장성 부족, 분산 지원 약함.
- Hystrix : 무거움, deprecated
- Bucket4j : 강력하지만 외부연동이 필요할 수 있음. 경량 라이브러리화 한것이 Resilience4j이다.

---

- Resilience4j 의 RateLimiter를 사용하여 Filter 설정을 해보자.

##### FilterConfig 클래스 작성
  
```java

@Configuration
public class FilterConfig {
    @Bean
    public RouteLocator gatewayRoutes(final RouteLocatorBuilder builder, Resilience4jRateLimiterFilter rateLimiterFilter) {
        return builder.routes()
                .route("backend-service", route -> route
                        .path("/request/**") // 이 주소로 요청이 올 경우
                        .filters(filter -> filter.filter(rateLimiterFilter))     // 필터 동작을 실행
                        .uri("http://localhost:8081"))
                .build();
    }
}
```

- RouteLocator는 Spring Cloud Gateway에서 라우팅 규칙을 정의하는 Bean이다.
- `RouteLocatorBuilder`를 이용해 어떤 요청을 하면 어떤 서비스로 보낼지 결정한다.
- `.route("backend-service", route -> ...)` 여기서 `"backend-service"`는 라우트의 식별자이다.
- `.path("/request/**"` 클라이언트의 요청 경로가 `/request/**` 패턴과 일치할 때만 라우트가 실행된다.
- `.filters(filter -> filter.filter(rateLimiterFilter))` 는 Gateway 필터를 적용하는 부분이다.
  - `rateLimiterFilter`는 Resilience4j RateLimiter를 구현한 커스텀 필터이다.
  - 요청이 들어올때마다 Resilience4j RateLimiter가 확인해서, 허용된 요청이면 진행한다.
  - 허용량을 초과하면 `HTTP 429 (Too Many Requests)` 에러를 반환한다.
- `.uri("http://localhost:8081")` 을 통하여 `backend-service`로 요청을 전달한다.
- `lb://`는 Eureka(Service Discovery)를 통한 로드 밸런싱을 의미한다.
  
##### RateLimiterConfigLoader 클래스 작성

```java
@Configuration
public class RateLimiterConfigLoader {
    @Bean
    public RateLimiter resilience4jRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofMillis(0))
                .build();
        return RateLimiter.of("gatewayRateLimiter", config);
    }
}
```

- `RateLimiterConfig`는 얼마나 많은 요청을 허용할지, 언제 리셋할지, 대기 정책은 어떻게 할지를 정의한다.
- `limitForPeriod(5)`
  - 주어진 refresh 기간 동안 최대 5개의 요청을 허용한다.
- `limitRefreshPeriod(Duration.ofSeconds(1))
  - 요청 제한이 1초 단위로 초기화 된다.
- `timeoutDuration(Duration.ofMillis(0))`
  - 요청이 허용되지 않으면 대기하지 않고 바로 실패 처리한다.

#### Resilience4jRateLimiterFilter

```java
@Component
public class Resilience4jRateLimiterFilter implements GatewayFilter, Ordered {

    private final RateLimiter rateLimiter;

    public Resilience4jRateLimiterFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return Mono.just(exchange)
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .flatMap(e -> chain.filter(exchange))
                .onErrorResume(throwable -> {
                    // 요청 제한 초과 시 429 반환
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                });
    }

    @Override
    public int getOrder() {
        return -1; // 필터 순서 우선순위 (적절히 조정)
    }
}
```
##### Gatewayfilter, Ordered, RateLimiter
- `GatewayFilter`는 Spring Cloud Gateway의 필터 체인에 참여할 수 있는 인터페이스이다.
- `Ordered` 는 필터 실행 순서를 지정할 수 있다.
- 두 클래스에 대한 구현 메서드를 작성하여 우리가 원하는 filter 동작을 설정할 수 있다.
- `RateLimiter` 는 `RateLimiterConfigLoader`에서 주입받은 객체이다.

##### filter 구현 메서드
- `Mono.just(exchange)`
  - WebFlux에서 요청/응답을 감싸는 `ServerWebExchange` 객체를 Reactive 스트림으로 시작한다.
- `.transformDeferred(RateLimiterOperator.of(rateLimiter))`
  - Resilience4j의 RateLimiter 연산자를 적용한다.
  - 해당 파싱에 도달했을때, 요청을 허용할지 차단할지 결정한다.
  - 요청이 거부되면, 에러가 발생한다.
- `.flatMap(e -> chain.filter(exchange))`
  - 허용된 경우, Gateway 의 다음 필터로 요청을 넘겨준다.
- `.onErrorResume(throwable -> { ... })`
  - 요청이 RateLimiter에 의해 거부되면 **429 Too Many Requests** 응답을 반환한다.

##### getOrder 구현 메서드
- GatewayFilter는 여러 개 있을 수 있으므로 순서를 지정해야 한다.
- 작은 숫자일수록 먼저 실행된다.
- 여기서는 `-1`로 작성하여 가장 먼저 실행되도록 유도했다.

