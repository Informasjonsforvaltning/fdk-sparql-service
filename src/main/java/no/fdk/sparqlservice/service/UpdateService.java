package no.fdk.sparqlservice.service;

import lombok.RequiredArgsConstructor;
import no.fdk.harvest.HarvestPhase;
import no.fdk.sparqlservice.kafka.HarvestEventProducer;
import no.fdk.sparqlservice.model.CatalogResource;
import no.fdk.sparqlservice.model.CatalogType;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UpdateService {
    private static final Logger log = LoggerFactory.getLogger(UpdateService.class);

    private final FusekiGraphClient fusekiGraphClient;
    private final ResourceService resourceService;
    private final SyncDataService syncDataService;
    private final HarvestEventProducer harvestEventProducer;

    public void updateFusekiForChanged(CatalogType type) {
        List<String> ids = resourceService.findNonSynced(type, PageRequest.of(0, 500));
        if (!ids.isEmpty()) {
            log.info("updating fuseki with {} {} graphs", ids.size(), type.logLabel());
        }
        for (String fdkId : ids) {
            Optional<? extends CatalogResource> resourceWrap = resourceService.findById(type, fdkId);
            if (resourceWrap.isEmpty()) {
                continue;
            }
            CatalogResource resource = resourceWrap.get();
            boolean success = false;
            String errorMessage = null;
            String startTime = Instant.now().toString();
            String endTime = null;
            try {
                if (resource.isRemoved()) {
                    success = fusekiGraphClient.removeByIdentifier(resource.getId());
                } else {
                    Model graph = fusekiGraphClient.graphAsModelForInsert(resource.getGraph(), resource.getCatalogGraph());
                    try {
                        success = fusekiGraphClient.insert(graph);
                    } finally {
                        if (graph != null) {
                            graph.close();
                        }
                    }
                }
                endTime = Instant.now().toString();
                if (success) {
                    syncDataService.updateLatestSync(type, resource.getId(), resource.getTimestamp());
                } else {
                    errorMessage = "SPARQL update failed for " + type.logLabel() + ": " + fdkId;
                }
            } catch (Exception e) {
                endTime = Instant.now().toString();
                errorMessage = "SPARQL update exception for " + type.logLabel() + ": " + fdkId + " - " + e.getMessage();
                log.error("Error updating {}", type.logLabel(), e);
            }

            if (resource.isPendingHarvestEvent() && resource.getHarvestRunId() != null) {
                harvestEventProducer.produceHarvestEvent(
                        resource.getHarvestRunId(),
                        type,
                        resource.getId(),
                        HarvestPhase.SPARQL_PROCESSING,
                        startTime,
                        endTime,
                        success,
                        errorMessage
                );
                resourceService.clearPendingHarvestEvent(resource.getId(), type);
            }
        }
    }

    public void updateFuseki() {
        fusekiGraphClient.initFusekiGraphIfEmpty();
        try {
            for (CatalogType type : CatalogType.values()) {
                updateFusekiForChanged(type);
            }
            log.debug("Fuseki synchronization complete");
        } catch (Exception exception) {
            log.error("Fuseki synchronization was aborted", exception);
        }
    }

    @Scheduled(initialDelay = 5, fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    private void scheduledSynchronization() {
        log.debug("Starting fuseki synchronization");
        updateFuseki();
    }
}
