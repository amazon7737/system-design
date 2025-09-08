### 처리량 제어 필터
- https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-mvc/filters/ratelimiter.html

### 블룸필터
- https://gngsn.tistory.com/201

- 기본 원리
  - n개의 hash 함수를 이용하여 dirty flag set
  - n개 모두 set되어 있어야 존재함

- 용도
  - 존재 여부 확인용
  - 비트단위 연산으로 가능
  - 없는데 있다고 할 경우 존재
  - hash 함수 증설

- 변종 버전 많음 (활용하기 나름)
  - 70대 나온거라 대체품도 있음
  - e.g. 리본 필터(살펴보진 않음)

- 기타
  - hash의 결과가 중복되어도 상관없음

- 단점
  - 삭제 불가
- 절대 없음 판단할때만 사용하여야함
