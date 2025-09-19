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
