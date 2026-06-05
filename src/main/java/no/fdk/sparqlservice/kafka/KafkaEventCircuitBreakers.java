package no.fdk.sparqlservice.kafka;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import no.fdk.sparqlservice.model.CatalogType;
import no.fdk.sparqlservice.service.ResourceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

import static no.fdk.sparqlservice.kafka.AvroRecordHelper.getOptionalString;
import static no.fdk.sparqlservice.kafka.AvroRecordHelper.getRequiredLong;
import static no.fdk.sparqlservice.kafka.AvroRecordHelper.getRequiredString;
import static no.fdk.sparqlservice.kafka.AvroRecordHelper.isType;

@Component
public class KafkaEventCircuitBreakers {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventCircuitBreakers.class);

    private final ResourceService resourceService;
    private final Map<CatalogType, CircuitBreaker> circuitBreakers;

    public KafkaEventCircuitBreakers(ResourceService resourceService, CircuitBreakerRegistry registry) {
        this.resourceService = resourceService;
        this.circuitBreakers = new EnumMap<>(CatalogType.class);
        for (CatalogType type : CatalogType.values()) {
            circuitBreakers.put(type, registry.circuitBreaker(type.circuitBreakerName()));
        }
    }

    public void processEvent(CatalogType type, ConsumerRecord<String, Object> record) {
        circuitBreakers.get(type).executeRunnable(() -> handleEvent(type, record));
    }

    private void handleEvent(CatalogType type, ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) {
                return;
            }
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            String catalogGraph = getOptionalString(event, "catalogGraph");
            if (fdkId == null || timestamp == null) {
                return;
            }
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, type);
            if (isType(event, type.reasonedEventType()) && hasHigherTimestamp && graph != null) {
                resourceService.save(type, fdkId, graph, timestamp, harvestRunId, catalogGraph);
            } else if (isType(event, type.removedEventType()) && hasHigherTimestamp) {
                resourceService.remove(type, fdkId, timestamp, harvestRunId);
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event", exception);
        } catch (Exception exception) {
            log.error("Error processing {} message", type.logLabel(), exception);
            throw exception;
        }
    }
}
