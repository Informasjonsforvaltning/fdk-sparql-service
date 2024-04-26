package no.fdk.sparqlservice.kafka;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import no.fdk.sparqlservice.service.ResourceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventCircuitBreakers {
    private final ResourceService resourceService;

    @CircuitBreaker(name="service-event-cb")
    public void processServiceEvent(ConsumerRecord<String, ServiceEvent> record) {
        ServiceEvent event = record.value();
        try {
            if(event.getType() == ServiceEventType.SERVICE_REASONED) {
                resourceService.saveService(
                        event.getFdkId().toString(),
                        event.getGraph().toString(),
                        event.getTimestamp()
                );
            } else if(event.getType() == ServiceEventType.SERVICE_REMOVED) {
                resourceService.deleteService(event.getFdkId().toString());
            }
        } catch (Exception exception) {
            log.error("Error processing service message",  exception);
            throw exception;
        }
    }

    @CircuitBreaker(name="information-model-event-cb")
    public void processInformationModelEvent(ConsumerRecord<String, InformationModelEvent> record) {
        InformationModelEvent event = record.value();
        try {
            if(event.getType() == InformationModelEventType.INFORMATION_MODEL_REASONED) {
                resourceService.saveInformationModel(
                        event.getFdkId().toString(),
                        event.getGraph().toString(),
                        event.getTimestamp()
                );
            } else if(event.getType() == InformationModelEventType.INFORMATION_MODEL_REMOVED) {
                resourceService.deleteInformationModel(event.getFdkId().toString());
            }
        } catch (Exception exception) {
            log.error("Error processing information model message",  exception);
            throw exception;
        }
    }

    @CircuitBreaker(name="event-event-cb")
    public void processEventEvent(ConsumerRecord<String, EventEvent> record) {
        EventEvent kafkaEvent = record.value();
        try {
            if(kafkaEvent.getType() == EventEventType.EVENT_REASONED) {
                resourceService.saveEvent(
                        kafkaEvent.getFdkId().toString(),
                        kafkaEvent.getGraph().toString(),
                        kafkaEvent.getTimestamp()
                );
            } else if(kafkaEvent.getType() == EventEventType.EVENT_REMOVED) {
                resourceService.deleteEvent(kafkaEvent.getFdkId().toString());
            }
        } catch (Exception exception) {
            log.error("Error processing event message",  exception);
            throw exception;
        }
    }

    @CircuitBreaker(name="dataset-event-cb")
    public void processDatasetEvent(ConsumerRecord<String, DatasetEvent> record) {
        DatasetEvent event = record.value();
        try {
            if(event.getType() == DatasetEventType.DATASET_REASONED) {
                resourceService.saveDataset(
                        event.getFdkId().toString(),
                        event.getGraph().toString(),
                        event.getTimestamp()
                );
            } else if(event.getType() == DatasetEventType.DATASET_REMOVED) {
                resourceService.deleteDataset(event.getFdkId().toString());
            }
        } catch (Exception exception) {
            log.error("Error processing dataset message",  exception);
            throw exception;
        }
    }

    @CircuitBreaker(name="data-service-event-cb")
    public void processDataServiceEvent(ConsumerRecord<String, DataServiceEvent> record) {
        DataServiceEvent event = record.value();
        try {
            if(event.getType() == DataServiceEventType.DATA_SERVICE_REASONED) {
                resourceService.saveDataService(
                        event.getFdkId().toString(),
                        event.getGraph().toString(),
                        event.getTimestamp()
                );
            } else if(event.getType() == DataServiceEventType.DATA_SERVICE_REMOVED) {
                resourceService.deleteDataService(event.getFdkId().toString());
            }
        } catch (Exception exception) {
            log.error("Error processing data service message",  exception);
            throw exception;
        }
    }

    @CircuitBreaker(name="concept-event-cb")
    public void processConceptEvent(ConsumerRecord<String, ConceptEvent> record) {
        ConceptEvent event = record.value();
        try {
            if(event.getType() == ConceptEventType.CONCEPT_REASONED) {
                resourceService.saveConcept(
                        event.getFdkId().toString(),
                        event.getGraph().toString(),
                        event.getTimestamp()
                );
            } else if(event.getType() == ConceptEventType.CONCEPT_REMOVED) {
                resourceService.deleteConcept(event.getFdkId().toString());
            }
        } catch (Exception exception) {
            log.error("Error processing concept message",  exception);
            throw exception;
        }
    }
}
