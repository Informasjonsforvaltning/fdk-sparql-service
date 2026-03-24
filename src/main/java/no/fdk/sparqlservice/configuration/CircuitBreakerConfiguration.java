package no.fdk.sparqlservice.configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {

    private static final String[] CIRCUIT_BREAKER_NAMES = {
            "service-event-cb",
            "information-model-event-cb",
            "concept-event-cb",
            "data-service-event-cb",
            "dataset-event-cb",
            "event-event-cb"
    };

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .failureRateThreshold(50)
                .permittedNumberOfCallsInHalfOpenState(3)
                .waitDurationInOpenState(Duration.ofMillis(60000))
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);
        for (String name : CIRCUIT_BREAKER_NAMES) {
            registry.circuitBreaker(name);
        }
        return registry;
    }
}
