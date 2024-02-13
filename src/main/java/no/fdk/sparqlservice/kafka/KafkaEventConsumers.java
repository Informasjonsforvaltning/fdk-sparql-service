package no.fdk.sparqlservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fdk.concept.ConceptEvent;
import no.fdk.concept.ConceptEventType;
import no.fdk.event.EventEvent;
import no.fdk.event.EventEventType;
import no.fdk.informationmodels.InformationModelEvent;
import no.fdk.informationmodels.InformationModelEventType;
import no.fdk.service.ServiceEvent;
import no.fdk.service.ServiceEventType;
import no.fdk.dataservice.DataServiceEvent;
import no.fdk.dataservice.DataServiceEventType;
import no.fdk.dataset.DatasetEvent;
import no.fdk.dataset.DatasetEventType;
import no.fdk.sparqlservice.service.ResourceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumers {
    private final ResourceService resourceService;

    @KafkaListener(
            topics = "service-events",
            groupId = "fdk-sparql-service",
            concurrency = "4",
            containerFactory = "serviceListenerContainerFactory"
    )
    public void serviceListener(ConsumerRecord<String, ServiceEvent> record, Acknowledgment ack) {
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

            ack.acknowledge();
        } catch (Exception exception) {
            log.error("Error processing service message",  exception);
        }
    }

    @KafkaListener(
            topics = "information-model-events",
            groupId = "fdk-sparql-service",
            concurrency = "4",
            containerFactory = "infoModelListenerContainerFactory"
    )
    public void infoModelListener(ConsumerRecord<String, InformationModelEvent> record, Acknowledgment ack) {
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

            ack.acknowledge();
        } catch (Exception exception) {
            log.error("Error processing information model message",  exception);
        }
    }

    @KafkaListener(
            topics = "event-events",
            groupId = "fdk-sparql-service",
            concurrency = "4",
            containerFactory = "eventListenerContainerFactory"
    )
    public void eventListener(ConsumerRecord<String, EventEvent> record, Acknowledgment ack) {
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

            ack.acknowledge();
        } catch (Exception exception) {
            log.error("Error processing event message",  exception);
        }
    }

    @KafkaListener(
            topics = "dataset-events",
            groupId = "fdk-sparql-service",
            concurrency = "4",
            containerFactory = "datasetListenerContainerFactory"
    )
    public void datasetListener(ConsumerRecord<String, DatasetEvent> record, Acknowledgment ack) {
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

            ack.acknowledge();
        } catch (Exception exception) {
            log.error("Error processing dataset message",  exception);
        }
    }

    @KafkaListener(
            topics = "data-service-events",
            groupId = "fdk-sparql-service",
            concurrency = "4",
            containerFactory = "dataServiceListenerContainerFactory"
    )
    public void dataServiceListener(ConsumerRecord<String, DataServiceEvent> record, Acknowledgment ack) {
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

            ack.acknowledge();
        } catch (Exception exception) {
            log.error("Error processing data service message",  exception);
        }
    }

    @KafkaListener(
            topics = "concept-events",
            groupId = "fdk-sparql-service",
            concurrency = "4",
            containerFactory = "conceptListenerContainerFactory"
    )
    public void conceptListener(ConsumerRecord<String, ConceptEvent> record, Acknowledgment ack) {
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

            ack.acknowledge();
        } catch (Exception exception) {
            log.error("Error processing concept message",  exception);
        }
    }

}
