package no.fdk.sparqlservice.kafka;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import no.fdk.sparqlservice.model.CatalogType;
import no.fdk.sparqlservice.service.ResourceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static no.fdk.sparqlservice.kafka.AvroRecordHelper.getOptionalString;
import static no.fdk.sparqlservice.kafka.AvroRecordHelper.getRequiredLong;
import static no.fdk.sparqlservice.kafka.AvroRecordHelper.getRequiredString;
import static no.fdk.sparqlservice.kafka.AvroRecordHelper.isType;

@Component
@RequiredArgsConstructor
public class KafkaEventCircuitBreakers {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventCircuitBreakers.class);
    private final ResourceService resourceService;

    @CircuitBreaker(name = "service-event-cb")
    public void processServiceEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) return;
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            if (fdkId == null || timestamp == null) return;
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, CatalogType.SERVICES);
            if (isType(event, "SERVICE_REASONED") && hasHigherTimestamp && graph != null) {
                resourceService.saveService(fdkId, graph, timestamp, harvestRunId);
            } else if (isType(event, "SERVICE_REMOVED") && hasHigherTimestamp) {
                resourceService.removeService(fdkId, timestamp, harvestRunId);
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event", exception);
        } catch (Exception exception) {
            log.error("Error processing service message", exception);
            throw exception;
        }
    }

    @CircuitBreaker(name = "information-model-event-cb")
    public void processInformationModelEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) return;
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            if (fdkId == null || timestamp == null) return;
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, CatalogType.INFORMATION_MODELS);
            if (isType(event, "INFORMATION_MODEL_REASONED") && hasHigherTimestamp && graph != null) {
                resourceService.saveInformationModel(fdkId, graph, timestamp, harvestRunId);
            } else if (isType(event, "INFORMATION_MODEL_REMOVED") && hasHigherTimestamp) {
                resourceService.removeInformationModel(fdkId, timestamp, harvestRunId);
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event", exception);
        } catch (Exception exception) {
            log.error("Error processing information model message", exception);
            throw exception;
        }
    }

    @CircuitBreaker(name = "event-event-cb")
    public void processEventEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) return;
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            if (fdkId == null || timestamp == null) return;
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, CatalogType.EVENTS);
            if (isType(event, "EVENT_REASONED") && hasHigherTimestamp && graph != null) {
                resourceService.saveEvent(fdkId, graph, timestamp, harvestRunId);
            } else if (isType(event, "EVENT_REMOVED") && hasHigherTimestamp) {
                resourceService.removeEvent(fdkId, timestamp, harvestRunId);
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event", exception);
        } catch (Exception exception) {
            log.error("Error processing event message", exception);
            throw exception;
        }
    }

    @CircuitBreaker(name = "dataset-event-cb")
    public void processDatasetEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) return;
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            if (fdkId == null || timestamp == null) return;
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, CatalogType.DATASETS);
            if (isType(event, "DATASET_REASONED") && hasHigherTimestamp && graph != null) {
                resourceService.saveDataset(fdkId, graph, timestamp, harvestRunId);
            } else if (isType(event, "DATASET_REMOVED") && hasHigherTimestamp) {
                resourceService.removeDataset(fdkId, timestamp, harvestRunId);
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event", exception);
        } catch (Exception exception) {
            log.error("Error processing dataset message", exception);
            throw exception;
        }
    }

    @CircuitBreaker(name = "data-service-event-cb")
    public void processDataServiceEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) return;
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            if (fdkId == null || timestamp == null) return;
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, CatalogType.DATA_SERVICES);
            if (isType(event, "DATA_SERVICE_REASONED") && hasHigherTimestamp && graph != null) {
                resourceService.saveDataService(fdkId, graph, timestamp, harvestRunId);
            } else if (isType(event, "DATA_SERVICE_REMOVED") && hasHigherTimestamp) {
                resourceService.removeDataService(fdkId, timestamp, harvestRunId);
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event", exception);
        } catch (Exception exception) {
            log.error("Error processing data service message", exception);
            throw exception;
        }
    }

    @CircuitBreaker(name = "concept-event-cb")
    public void processConceptEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) return;
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            if (fdkId == null || timestamp == null) return;
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, CatalogType.CONCEPTS);
            if (isType(event, "CONCEPT_REASONED") && hasHigherTimestamp && graph != null) {
                resourceService.saveConcept(fdkId, graph, timestamp, harvestRunId);
            } else if (isType(event, "CONCEPT_REMOVED") && hasHigherTimestamp) {
                resourceService.removeConcept(fdkId, timestamp, harvestRunId);
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event", exception);
        } catch (Exception exception) {
            log.error("Error processing concept message", exception);
            throw exception;
        }
    }
}
