package no.fdk.sparqlservice.kafka;

import lombok.RequiredArgsConstructor;
import no.fdk.harvest.HarvestEvent;
import no.fdk.harvest.HarvestPhase;
import no.fdk.harvest.DataType;
import no.fdk.sparqlservice.model.CatalogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class HarvestEventProducer {
    private static final Logger log = LoggerFactory.getLogger(HarvestEventProducer.class);
    private static final String HARVEST_EVENTS_TOPIC = "harvest-events";
    
    private final KafkaTemplate<String, HarvestEvent> kafkaTemplate;

    public void produceHarvestEvent(
            String harvestRunId,
            CatalogType catalogType,
            String fdkId,
            HarvestPhase phase,
            String startTime,
            String endTime,
            boolean success,
            String errorMessage
    ) {
        if (harvestRunId == null) {
            log.debug("Skipping harvest event production - no harvestRunId");
            return;
        }

        try {
            DataType dataType = mapCatalogTypeToDataType(catalogType);

            HarvestEvent event = HarvestEvent.newBuilder()
                    .setPhase(phase)
                    .setRunId(harvestRunId)
                    .setDataType(dataType)
                    .setFdkId(fdkId)
                    .setStartTime(startTime)
                    .setEndTime(endTime)
                    .setErrorMessage(success ? null : errorMessage)
                    .setDataSourceId(null)
                    .setDataSourceUrl(null)
                    .setAcceptHeader(null)
                    .setResourceUri(null)
                    .setChangedResourcesCount(null)
                    .setUnchangedResourcesCount(null)
                    .setRemovedResourcesCount(null)
                    .build();

            kafkaTemplate.send(HARVEST_EVENTS_TOPIC, harvestRunId, event);
            log.debug("Produced harvest event for runId: {}, phase: {}, dataType: {}, fdkId: {}", 
                    harvestRunId, phase, dataType, fdkId);
        } catch (Exception e) {
            log.error("Failed to produce harvest event for runId: {}, phase: {}", harvestRunId, phase, e);
        }
    }

    private DataType mapCatalogTypeToDataType(CatalogType catalogType) {
        return switch (catalogType) {
            case CONCEPTS -> DataType.concept;
            case DATASETS -> DataType.dataset;
            case INFORMATION_MODELS -> DataType.informationmodel;
            case DATA_SERVICES -> DataType.dataservice;
            case SERVICES -> DataType.publicService;
            case EVENTS -> DataType.event;
        };
    }
}




