package no.fdk.sparqlservice.service;

import lombok.RequiredArgsConstructor;
import no.fdk.sparqlservice.configuration.FusekiConfiguration;
import no.fdk.sparqlservice.configuration.HarvestGraphProperties;
import no.fdk.sparqlservice.fuseki.action.CompactAction;
import no.fdk.sparqlservice.model.*;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb2.TDB2Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UpdateService {
    private static final Logger log = LoggerFactory.getLogger(UpdateService.class);
    private final HarvestGraphProperties graphProperties;
    private final FusekiConfiguration fusekiConfiguration;
    private final CompactAction compactAction;
    private final ResourceService resourceService;
    private final SyncDataService syncDataService;

    private final int COMPACTION_FREQUENCY = 100;

    private void updateGraph(String graphName, byte[] graph) {
        File dbDir = Paths.get(fusekiConfiguration.getStorePath() + "/" + fusekiConfiguration.getDatasetName()).toFile();
        org.apache.jena.query.Dataset dbDataset = TDB2Factory.connectDataset(dbDir.getAbsolutePath());
        try {
            dbDataset.begin(ReadWrite.WRITE);
            Model dbModel = dbDataset.getNamedModel(graphName);
            RDFDataMgr.read(dbModel, new ByteArrayInputStream(graph), Lang.TURTLE);
            dbDataset.commit();
        } catch (Exception exception) {
            log.error("update of graph {} failed", graphName, exception);
            throw exception;
        } finally {
            dbDataset.end();
        }
    }

    private void deleteGraph(String graphName) {
        File dbDir = Paths.get("store/fdk").toFile();
        org.apache.jena.query.Dataset dbDataset = TDB2Factory.connectDataset(dbDir.getAbsolutePath());
        try {
            dbDataset.begin(ReadWrite.WRITE);
            dbDataset.removeNamedModel(graphName);
            dbDataset.commit();
        } catch (Exception exception) {
            log.error("delete of graph {} failed", graphName, exception);
        } finally {
            dbDataset.end();
        }
    }

    public void updateFusekiForChangedConcepts() {
        List<Concept> concepts = resourceService.findNonSyncedConcepts(PageRequest.of(0, 500));
        log.debug("updating fuseki with {} concepts", concepts.size());
        for (int i = 0; i < concepts.size(); i++) {
            Concept concept = concepts.get(i);
            String graphName = graphProperties.getConcepts() + concept.getId();

            if (i % COMPACTION_FREQUENCY == 0) {
                runCompaction();
            }

            if (concept.isRemoved()) {
                deleteGraph(graphName);
            } else {
                updateGraph(graphName, concept.getGraph());
            }
            syncDataService.updateLatestSync(CatalogType.CONCEPTS, concept.getId(), concept.getTimestamp());
        }
    }

    public void updateFusekiForChangedDataServices() {
        List<DataService> dataServices = resourceService.findNonSyncedDataServices(PageRequest.of(0, 500));
        log.debug("updating fuseki with {} data services", dataServices.size());
        for (int i = 0; i < dataServices.size(); i++) {
            DataService dataService = dataServices.get(i);
            String graphName = graphProperties.getDataservices() + dataService.getId();

            if (i % COMPACTION_FREQUENCY == 0) {
                runCompaction();
            }

            if (dataService.isRemoved()) {
                deleteGraph(graphName);
            } else {
                updateGraph(graphName, dataService.getGraph());
            }
            syncDataService.updateLatestSync(CatalogType.DATA_SERVICES, dataService.getId(), dataService.getTimestamp());
        }
    }

    public void updateFusekiForChangedDatasets() {
        List<Dataset> datasets = resourceService.findNonSyncedDatasets(PageRequest.of(0, 500));
        log.debug("updating fuseki with {} datasets", datasets.size());
        for (int i = 0; i < datasets.size(); i++) {
            Dataset dataset = datasets.get(i);
            String graphName = graphProperties.getDatasets() + dataset.getId();

            if (i % COMPACTION_FREQUENCY == 0) {
                runCompaction();
            }

            if (dataset.isRemoved()) {
                deleteGraph(graphName);
            } else {
                updateGraph(graphName, dataset.getGraph());
            }
            syncDataService.updateLatestSync(CatalogType.DATASETS, dataset.getId(), dataset.getTimestamp());
        }
    }

    public void updateFusekiForChangedEvents() {
        List<Event> events = resourceService.findNonSyncedEvents(PageRequest.of(0, 500));
        log.debug("updating fuseki with {} events", events.size());
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            String graphName = graphProperties.getEvents() + event.getId();

            if (i % COMPACTION_FREQUENCY == 0) {
                runCompaction();
            }

            if (event.isRemoved()) {
                deleteGraph(graphName);
            } else {
                Model m = ModelFactory.createDefaultModel();
                updateGraph(graphName, event.getGraph());
            }
            syncDataService.updateLatestSync(CatalogType.EVENTS, event.getId(), event.getTimestamp());
        }
    }

    public void updateFusekiForChangedInformationModels() {
        List<InformationModel> infoModels = resourceService.findNonSyncedInformationModels(PageRequest.of(0, 500));
        log.debug("updating fuseki with {} information models", infoModels.size());
        for (int i = 0; i < infoModels.size(); i++) {
            InformationModel infoModel = infoModels.get(i);
            String graphName = graphProperties.getInformationmodels() + infoModel.getId();

            if (i % COMPACTION_FREQUENCY == 0) {
                runCompaction();
            }

            if (infoModel.isRemoved()) {
                deleteGraph(graphName);
            } else {
                updateGraph(graphName, infoModel.getGraph());
            }
            syncDataService.updateLatestSync(CatalogType.INFORMATION_MODELS, infoModel.getId(), infoModel.getTimestamp());
        }
    }

    public void updateFusekiForChangedServices() {
        List<no.fdk.sparqlservice.model.Service> services = resourceService.findNonSyncedServices(PageRequest.of(0, 500));
        log.debug("updating fuseki with {} services", services.size());
        for (int i = 0; i < services.size(); i++) {
            no.fdk.sparqlservice.model.Service service = services.get(i);
            String graphName = graphProperties.getServices() + service.getId();

            if (i % COMPACTION_FREQUENCY == 0) {
                runCompaction();
            }

            if (service.isRemoved()) {
                deleteGraph(graphName);
            } else {
                updateGraph(graphName, service.getGraph());
            }
            syncDataService.updateLatestSync(CatalogType.SERVICES, service.getId(), service.getTimestamp());
        }
    }

    private void runCompaction() {
        String path = "%s/%s".formatted(fusekiConfiguration.getStorePath(), fusekiConfiguration.getDatasetName());
        compactAction.compact(path);
    }

    @Scheduled(initialDelay = 5, fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
    private void scheduledSynchronization() {
        log.info("Starting fuseki synchronization");
        try {
            updateFusekiForChangedConcepts();
            updateFusekiForChangedDatasets();
            updateFusekiForChangedDataServices();
            updateFusekiForChangedEvents();
            updateFusekiForChangedInformationModels();
            updateFusekiForChangedServices();
            log.info("Fuseki synchronization complete");
        } catch (Exception exception) {
            log.error("Fuseki synchronization was aborted", exception);
        }
    }

}
