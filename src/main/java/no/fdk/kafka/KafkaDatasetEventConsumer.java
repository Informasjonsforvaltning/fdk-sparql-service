package no.fdk.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fdk.configuration.FusekiConfiguration;
import no.fdk.dataset.DatasetEvent;
import no.fdk.dataset.DatasetEventType;
import no.fdk.model.fuseki.action.CatalogType;
import no.fdk.model.fuseki.action.CompactAction;
import no.fdk.service.UpdateService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaDatasetEventConsumer {
    private final UpdateService updateService;
    private final FusekiConfiguration fusekiConfiguration;
    private final CompactAction compactAction;

    private int graphsUpdated = 0;

    private void runCompact() {
        int MAX_UPDATES_BEFORE_COMPACT = 5000;
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
    public void listen(ConsumerRecord<String, DatasetEvent> record, Acknowledgment ack) {
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

}
