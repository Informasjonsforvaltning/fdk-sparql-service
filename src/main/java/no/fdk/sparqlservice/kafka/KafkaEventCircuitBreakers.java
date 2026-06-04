package no.fdk.sparqlservice.kafka;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
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
public class KafkaEventCircuitBreakers {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventCircuitBreakers.class);

    private final ResourceService resourceService;
    private final CircuitBreaker serviceEventCb;
    private final CircuitBreaker informationModelEventCb;
    private final CircuitBreaker eventEventCb;
    private final CircuitBreaker datasetEventCb;
    private final CircuitBreaker dataServiceEventCb;
    private final CircuitBreaker conceptEventCb;

    public KafkaEventCircuitBreakers(ResourceService resourceService, CircuitBreakerRegistry registry) {
        this.resourceService = resourceService;
        this.serviceEventCb = registry.circuitBreaker("service-event-cb");
        this.informationModelEventCb = registry.circuitBreaker("information-model-event-cb");
        this.eventEventCb = registry.circuitBreaker("event-event-cb");
        this.datasetEventCb = registry.circuitBreaker("dataset-event-cb");
        this.dataServiceEventCb = registry.circuitBreaker("data-service-event-cb");
        this.conceptEventCb = registry.circuitBreaker("concept-event-cb");
    }

    public void processServiceEvent(ConsumerRecord<String, Object> record) {
        serviceEventCb.executeRunnable(() -> handleServiceEvent(record));
    }

    public void processInformationModelEvent(ConsumerRecord<String, Object> record) {
        informationModelEventCb.executeRunnable(() -> handleInformationModelEvent(record));
    }

    public void processEventEvent(ConsumerRecord<String, Object> record) {
        eventEventCb.executeRunnable(() -> handleEventEvent(record));
    }

    public void processDatasetEvent(ConsumerRecord<String, Object> record) {
        datasetEventCb.executeRunnable(() -> handleDatasetEvent(record));
    }

    public void processDataServiceEvent(ConsumerRecord<String, Object> record) {
        dataServiceEventCb.executeRunnable(() -> handleDataServiceEvent(record));
    }

    public void processConceptEvent(ConsumerRecord<String, Object> record) {
        conceptEventCb.executeRunnable(() -> handleConceptEvent(record));
    }

    private void handleServiceEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) return;
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            String catalogGraph = getOptionalString(event, "catalogGraph");
            if (fdkId == null || timestamp == null) return;
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, CatalogType.SERVICES);
            if (isType(event, "SERVICE_REASONED") && hasHigherTimestamp && graph != null) {
                resourceService.saveService(fdkId, graph, timestamp, harvestRunId, catalogGraph);
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

    private void handleInformationModelEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) return;
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            String catalogGraph = getOptionalString(event, "catalogGraph");
            if (fdkId == null || timestamp == null) return;
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, CatalogType.INFORMATION_MODELS);
            if (isType(event, "INFORMATION_MODEL_REASONED") && hasHigherTimestamp && graph != null) {
                resourceService.saveInformationModel(fdkId, graph, timestamp, harvestRunId, catalogGraph);
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

    private void handleEventEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) return;
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            String catalogGraph = getOptionalString(event, "catalogGraph");
            if (fdkId == null || timestamp == null) return;
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, CatalogType.EVENTS);
            if (isType(event, "EVENT_REASONED") && hasHigherTimestamp && graph != null) {
                resourceService.saveEvent(fdkId, graph, timestamp, harvestRunId, catalogGraph);
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

    private void handleDatasetEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) return;
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            String catalogGraph = getOptionalString(event, "catalogGraph");
            if (fdkId == null || timestamp == null) return;
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, CatalogType.DATASETS);
            if (isType(event, "DATASET_REASONED") && hasHigherTimestamp && graph != null) {
                resourceService.saveDataset(fdkId, graph, timestamp, harvestRunId, catalogGraph);
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

    private void handleDataServiceEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) return;
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            String catalogGraph = getOptionalString(event, "catalogGraph");
            if (fdkId == null || timestamp == null) return;
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, CatalogType.DATA_SERVICES);
            if (isType(event, "DATA_SERVICE_REASONED") && hasHigherTimestamp && graph != null) {
                resourceService.saveDataService(fdkId, graph, timestamp, harvestRunId, catalogGraph);
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

    private void handleConceptEvent(ConsumerRecord<String, Object> record) {
        try {
            Object event = record.value();
            if (event == null) return;
            String harvestRunId = getOptionalString(event, "harvestRunId");
            String fdkId = getRequiredString(event, "fdkId");
            String graph = getRequiredString(event, "graph");
            Long timestamp = getRequiredLong(event, "timestamp");
            String catalogGraph = getOptionalString(event, "catalogGraph");
            if (fdkId == null || timestamp == null) return;
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(fdkId, timestamp, CatalogType.CONCEPTS);
            if (isType(event, "CONCEPT_REASONED") && hasHigherTimestamp && graph != null) {
                resourceService.saveConcept(fdkId, graph, timestamp, harvestRunId, catalogGraph);
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
