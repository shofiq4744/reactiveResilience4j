package com.example.springwebflax.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4jConfig {

	@Bean
	public CircuitBreakerRegistry getCircuitBreaker() {
		return  CircuitBreakerRegistry.ofDefaults();
	}
}
