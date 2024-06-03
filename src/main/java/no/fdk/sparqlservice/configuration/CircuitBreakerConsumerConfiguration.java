package no.fdk.sparqlservice.configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import no.fdk.sparqlservice.kafka.KafkaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class CircuitBreakerConsumerConfiguration {
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerConsumerConfiguration.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final KafkaManager kafkaManager;

    private void stateTransition(String cb, String listener) {
        circuitBreakerRegistry.circuitBreaker(cb).getEventPublisher().onStateTransition( event -> {
            switch(event.getStateTransition()) {
                case CLOSED_TO_OPEN, CLOSED_TO_FORCED_OPEN, HALF_OPEN_TO_OPEN -> kafkaManager.pause(listener);
                case OPEN_TO_HALF_OPEN, HALF_OPEN_TO_CLOSED, FORCED_OPEN_TO_CLOSED, FORCED_OPEN_TO_HALF_OPEN -> kafkaManager.resume(listener);
                default -> throw new IllegalStateException("Unknown transition state: " + event.getStateTransition());
            }
        });
    }

    @PostConstruct
    private void init() {
        log.debug("Configuring circuit breaker event listener");
        stateTransition("service-event-cb", "service-event-consumer");
        stateTransition("information-model-event-cb", "information-model-event-consumer");
        stateTransition("concept-event-cb", "concept-event-consumer");
        stateTransition("data-service-event-cb", "data-service-event-consumer");
        stateTransition("dataset-event-cb", "dataset-event-consumer");
        stateTransition("event-event-cb", "event-event-consumer");
    }
}
