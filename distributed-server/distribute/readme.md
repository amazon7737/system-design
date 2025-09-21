프로젝트의 README 파일을 작성해드리겠습니다. 먼저 현재 프로젝트 구조를 다시 한번 확인하고 포괄적인 소개글을 구성하겠습니다.

[3 tools called]

이제 README 파일을 작성하겠습니다:

```markdown
# 분산 서버 시스템 (Distributed Server System)

> **대규모 설계 기초** 실습 프로젝트 - 마이크로서비스 아키텍처와 API Gateway 패턴 구현

## 📋 프로젝트 개요

이 프로젝트는 **Spring Cloud Gateway**와 **마이크로서비스 아키텍처**를 활용한 분산 서버 시스템입니다. API Gateway 패턴, 로드밸런싱, Rate Limiting 등 실제 프로덕션 환경에서 사용되는 핵심 패턴들을 학습하고 구현합니다.

## 🏗️ 시스템 아키텍처

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Client        │───▶│  Gateway Service │───▶│ Backend Service │
│   (Port: Any)   │    │   (Port: 8080)   │    │  (Port: 8081)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │                 ┌─────────────────┐
                              └────────────────▶│ Backend Service │
                                                │  (Port: 8082)   │
                                                └─────────────────┘
```

## 🚀 주요 기능

### 🌐 Gateway Service
- **API Gateway**: 단일 진입점으로 모든 요청 라우팅
- **로드밸런싱**: 백엔드 서비스 인스턴스 간 트래픽 분산
- **Rate Limiting**: Resilience4j를 통한 요청 제한 (429 응답)
- **WebFlux**: 비동기/논블로킹 처리로 높은 성능
- **헬스체크**: `/actuator/health` 엔드포인트

### 🔧 Backend Service
- **REST API**: 간단한 인스턴스 식별 API 제공
- **다중 인스턴스**: 고가용성을 위한 2개 인스턴스 운영
- **인스턴스 식별**: 각 인스턴스별 고유 ID 반환
- **Spring MVC**: 전통적인 서블릿 기반 웹 애플리케이션

## 📦 기술 스택

### Core Framework
- **Java 17**: 최신 LTS 버전
- **Spring Boot 3.5.4**: 애플리케이션 프레임워크
- **Spring Cloud 2025.0.0**: 마이크로서비스 도구

### Gateway Service 패키지
| 패키지 | 버전 | 용도 |
|--------|------|------|
| `spring-cloud-starter-gateway-server-webflux` | - | API Gateway (WebFlux 기반) |
| `spring-boot-starter-webflux` | - | 반응형 웹 애플리케이션 |
| `spring-cloud-starter-loadbalancer` | - | 클라이언트 사이드 로드밸런싱 |
| `resilience4j-ratelimiter` | 2.0.0 | Rate Limiting 구현 |
| `resilience4j-reactor` | 2.0.0 | Resilience4j와 Reactor 통합 |

### Backend Service 패키지
| 패키지 | 버전 | 용도 |
|--------|------|------|
| `spring-boot-starter-web` | - | REST API 개발 (Spring MVC) |
| `spring-boot-starter-actuator` | - | 모니터링 및 헬스체크 |

> **📌 패키지 이름 변경 이력**  
> `spring-cloud-starter-gateway-server-webflux`는 이전에 `spring-cloud-starter-gateway`로 제공되었으나, WebFlux 기반 서버임을 명확히 구분하기 위해 현재 이름으로 변경되었습니다.

## 🐳 실행 방법

### 1. Docker Compose로 전체 시스템 실행
```bash
# 전체 시스템 빌드 및 실행
docker-compose up --build

# 백그라운드 실행
docker-compose up -d --build
```

### 2. 개별 서비스 실행 (개발용)
```bash
# Gateway Service 실행
./gradlew gateway-service:bootRun

# Backend Service 인스턴스 1 실행
./gradlew backend-service:bootRun --args='--server.port=8081 --server.instance-id=backend-1'

# Backend Service 인스턴스 2 실행
./gradlew backend-service:bootRun --args='--server.port=8082 --server.instance-id=backend-2'
```

## 🔍 API 테스트

### 기본 요청
```bash
# Gateway를 통한 요청 (로드밸런싱 확인)
curl http://localhost:8080/request

# 응답 예시: "backend-1" 또는 "backend-2"
```

### Rate Limiting 테스트
```bash
# 연속 요청으로 Rate Limit 확인
for i in {1..10}; do curl http://localhost:8080/request; echo; done

# Rate Limit 초과 시 429 응답 확인
```

### 헬스체크
```bash
# Gateway 헬스체크
curl http://localhost:8080/actuator/health

# Backend 서비스 헬스체크
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## 📊 모니터링

### 서비스 포트
- **Gateway Service**: `8080`
- **Backend Service 1**: `8081`
- **Backend Service 2**: `8082`

### 헬스체크 엔드포인트
- Gateway: `http://localhost:8080/actuator/health`
- Backend 1: `http://localhost:8081/actuator/health`
- Backend 2: `http://localhost:8082/actuator/health`

## 🛠️ 개발 환경 설정

### 필수 요구사항
- Java 17+
- Docker & Docker Compose
- Gradle 7+

### 프로젝트 구조
```
distribute/
├── gateway-service/          # API Gateway 서비스
│   ├── src/main/java/       # Gateway 소스코드
│   ├── src/main/resources/  # 설정 파일
│   └── Dockerfile           # Gateway 컨테이너 이미지
├── backend-service/         # 백엔드 서비스
│   ├── src/main/java/      # Backend 소스코드
│   ├── src/main/resources/ # 설정 파일
│   └── Dockerfile          # Backend 컨테이너 이미지
├── docker-compose.yml      # 전체 시스템 오케스트레이션
└── build.gradle           # 루트 빌드 설정
```

## 🎯 학습 목표

이 프로젝트를 통해 다음 개념들을 학습할 수 있습니다:

- ✅ **API Gateway 패턴**: 마이크로서비스의 단일 진입점
- ✅ **로드밸런싱**: 트래픽 분산을 통한 가용성 향상
- ✅ **Rate Limiting**: 서비스 보호를 위한 요청 제한
- ✅ **서비스 디스커버리**: 정적 설정 기반 서비스 발견
- ✅ **컨테이너 오케스트레이션**: Docker Compose를 통한 멀티 서비스 관리
- ✅ **헬스체크**: 서비스 상태 모니터링
- ✅ **WebFlux vs MVC**: 반응형 vs 전통적 웹 프로그래밍 모델

## 📚 참고 자료

- [Spring Cloud Gateway 공식 문서](https://docs.spring.io/spring-cloud-gateway/reference/)
- [Resilience4j 공식 문서](https://resilience4j.readme.io/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Docker Compose 가이드](https://docs.docker.com/compose/)

---

**📝 Note**: 이 프로젝트는 학습 목적으로 제작되었으며, 실제 프로덕션 환경에서는 보안, 로깅, 모니터링 등 추가적인 고려사항이 필요합니다.
```

이 README는 프로젝트의 전체적인 개요부터 구체적인 실행 방법, 기술 스택, 학습 목표까지 포괄적으로 다루고 있습니다. 특히 Spring Cloud Gateway 패키지 이름 변경 이력도 포함하여 완전한 기술 문서로 구성했습니다.
