package no.fdk.sparqlservice.service;

import lombok.RequiredArgsConstructor;
import no.fdk.harvest.HarvestPhase;
import no.fdk.sparqlservice.configuration.GraphProperties;
import no.fdk.sparqlservice.kafka.HarvestEventProducer;
import no.fdk.sparqlservice.model.*;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UpdateService {
    private static final Logger log = LoggerFactory.getLogger(UpdateService.class);
    private final GraphProperties graphProperties;
    private final ResourceService resourceService;
    private final SyncDataService syncDataService;
    private final HarvestEventProducer harvestEventProducer;

    private String graphAsStringForInsert(byte[] ttlGraph) {
        Model model = ModelFactory.createDefaultModel();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            model.read(new ByteArrayInputStream(ttlGraph), null, "TURTLE");
            model.clearNsPrefixMap();
            model.write(out, "TURTLE");
            out.flush();
            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("namespace removal failed, skipping update", e);
            return "";
        } finally {
            model.close();
        }
    }

    private String graphAsStringForDelete(byte[] ttlGraph, String fdkId) {
        Model fullModel = ModelFactory.createDefaultModel();
        Model partialModel = ModelFactory.createDefaultModel();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            fullModel.read(new ByteArrayInputStream(ttlGraph), null, "TURTLE");
            fullModel.clearNsPrefixMap();

            ResIterator resources = fullModel.listResourcesWithProperty(DCTerms.identifier, fdkId);
            resources.forEach(resource -> {
                partialModel.add(resource.listProperties());
                resource.listProperties(FOAF.primaryTopic).forEach(stmt -> partialModel.add(stmt.getResource().listProperties()));
            });

            partialModel.write(out, "TURTLE");
            out.flush();
            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("creation of remove graph failed, skipping update", e);
            return "";
        } finally {
            fullModel.close();
            partialModel.close();
        }
    }

    private RDFConnection fusekiConnection() {
        return RDFConnectionFuseki.create()
                .destination(graphProperties.getUri())
                .build();
    }

    private void initFusekiGraphIfEmpty() {
        try (RDFConnection conn = fusekiConnection()) {
            conn.begin(ReadWrite.READ);
            boolean hasTriples = conn.queryAsk("ASK { ?s ?p ?o }");
            conn.commit();
            if (!hasTriples) {
                Model m = ModelFactory.createDefaultModel();
                m.add(m.createResource(graphProperties.getName()), RDF.type, DCAT.Catalog);
                conn.begin(ReadWrite.WRITE);
                conn.put(graphProperties.getName(), m);
                conn.commit();
            }
        } catch (Exception e) {
            log.error("Init failed", e);
        }
    }

    private boolean insertTurtleGraph(String ttlGraph) {
        String updateQuery = "INSERT DATA { GRAPH <" + graphProperties.getName() + "> { " + ttlGraph + " } }";

        try (RDFConnection conn = fusekiConnection()) {
            conn.begin(ReadWrite.WRITE);
            conn.update(updateQuery);
            conn.commit();
            return true;
        } catch (Exception e) {
            log.error("Insert failed", e);
            return false;
        }
    }

    private boolean removeTurtleGraph(String ttlGraph) {
        String updateQuery = "DELETE DATA { GRAPH <" + graphProperties.getName() + "> { " + ttlGraph + " } }";

        try (RDFConnection conn = fusekiConnection()) {
            conn.begin(ReadWrite.WRITE);
            conn.update(updateQuery);
            conn.commit();
            return true;
        } catch (Exception e) {
            log.error("Delete failed", e);
            return false;
        }
    }

    public void updateFusekiForChangedConcepts() {
        List<String> concepts = resourceService.findNonSyncedConcepts(PageRequest.of(0, 500));
        if (!concepts.isEmpty()) {
            log.info("updating fuseki with {} concept graphs", concepts.size());
        }
        for (String fdkId : concepts) {
            Optional<Concept> conceptWrap = resourceService.findConceptById(fdkId);
            if (conceptWrap.isPresent()) {
                Concept concept = conceptWrap.get();
                boolean success = false;
                String errorMessage = null;
                String startTime = Instant.now().toString();
                String endTime = null;
                try {
                    if (concept.isRemoved()) {
                        String graph = graphAsStringForDelete(concept.getGraph(), concept.getId());
                        if (!graph.isBlank()) {
                            success = removeTurtleGraph(graph);
                        } else {
                            success = true;
                        }
                    } else {
                        String graph = graphAsStringForInsert(concept.getGraph());
                        if (!graph.isBlank()) {
                            success = insertTurtleGraph(graph);
                        } else {
                            success = true;
                        }
                    }
                    endTime = Instant.now().toString();
                    if (success) {
                        syncDataService.updateLatestSync(CatalogType.CONCEPTS, concept.getId(), concept.getTimestamp());
                    } else {
                        errorMessage = "SPARQL update failed for concept: " + fdkId;
                    }
                } catch (Exception e) {
                    endTime = Instant.now().toString();
                    errorMessage = "SPARQL update exception for concept: " + fdkId + " - " + e.getMessage();
                    log.error("Error updating concept", e);
                }
                
                if (concept.isPendingHarvestEvent() && concept.getHarvestRunId() != null) {
                    harvestEventProducer.produceHarvestEvent(
                            concept.getHarvestRunId(),
                            CatalogType.CONCEPTS,
                            concept.getId(),
                            HarvestPhase.SPARQL_PROCESSING,
                            startTime,
                            endTime,
                            success,
                            errorMessage
                    );
                    resourceService.clearPendingHarvestEvent(concept.getId(), CatalogType.CONCEPTS);
                }
            }
        }
    }

    public void updateFusekiForChangedDataServices() {
        List<String> dataServices = resourceService.findNonSyncedDataServices(PageRequest.of(0, 500));
        if (!dataServices.isEmpty()) {
            log.info("updating fuseki with {} data service graphs", dataServices.size());
        }
        for (String fdkId : dataServices) {
            Optional<DataService> dataServiceWrap = resourceService.findDataServiceById(fdkId);
            if (dataServiceWrap.isPresent()) {
                DataService dataService = dataServiceWrap.get();
                boolean success = false;
                String errorMessage = null;
                String startTime = Instant.now().toString();
                String endTime = null;
                try {
                    if (dataService.isRemoved()) {
                        String graph = graphAsStringForDelete(dataService.getGraph(), dataService.getId());
                        if (!graph.isBlank()) {
                            success = removeTurtleGraph(graph);
                        } else {
                            success = true;
                        }
                    } else {
                        String graph = graphAsStringForInsert(dataService.getGraph());
                        if (!graph.isBlank()) {
                            success = insertTurtleGraph(graph);
                        } else {
                            success = true;
                        }
                    }
                    endTime = Instant.now().toString();
                    if (success) {
                        syncDataService.updateLatestSync(CatalogType.DATA_SERVICES, dataService.getId(), dataService.getTimestamp());
                    } else {
                        errorMessage = "SPARQL update failed for data service: " + fdkId;
                    }
                } catch (Exception e) {
                    endTime = Instant.now().toString();
                    errorMessage = "SPARQL update exception for data service: " + fdkId + " - " + e.getMessage();
                    log.error("Error updating data service", e);
                }
                
                if (dataService.isPendingHarvestEvent() && dataService.getHarvestRunId() != null) {
                    harvestEventProducer.produceHarvestEvent(
                            dataService.getHarvestRunId(),
                            CatalogType.DATA_SERVICES,
                            dataService.getId(),
                            HarvestPhase.SPARQL_PROCESSING,
                            startTime,
                            endTime,
                            success,
                            errorMessage
                    );
                    resourceService.clearPendingHarvestEvent(dataService.getId(), CatalogType.DATA_SERVICES);
                }
            }
        }
    }

    public void updateFusekiForChangedDatasets() {
        List<String> datasets = resourceService.findNonSyncedDatasets(PageRequest.of(0, 500));
        if (!datasets.isEmpty()) {
            log.info("updating fuseki with {} dataset graphs", datasets.size());
        }
        for (String fdkId : datasets) {
            Optional<Dataset> datasetWrap = resourceService.findDatasetById(fdkId);
            if (datasetWrap.isPresent()) {
                Dataset dataset = datasetWrap.get();
                boolean success = false;
                String errorMessage = null;
                String startTime = Instant.now().toString();
                String endTime = null;
                try {
                    if (dataset.isRemoved()) {
                        String graph = graphAsStringForDelete(dataset.getGraph(), dataset.getId());
                        if (!graph.isBlank()) {
                            success = removeTurtleGraph(graph);
                        } else {
                            success = true;
                        }
                    } else {
                        String graph = graphAsStringForInsert(dataset.getGraph());
                        if (!graph.isBlank()) {
                            success = insertTurtleGraph(graph);
                        } else {
                            success = true;
                        }
                    }
                    endTime = Instant.now().toString();
                    if (success) {
                        syncDataService.updateLatestSync(CatalogType.DATASETS, dataset.getId(), dataset.getTimestamp());
                    } else {
                        errorMessage = "SPARQL update failed for dataset: " + fdkId;
                    }
                } catch (Exception e) {
                    endTime = Instant.now().toString();
                    errorMessage = "SPARQL update exception for dataset: " + fdkId + " - " + e.getMessage();
                    log.error("Error updating dataset", e);
                }
                
                if (dataset.isPendingHarvestEvent() && dataset.getHarvestRunId() != null) {
                    harvestEventProducer.produceHarvestEvent(
                            dataset.getHarvestRunId(),
                            CatalogType.DATASETS,
                            dataset.getId(),
                            HarvestPhase.SPARQL_PROCESSING,
                            startTime,
                            endTime,
                            success,
                            errorMessage
                    );
                    resourceService.clearPendingHarvestEvent(dataset.getId(), CatalogType.DATASETS);
                }
            }
        }
    }

    public void updateFusekiForChangedEvents() {
        List<String> events = resourceService.findNonSyncedEvents(PageRequest.of(0, 500));
        if (!events.isEmpty()) {
            log.info("updating fuseki with {} event graphs", events.size());
        }
        for (String fdkId : events) {
            Optional<Event> eventWrap = resourceService.findEventById(fdkId);
            if (eventWrap.isPresent()) {
                Event event = eventWrap.get();
                boolean success = false;
                String errorMessage = null;
                String startTime = Instant.now().toString();
                String endTime = null;
                try {
                    if (event.isRemoved()) {
                        String graph = graphAsStringForDelete(event.getGraph(), event.getId());
                        if (!graph.isBlank()) {
                            success = removeTurtleGraph(graph);
                        } else {
                            success = true;
                        }
                    } else {
                        String graph = graphAsStringForInsert(event.getGraph());
                        if (!graph.isBlank()) {
                            success = insertTurtleGraph(graph);
                        } else {
                            success = true;
                        }
                    }
                    endTime = Instant.now().toString();
                    if (success) {
                        syncDataService.updateLatestSync(CatalogType.EVENTS, event.getId(), event.getTimestamp());
                    } else {
                        errorMessage = "SPARQL update failed for event: " + fdkId;
                    }
                } catch (Exception e) {
                    endTime = Instant.now().toString();
                    errorMessage = "SPARQL update exception for event: " + fdkId + " - " + e.getMessage();
                    log.error("Error updating event", e);
                }
                
                if (event.isPendingHarvestEvent() && event.getHarvestRunId() != null) {
                    harvestEventProducer.produceHarvestEvent(
                            event.getHarvestRunId(),
                            CatalogType.EVENTS,
                            event.getId(),
                            HarvestPhase.SPARQL_PROCESSING,
                            startTime,
                            endTime,
                            success,
                            errorMessage
                    );
                    resourceService.clearPendingHarvestEvent(event.getId(), CatalogType.EVENTS);
                }
            }
        }
    }

    public void updateFusekiForChangedInformationModels() {
        List<String> infoModels = resourceService.findNonSyncedInformationModels(PageRequest.of(0, 500));
        if (!infoModels.isEmpty()) {
            log.info("updating fuseki with {} information model graphs", infoModels.size());
        }
        for (String fdkId : infoModels) {
            Optional<InformationModel> infoModelWrap = resourceService.findInformationModelById(fdkId);
            if (infoModelWrap.isPresent()) {
                InformationModel infoModel = infoModelWrap.get();
                boolean success = false;
                String errorMessage = null;
                String startTime = Instant.now().toString();
                String endTime = null;
                try {
                    if (infoModel.isRemoved()) {
                        String graph = graphAsStringForDelete(infoModel.getGraph(), infoModel.getId());
                        if (!graph.isBlank()) {
                            success = removeTurtleGraph(graph);
                        } else {
                            success = true;
                        }
                    } else {
                        String graph = graphAsStringForInsert(infoModel.getGraph());
                        if (!graph.isBlank()) {
                            success = insertTurtleGraph(graph);
                        } else {
                            success = true;
                        }
                    }
                    endTime = Instant.now().toString();
                    if (success) {
                        syncDataService.updateLatestSync(CatalogType.INFORMATION_MODELS, infoModel.getId(), infoModel.getTimestamp());
                    } else {
                        errorMessage = "SPARQL update failed for information model: " + fdkId;
                    }
                } catch (Exception e) {
                    endTime = Instant.now().toString();
                    errorMessage = "SPARQL update exception for information model: " + fdkId + " - " + e.getMessage();
                    log.error("Error updating information model", e);
                }
                
                if (infoModel.isPendingHarvestEvent() && infoModel.getHarvestRunId() != null) {
                    harvestEventProducer.produceHarvestEvent(
                            infoModel.getHarvestRunId(),
                            CatalogType.INFORMATION_MODELS,
                            infoModel.getId(),
                            HarvestPhase.SPARQL_PROCESSING,
                            startTime,
                            endTime,
                            success,
                            errorMessage
                    );
                    resourceService.clearPendingHarvestEvent(infoModel.getId(), CatalogType.INFORMATION_MODELS);
                }
            }
        }
    }

    public void updateFusekiForChangedServices() {
        List<String> services = resourceService.findNonSyncedServices(PageRequest.of(0, 500));
        if (!services.isEmpty()) {
            log.info("updating fuseki with {} service graphs", services.size());
        }
        for (String fdkId : services) {
            Optional<no.fdk.sparqlservice.model.Service> serviceWrap = resourceService.findServiceById(fdkId);
            if (serviceWrap.isPresent()) {
                no.fdk.sparqlservice.model.Service service = serviceWrap.get();
                boolean success = false;
                String errorMessage = null;
                String startTime = Instant.now().toString();
                String endTime = null;
                try {
                    if (service.isRemoved()) {
                        String graph = graphAsStringForDelete(service.getGraph(), service.getId());
                        if (!graph.isBlank()) {
                            success = removeTurtleGraph(graph);
                        } else {
                            success = true;
                        }
                    } else {
                        String graph = graphAsStringForInsert(service.getGraph());
                        if (!graph.isBlank()) {
                            success = insertTurtleGraph(graph);
                        } else {
                            success = true;
                        }
                    }
                    endTime = Instant.now().toString();
                    if (success) {
                        syncDataService.updateLatestSync(CatalogType.SERVICES, service.getId(), service.getTimestamp());
                    } else {
                        errorMessage = "SPARQL update failed for service: " + fdkId;
                    }
                } catch (Exception e) {
                    endTime = Instant.now().toString();
                    errorMessage = "SPARQL update exception for service: " + fdkId + " - " + e.getMessage();
                    log.error("Error updating service", e);
                }
                
                if (service.isPendingHarvestEvent() && service.getHarvestRunId() != null) {
                    harvestEventProducer.produceHarvestEvent(
                            service.getHarvestRunId(),
                            CatalogType.SERVICES,
                            service.getId(),
                            HarvestPhase.SPARQL_PROCESSING,
                            startTime,
                            endTime,
                            success,
                            errorMessage
                    );
                    resourceService.clearPendingHarvestEvent(service.getId(), CatalogType.SERVICES);
                }
            }
        }
    }

    public void updateFuseki() {
        initFusekiGraphIfEmpty();
        initFusekiGraphIfEmpty();
        try {
            updateFusekiForChangedConcepts();
            updateFusekiForChangedDatasets();
            updateFusekiForChangedDataServices();
            updateFusekiForChangedEvents();
            updateFusekiForChangedInformationModels();
            updateFusekiForChangedServices();
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
