package no.fdk.sparqlservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fdk.concept.ConceptEvent;
import no.fdk.concept.ConceptEventType;
import no.fdk.sparqlservice.configuration.FusekiConfiguration;
import no.fdk.dataservice.DataServiceEvent;
import no.fdk.dataservice.DataServiceEventType;
import no.fdk.dataset.DatasetEvent;
import no.fdk.dataset.DatasetEventType;
import no.fdk.sparqlservice.model.CatalogType;
import no.fdk.sparqlservice.fuseki.action.CompactAction;
import no.fdk.sparqlservice.service.UpdateService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumers {
    private final UpdateService updateService;
    private final FusekiConfiguration fusekiConfiguration;
    private final CompactAction compactAction;

    private int graphsUpdated = 0;

    private void runCompact() {
        int MAX_UPDATES_BEFORE_COMPACT = 1000;
        if (graphsUpdated > MAX_UPDATES_BEFORE_COMPACT) {
            graphsUpdated = 0;
            String datasetPath = "%s/%s".formatted(fusekiConfiguration.getStorePath(), fusekiConfiguration.getDatasetName());
            compactAction.compact(datasetPath);
        }
    }

    @KafkaListener(
            topics = "dataset-events",
            groupId = "fdk-sparql-service",
            containerFactory = "datasetListenerContainerFactory"
    )
    public void datasetListener(ConsumerRecord<String, DatasetEvent> record, Acknowledgment ack) {
        DatasetEvent event = record.value();
        try {
            if(event.getType() == DatasetEventType.DATASET_REASONED) {
                updateService.updateGraph(CatalogType.DATASETS, event.getFdkId().toString(), event.getGraph().toString());
                graphsUpdated++;
            } else if(event.getType() == DatasetEventType.DATASET_REMOVED) {
                updateService.deleteGraph(CatalogType.DATASETS, event.getFdkId().toString());
            }

            ack.acknowledge();
            runCompact();
        } catch (Exception exception) {
            log.error("Error processing dataset message",  exception);
        }
    }

    @KafkaListener(
            topics = "data-service-events",
            groupId = "fdk-sparql-service",
            containerFactory = "dataServiceListenerContainerFactory"
    )
    public void dataServiceListener(ConsumerRecord<String, DataServiceEvent> record, Acknowledgment ack) {
        DataServiceEvent event = record.value();
        try {
            if(event.getType() == DataServiceEventType.DATA_SERVICE_REASONED) {
                updateService.updateGraph(CatalogType.DATA_SERVICES, event.getFdkId().toString(), event.getGraph().toString());
                graphsUpdated++;
            } else if(event.getType() == DataServiceEventType.DATA_SERVICE_REMOVED) {
                updateService.deleteGraph(CatalogType.DATA_SERVICES, event.getFdkId().toString());
            }

            ack.acknowledge();
            runCompact();
        } catch (Exception exception) {
            log.error("Error processing data service message",  exception);
        }
    }

    @KafkaListener(
            topics = "concept-events",
            groupId = "fdk-sparql-service",
            containerFactory = "conceptListenerContainerFactory"
    )
    public void conceptListener(ConsumerRecord<String, ConceptEvent> record, Acknowledgment ack) {
        ConceptEvent event = record.value();
        try {
            if(event.getType() == ConceptEventType.CONCEPT_REASONED) {
                updateService.updateGraph(CatalogType.CONCEPTS, event.getFdkId().toString(), event.getGraph().toString());
                graphsUpdated++;
            } else if(event.getType() == ConceptEventType.CONCEPT_REMOVED) {
                updateService.deleteGraph(CatalogType.CONCEPTS, event.getFdkId().toString());
            }

            ack.acknowledge();
            runCompact();
        } catch (Exception exception) {
            log.error("Error processing concept message",  exception);
        }
    }

}
