## 해시 안정기와 Eureka를 통한 분산 처리

- 서비스 관리는 유레카를 통해서 진행하고, 유레카 서비스를 해시 링 형태로 구성하여 분산처리를 진행하였다.

- client-service는 직접 요청을 하는 서버이다. 가상의 클라이언트가 있다고 가정하고 실습을 진행하였다.

- https://cloud.spring.io/spring-cloud-netflix/reference/html/#netflix-eureka-client-starter


유레카 서버 등록설정
[code](https://github.com/amazon7737/system-design/blob/main/consistent%20hash/eureka-service/src/main/resources/application.yml)
```yml
server:
  port: 8761

eureka:
  client:
    fetchRegistry: false
    registerWithEureka: false
  server:
    enableSelfPreservation: false
```

백엔드 서버 설정
```yml
server:
  port: 8081

spring:
  application:
    name: backend-service
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
    instance:
      instance-id: backend-service-1-${random.value}
```

두번째 백엔드 서버 설정
```yml
server:
  port: 8082

spring:
  application:
    name: backend-service
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
    instance:
      instance-id: backend-service-1-${random.value}
```

클라이언트 서버 설정
```yml
spring:
  application:
    name: client-service

  cloud:
    loadbalancer:
      ribbon:
        enabled: false

server:
  port: 8083

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
gateway:
  url: http://localhost:8080
  
```

게이트웨이 서버 설정
```yml
spring:
  main:
    allow-bean-definition-overriding: true
  application:
    name: gateway-service
  cloud:
    gateway:
      routes:
        - id: backend-service
          uri: lb://backend-service
          predicates:
            - Path=/api/**
    discovery:
      enabled: true
    eureka:
      client:
        serviceUrl:
          defaultZone: http://localhost:8761/eureka/
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: INFO
```


### 전체 프로세스

1. client-service → /test?userId=abc123 요청
2. WebClient가 X-Hash-Key: abc123 포함해서 gateway-service의 /api/hello 호출
3. gateway-service의 Consistent Hash LoadBalancer가 같은 userId는 같은 인스턴스로 라우팅
4. backend-service 인스턴스가 응답
5. 동일한 userId로 요청하면 항상 같은 인스턴스에 도달


#### 백엔드 서비스들 실행


#### 유레카 서비스 실행


#### 클라이언트 서비스 실행



#### 게이트웨이 서비스 실행

