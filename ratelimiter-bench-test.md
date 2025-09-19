### Apache Bench 의 ab를 통해서 요청량 제한 테스트 진행
앞서, `gateway-service` 와 `backend-service` 를 구성하여, ratelimiter 를 설정하였다. 직접 테스트해보며, 동시성 요청을 어떻게 ratelimiter가 처리하는지 직접 확인해보자.

```
ab -n [총 요청 수] -c [동시 요청 수] [테스트할 URL]
```

- `-c`는 동시 접속자 수를 설정할 수 있다.
- `-n` 는 총 요청수 이다.
- `-s` 는 소켓 타임아웃(sec) 을 설정할 수 있다.

- `ab -n 100 -c 20 http://localhost:8080/request` 을 실행하여 총 100번의 요청을 동시에 20개씩 보내었다.

<img width="526" height="184" alt="image" src="https://github.com/user-attachments/assets/00dbbe19-cd9e-44b4-b706-e42214231070" />

- 결과로, 100개의 요청중 95개의 요청이 실패하였다. Filter에서 설정된 5개 요청 이후 동시적인 요청들은 모두 거부하도록 설정해서 인듯 하다.

- 다음으로 더 큰 요청량들도 마찬가지의 결과를 보이는지 궁금하였다.
- `ab -n 10000 -c 20 http://localhost:8080/request` 을 실행하여 총 10000번의 요청으로 늘려보았다.

<img width="523" height="193" alt="image" src="https://github.com/user-attachments/assets/e793f774-20d8-4ada-a1bb-385b27749634" />

- 마찬가지로 9995번의 요청이 실패하였고 5개의 요청이 성공하였다. 똑같은 결과가 나타남을 알 수 있다.

- 부하 테스트의 동시성 레벨을 20으로 고정해두고, Filter의 최대 요청량 값을 변경해보았다.

<img width="521" height="196" alt="image" src="https://github.com/user-attachments/assets/61b0e75b-6ab2-4dec-87e3-41749acde8f0" />

- 동시성 레벨을 20을 유지한상태로, RateLimiter의 설정값을 `limitForPeriod(200)` 으로 설정하고, `limitRefreshPeriod(Duration.ofSeconds(10))` 10초마다 복구되도록 하였다.
  - 그리고 부하를 10000개의 요청을 주었다.

<img width="1909" height="103" alt="image" src="https://github.com/user-attachments/assets/2f732c1e-22f2-451b-bc69-9612d52d8043" />

- 소켓 연결이 리셋되는 현상들이 발생하여, 요청이 소실된 것을 볼 수 있다.

<img width="1210" height="1040" alt="image" src="https://github.com/user-attachments/assets/22c97743-c1b2-4315-852c-f186a02dc97d" />

- `backend-service` 의 스레드풀은 열심히 동작한것을 시간대를 비교해보면 알 수 있다. 일부 요청들이 소실되는 것이다.

- 요청을 100,000 으로 늘려보았다.

<img width="295" height="75" alt="image" src="https://github.com/user-attachments/assets/e6567fa5-6a77-451e-987e-089f85479a70" />

- `apr_socket-recv: Operation timed out (60)` 메시지는 서버에서 응답을 제대로 돌려주지 못하거나, 동시에 요청이 몰려서 소켓 응답을 받지 못할 때 발생한다.
- RateLimiter를 통해서 429 처리가 많이 나오고, 남은 요청들은 타임아웃 처리된 것이다.
- RateLimiter에 의해서 일부 요청은 의도적으로 거절되는 것이다.

```
ab -n 100000 -c 20 -s 600 http://localhost:8080/request
```
- 타임아웃 설정이 문제인가 싶어, 타임아웃을 600s를 주고 테스트해보았다.

<img width="292" height="69" alt="image" src="https://github.com/user-attachments/assets/411219f2-9338-4db5-af37-438461fc682b" />

- 여전히 타임아웃 현상이 발생하는 것을 확인할 수 있다. 성공된 요청은 총 요청량에 16% 정도가 처리되었다.

<img width="429" height="147" alt="image" src="https://github.com/user-attachments/assets/7bca5b33-3d81-48b6-b0aa-09fdf220a357" />

- 100% 를 보면 6708 의 요청이 429 거절을 당한것을 확인할 수 있다.
- RateLimiter를 설정해두면, 최대 부하량을 넘는 수치들은 대부분 거절시켜 서버가 장애가 나는것을 방지한다. 사용자의 경험은 떨어질 수 있으나 서버의 전체 장애로 서비스가 멈추는 일은 줄일 수 있는 것이다.
- 서비스마다, 설정값이 다를테지만 상황에 맞는 적절한 설정법이 필요할듯 하다.
  - 처음 테스트 환경에서 최대 요청량을 5로 제한하고 복구시간을 1초로 잡았을때는 5라는 수치보다 훨씬 높은 10000정도의 요청량이 들어왔을때, 소켓 연결이 끊어저 버리는 상황이 발생하였다.
  - 최대 요청량을 200으로 변경하고 복구시간을 10초로 잡았을때는 10000의 요청량이 들어왔을때, 소켓 연결이 끊어지는 로그는 발견되지 않았다. 다만 Ratelimiter가 직접 429 응답을 처리하여 거절되는 요청이 많았다.
  - 테스트를 진행해보았을때, 최대 요청량이 실제로 들어오는 동시적 요청량과 비슷할 수록 429 응답을 내리는 요청이 적을 것이고, 소켓 연결이 끊어지는 현상이 줄어들 것임을 알 수 있다.
