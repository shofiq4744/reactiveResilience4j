package com.example.springwebflax.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.netty.channel.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.concurrent.TimeoutException;


@Service
public class CommentService {

    public static final Logger LOGGER = LoggerFactory.getLogger(CommentService.class);

    CircuitBreakerRegistry circuitBreakerRegistry;
    WebClient webClient;

    @Autowired
    public  CommentService(WebClient webClient,
                           CircuitBreakerRegistry circuitBreakerRegistry){
        this.webClient = webClient;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public Mono<Object> getComments(String uri){
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.find("webclient").get();
        return webClient.get()
                .uri(uri)
                .exchange()
                .timeout(Duration.ofMillis(1000))
                .flatMap(clientResponse -> {
                   /* if (clientResponse.statusCode().isError()) {
                        throw new RuntimeException("Error");
                    }
                    System.out.println(String.format(
                            "Thread %s", Thread.currentThread().getName()));*/
                    return clientResponse.bodyToMono(Object.class);
                })
                .onErrorResume(throwable -> {
                    if (throwable instanceof TimeoutException ||
                            throwable instanceof io.netty.handler.timeout.TimeoutException ||
                            throwable instanceof ConnectTimeoutException) {
                        LOGGER.error("timeout happened");
                    }
                    return Mono.error(throwable);
                })
                .doOnSubscribe(sub->{
                    System.out.println("Subscribe start");
                })
                .doOnError(err->{
                   // System.out.println("error");
                })
                .doFinally(fn->{
                   // System.out.println("webclient final");
                })
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
    }
}
