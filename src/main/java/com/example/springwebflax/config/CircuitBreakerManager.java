package com.example.springwebflax.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class CircuitBreakerManager implements ApplicationRunner {

	public static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerManager.class);
	private CircuitBreakerRegistry circuitBreakerRegistry;

	public CircuitBreakerManager(CircuitBreakerRegistry circuitBreakerRegistry) {
		this.circuitBreakerRegistry = circuitBreakerRegistry;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
				.failureRateThreshold(60)
				.slowCallRateThreshold(30)
				.slowCallDurationThreshold(Duration.ofMillis(600))
				.permittedNumberOfCallsInHalfOpenState(1)
				.slidingWindowType(
						CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
				.slidingWindowSize(10)
				.minimumNumberOfCalls(3)
				.waitDurationInOpenState(Duration.ofMillis(100000000))
				.automaticTransitionFromOpenToHalfOpenEnabled(false)
				.recordException(throwable -> true)//all throwable are increase failure rate
				.build();

		CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("webclient", circuitBreakerConfig);
		circuitBreaker.getEventPublisher().onStateTransition(event ->
				LOGGER.info(event.getStateTransition().toString()));
	}
}
