package no.fdk.sparqlservice.kafka;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import no.fdk.concept.ConceptEvent;
import no.fdk.concept.ConceptEventType;
import no.fdk.dataservice.DataServiceEvent;
import no.fdk.dataservice.DataServiceEventType;
import no.fdk.dataset.DatasetEvent;
import no.fdk.dataset.DatasetEventType;
import no.fdk.event.EventEvent;
import no.fdk.event.EventEventType;
import no.fdk.informationmodel.InformationModelEvent;
import no.fdk.informationmodel.InformationModelEventType;
import no.fdk.service.ServiceEvent;
import no.fdk.service.ServiceEventType;
import no.fdk.sparqlservice.model.CatalogType;
import no.fdk.sparqlservice.service.ResourceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventCircuitBreakers {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventCircuitBreakers.class);
    private final ResourceService resourceService;

    @CircuitBreaker(name="service-event-cb")
    public void processServiceEvent(ConsumerRecord<String, ServiceEvent> record) {
        try {
            ServiceEvent event = record.value();
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(event.getFdkId().toString(), event.getTimestamp(), CatalogType.SERVICES);
            if(event.getType() == ServiceEventType.SERVICE_REASONED && hasHigherTimestamp) {
                resourceService.saveService(
                        event.getFdkId().toString(),
                        event.getGraph().toString(),
                        event.getTimestamp()
                );
            } else if(event.getType() == ServiceEventType.SERVICE_REMOVED && hasHigherTimestamp) {
                resourceService.deleteService(event.getFdkId().toString());
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event",  exception);
        } catch (Exception exception) {
            log.error("Error processing service message",  exception);
            throw exception;
        }
    }

    @CircuitBreaker(name="information-model-event-cb")
    public void processInformationModelEvent(ConsumerRecord<String, InformationModelEvent> record) {
        try {
            InformationModelEvent event = record.value();
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(event.getFdkId().toString(), event.getTimestamp(), CatalogType.INFORMATION_MODELS);
            if(event.getType() == InformationModelEventType.INFORMATION_MODEL_REASONED && hasHigherTimestamp) {
                resourceService.saveInformationModel(
                        event.getFdkId().toString(),
                        event.getGraph().toString(),
                        event.getTimestamp()
                );
            } else if(event.getType() == InformationModelEventType.INFORMATION_MODEL_REMOVED && hasHigherTimestamp) {
                resourceService.deleteInformationModel(event.getFdkId().toString());
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event",  exception);
        } catch (Exception exception) {
            log.error("Error processing information model message",  exception);
            throw exception;
        }
    }

    @CircuitBreaker(name="event-event-cb")
    public void processEventEvent(ConsumerRecord<String, EventEvent> record) {
        try {
            EventEvent kafkaEvent = record.value();
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(kafkaEvent.getFdkId().toString(), kafkaEvent.getTimestamp(), CatalogType.EVENTS);
            if(kafkaEvent.getType() == EventEventType.EVENT_REASONED && hasHigherTimestamp) {
                resourceService.saveEvent(
                        kafkaEvent.getFdkId().toString(),
                        kafkaEvent.getGraph().toString(),
                        kafkaEvent.getTimestamp()
                );
            } else if(kafkaEvent.getType() == EventEventType.EVENT_REMOVED && hasHigherTimestamp) {
                resourceService.deleteEvent(kafkaEvent.getFdkId().toString());
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event",  exception);
        } catch (Exception exception) {
            log.error("Error processing event message",  exception);
            throw exception;
        }
    }

    @CircuitBreaker(name="dataset-event-cb")
    public void processDatasetEvent(ConsumerRecord<String, DatasetEvent> record) {
        try {
            DatasetEvent event = record.value();
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(event.getFdkId().toString(), event.getTimestamp(), CatalogType.DATASETS);
            if(event.getType() == DatasetEventType.DATASET_REASONED && hasHigherTimestamp) {
                resourceService.saveDataset(
                        event.getFdkId().toString(),
                        event.getGraph().toString(),
                        event.getTimestamp()
                );
            } else if(event.getType() == DatasetEventType.DATASET_REMOVED && hasHigherTimestamp) {
                resourceService.deleteDataset(event.getFdkId().toString());
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event",  exception);
        } catch (Exception exception) {
            log.error("Error processing dataset message",  exception);
            throw exception;
        }
    }

    @CircuitBreaker(name="data-service-event-cb")
    public void processDataServiceEvent(ConsumerRecord<String, DataServiceEvent> record) {
        try {
            DataServiceEvent event = record.value();
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(event.getFdkId().toString(), event.getTimestamp(), CatalogType.DATA_SERVICES);
            if(event.getType() == DataServiceEventType.DATA_SERVICE_REASONED && hasHigherTimestamp) {
                resourceService.saveDataService(
                        event.getFdkId().toString(),
                        event.getGraph().toString(),
                        event.getTimestamp()
                );
            } else if(event.getType() == DataServiceEventType.DATA_SERVICE_REMOVED && hasHigherTimestamp) {
                resourceService.deleteDataService(event.getFdkId().toString());
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event",  exception);
        } catch (Exception exception) {
            log.error("Error processing data service message",  exception);
            throw exception;
        }
    }

    @CircuitBreaker(name="concept-event-cb")
    public void processConceptEvent(ConsumerRecord<String, ConceptEvent> record) {
        try {
            ConceptEvent event = record.value();
            boolean hasHigherTimestamp = resourceService.timestampIsHigherThanSaved(event.getFdkId().toString(), event.getTimestamp(), CatalogType.CONCEPTS);
            if(event.getType() == ConceptEventType.CONCEPT_REASONED && hasHigherTimestamp) {
                resourceService.saveConcept(
                        event.getFdkId().toString(),
                        event.getGraph().toString(),
                        event.getTimestamp()
                );
            } else if(event.getType() == ConceptEventType.CONCEPT_REMOVED && hasHigherTimestamp) {
                resourceService.deleteConcept(event.getFdkId().toString());
            }
        } catch (ClassCastException exception) {
            log.error("Skipping unknown event",  exception);
        } catch (Exception exception) {
            log.error("Error processing concept message",  exception);
            throw exception;
        }
    }
}
