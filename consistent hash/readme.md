# Consistent Hash Load Balancer 실습

이 프로젝트는 Spring Cloud Gateway와 Eureka를 사용하여 Consistent Hash 기반 로드 밸런싱을 구현한 마이크로서비스 아키텍처 실습입니다.

## 📋 프로젝트 개요

Consistent Hash 알고리즘을 사용하여 동일한 사용자 ID를 가진 요청이 항상 같은 백엔드 인스턴스로 라우팅되도록 하는 시스템입니다. 이를 통해 세션 친화성(Session Affinity)을 구현하고, 서버 추가/제거 시에도 최소한의 재분배만 발생하도록 합니다.

## 🏗️ 아키텍처

Client Service (8083)
↓
Gateway Service (8080) - Consistent Hash Load Balancer
↓
Backend Service (8081) - Multiple Instances
↑
Eureka Service (8761) - Service Discovery


## 🔧 기술 스택

- **Java 17**
- **Spring Boot 3.3.7**
- **Spring Cloud 2023.0.5**
- **Spring Cloud Gateway**
- **Spring Cloud Netflix Eureka**
- **Gradle**

## 📦 서비스 구성

### 1. Eureka Service (8761)
- 서비스 디스커버리 서버
- 모든 마이크로서비스가 등록되는 중앙 레지스트리

### 2. Gateway Service (8080)
- API Gateway 역할
- **Consistent Hash Load Balancer** 구현
- `/api/**` 경로를 백엔드 서비스로 라우팅
- 사용자 ID 기반으로 일관된 라우팅 제공

### 3. Backend Service (8081)
- 실제 비즈니스 로직을 처리하는 서비스
- 여러 인스턴스로 확장 가능
- `/api/hello` 엔드포인트 제공

### 4. Client Service (8083)
- 클라이언트 요청을 시뮬레이션하는 서비스
- WebClient를 사용하여 Gateway를 통해 Backend 호출

## 🔄 Consistent Hash 알고리즘

### 주요 특징
- **Virtual Nodes**: 100개의 가상 노드를 사용하여 균등한 분산
- **MD5 해싱**: 안정적인 해시 함수 사용
- **Ring 구조**: TreeMap을 사용한 원형 해시 링 구현
- **최소 재분배**: 노드 추가/제거 시 최소한의 키만 재분배

### 동작 원리
1. 사용자 ID를 해시하여 링 위의 위치 결정
2. 시계방향으로 가장 가까운 서버 노드 선택
3. 동일한 사용자 ID는 항상 같은 서버로 라우팅

## 🚀 실행 방법

### 1. 전체 빌드
```bash
./gradlew build
```

### 2. 서비스 실행 순서

#### 1) Eureka Service 실행
```bash
./gradlew :eureka-service:bootRun
```

#### 2) Backend Service 실행 (여러 인스턴스)
```bash
# 첫 번째 인스턴스
./gradlew :backend-service:bootRun

# 두 번째 인스턴스 (다른 터미널에서)
SERVER_PORT=8082 ./gradlew :backend-service:bootRun
```

#### 3) Gateway Service 실행
```bash
./gradlew :gateway-service:bootRun
```

#### 4) Client Service 실행
```bash
./gradlew :client-service:bootRun
```

## 📝 API 테스트

### 직접 Gateway 호출
```bash
# 동일한 userId로 여러 번 호출하여 일관성 확인
curl "http://localhost:8080/api/hello?userId=alice"
curl "http://localhost:8080/api/hello?userId=bob"
curl "http://localhost:8080/api/hello?userId=alice"  # alice는 항상 같은 인스턴스
```

### Client Service를 통한 호출
```bash
curl "http://localhost:8083/api/hello?userId=alice"
curl "http://localhost:8083/api/hello?userId=bob"
```

## 🔍 동작 확인

### 1. 로그 확인
Gateway Service 로그에서 다음과 같은 메시지를 확인할 수 있습니다:

Selected instance for userId=alice: localhost:8081
Selected instance for userId=bob: localhost:8082
Selected instance for userId=alice: localhost:8081 # 동일한 인스턴스


### 2. Eureka Dashboard
브라우저에서 `http://localhost:8761`에 접속하여 등록된 서비스들을 확인할 수 있습니다.

## 🧪 테스트 시나리오

### 1. 일관성 테스트
동일한 `userId`로 여러 번 요청하여 항상 같은 백엔드 인스턴스로 라우팅되는지 확인

### 2. 분산 테스트
다양한 `userId`로 요청하여 여러 백엔드 인스턴스에 분산되는지 확인

### 3. 확장성 테스트
백엔드 인스턴스를 추가/제거하면서 기존 사용자의 라우팅이 유지되는지 확인

## 📊 전체 프로세스

1. **Client Service** → `/api/hello?userId=abc123` 요청
2. **WebClient**가 Gateway Service의 `/api/hello` 호출
3. **Gateway Service**의 Consistent Hash LoadBalancer가 `userId`를 기반으로 백엔드 인스턴스 선택
4. **Backend Service** 인스턴스가 응답 처리
5. 동일한 `userId`로 재요청 시 항상 같은 인스턴스에 도달

## 🔧 설정 파일

### Gateway Service
- Eureka 클라이언트 설정
- Spring Cloud Gateway 라우팅 규칙
- 커스텀 로드 밸런서 설정

### Backend Service
- 다중 인스턴스 지원을 위한 랜덤 인스턴스 ID
- Eureka 등록 설정

## 🎯 학습 목표

1. **Consistent Hashing** 알고리즘 이해
2. **Spring Cloud Gateway** 커스텀 로드 밸런서 구현
3. **마이크로서비스 아키텍처**에서의 서비스 디스커버리
4. **세션 친화성(Session Affinity)** 구현 방법
5. **분산 시스템**에서의 로드 밸런싱 전략

## 🚨 주의사항

- 서비스 실행 순서를 지켜주세요 (Eureka → Backend → Gateway → Client)
- 포트 충돌이 발생하지 않도록 확인해주세요
- 백엔드 서비스의 여러 인스턴스 실행 시 다른 포트를 사용해주세요

## 📚 참고 자료

- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix)
- [Consistent Hashing Algorithm](https://en.wikipedia.org/wiki/Consistent_hashing)
