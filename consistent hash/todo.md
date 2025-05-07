
### 전체 실행 흐름

1. client-service → /test?userId=abc123 요청
2. WebClient가 X-Hash-Key: abc123 포함해서 gateway-service의 /api/hello 호출
3. gateway-service의 Consistent Hash LoadBalancer가 같은 userId는 같은 인스턴스로 라우팅
4. backend-service 인스턴스가 응답
5. 동일한 userId로 요청하면 항상 같은 인스턴스에 도달

