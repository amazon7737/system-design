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

