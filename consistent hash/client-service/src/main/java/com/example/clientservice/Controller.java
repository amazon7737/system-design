package com.example.clientservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class Controller {

    private final WebClient webClient;

    public Controller(WebClient webClient) {
        this.webClient = webClient;
    }


    @GetMapping("/api/hello")
    public Mono<String> call(@RequestParam("userId") String userId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/hello")
                    .queryParam("userId", userId)
                    .build())
                .retrieve()
                .bodyToMono(String.class);
    }
}
