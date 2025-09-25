package no.fdk.sparqlservice.service;

import lombok.RequiredArgsConstructor;
import no.fdk.sparqlservice.configuration.GraphProperties;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UpdateService {
    private static final Logger log = LoggerFactory.getLogger(UpdateService.class);
    private final GraphProperties graphProperties;
    private final ResourceService resourceService;
    private final SyncDataService syncDataService;

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

    private void insertTurtleGraph(String ttlGraph) {
        String updateQuery = "INSERT DATA { GRAPH <" + graphProperties.getName() + "> { " + ttlGraph + " } }";

        try (RDFConnection conn = fusekiConnection()) {
            conn.begin(ReadWrite.WRITE);
            conn.update(updateQuery);
            conn.commit();
        } catch (Exception e) {
            log.error("Insert failed", e);
        }
    }

    private void removeTurtleGraph(String ttlGraph) {
        String updateQuery = "DELETE DATA { GRAPH <" + graphProperties.getName() + "> { " + ttlGraph + " } }";

        try (RDFConnection conn = fusekiConnection()) {
            conn.begin(ReadWrite.WRITE);
            conn.update(updateQuery);
            conn.commit();
        } catch (Exception e) {
            log.error("Delete failed", e);
        }
    }

    public void updateFusekiForChangedConcepts() {
        List<Concept> concepts = resourceService.findNonSyncedConcepts(PageRequest.of(0, 500));
        log.debug("updating fuseki with {} concepts", concepts.size());
        for (Concept concept : concepts) {

            if (concept.isRemoved()) {
                String graph = graphAsStringForDelete(concept.getGraph(), concept.getId());
                if (!graph.isBlank()) {
                    removeTurtleGraph(graph);
                }
            } else {
                String graph = graphAsStringForInsert(concept.getGraph());
                if (!graph.isBlank()) {
                    insertTurtleGraph(graph);
                }
            }
            syncDataService.updateLatestSync(CatalogType.CONCEPTS, concept.getId(), concept.getTimestamp());
        }
    }

    public void updateFusekiForChangedDataServices() {
        List<DataService> dataServices = resourceService.findNonSyncedDataServices(PageRequest.of(0, 500));
        log.debug("updating fuseki with {} data services", dataServices.size());
        for (DataService dataService : dataServices) {

            if (dataService.isRemoved()) {
                String graph = graphAsStringForDelete(dataService.getGraph(), dataService.getId());
                if (!graph.isBlank()) {
                    removeTurtleGraph(graph);
                }
            } else {
                String graph = graphAsStringForInsert(dataService.getGraph());
                if (!graph.isBlank()) {
                    insertTurtleGraph(graph);
                }
            }
            syncDataService.updateLatestSync(CatalogType.DATA_SERVICES, dataService.getId(), dataService.getTimestamp());
        }
    }

    public void updateFusekiForChangedDatasets() {
        List<Dataset> datasets = resourceService.findNonSyncedDatasets(PageRequest.of(0, 500));
        log.debug("updating fuseki with {} datasets", datasets.size());
        for (Dataset dataset : datasets) {

            if (dataset.isRemoved()) {
                String graph = graphAsStringForDelete(dataset.getGraph(), dataset.getId());
                if (!graph.isBlank()) {
                    removeTurtleGraph(graph);
                }
            } else {
                String graph = graphAsStringForInsert(dataset.getGraph());
                if (!graph.isBlank()) {
                    insertTurtleGraph(graph);
                }
            }
            syncDataService.updateLatestSync(CatalogType.DATASETS, dataset.getId(), dataset.getTimestamp());
        }
    }

    public void updateFusekiForChangedEvents() {
        List<Event> events = resourceService.findNonSyncedEvents(PageRequest.of(0, 500));
        log.debug("updating fuseki with {} events", events.size());
        for (Event event : events) {

            if (event.isRemoved()) {
                String graph = graphAsStringForDelete(event.getGraph(), event.getId());
                if (!graph.isBlank()) {
                    removeTurtleGraph(graph);
                }
            } else {
                String graph = graphAsStringForInsert(event.getGraph());
                if (!graph.isBlank()) {
                    insertTurtleGraph(graph);
                }
            }
            syncDataService.updateLatestSync(CatalogType.EVENTS, event.getId(), event.getTimestamp());
        }
    }

    public void updateFusekiForChangedInformationModels() {
        List<InformationModel> infoModels = resourceService.findNonSyncedInformationModels(PageRequest.of(0, 500));
        log.debug("updating fuseki with {} information models", infoModels.size());
        for (InformationModel infoModel : infoModels) {

            if (infoModel.isRemoved()) {
                String graph = graphAsStringForDelete(infoModel.getGraph(), infoModel.getId());
                if (!graph.isBlank()) {
                    removeTurtleGraph(graph);
                }
            } else {
                String graph = graphAsStringForInsert(infoModel.getGraph());
                if (!graph.isBlank()) {
                    insertTurtleGraph(graph);
                }
            }
            syncDataService.updateLatestSync(CatalogType.INFORMATION_MODELS, infoModel.getId(), infoModel.getTimestamp());
        }
    }

    public void updateFusekiForChangedServices() {
        List<no.fdk.sparqlservice.model.Service> services = resourceService.findNonSyncedServices(PageRequest.of(0, 500));
        log.debug("updating fuseki with {} services", services.size());
        for (no.fdk.sparqlservice.model.Service service : services) {

            if (service.isRemoved()) {
                String graph = graphAsStringForDelete(service.getGraph(), service.getId());
                if (!graph.isBlank()) {
                    removeTurtleGraph(graph);
                }
            } else {
                String graph = graphAsStringForInsert(service.getGraph());
                if (!graph.isBlank()) {
                    insertTurtleGraph(graph);
                }
            }
            syncDataService.updateLatestSync(CatalogType.SERVICES, service.getId(), service.getTimestamp());
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
            log.info("Fuseki synchronization complete");
        } catch (Exception exception) {
            log.error("Fuseki synchronization was aborted", exception);
        }
    }

    @Scheduled(initialDelay = 5, fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
    private void scheduledSynchronization() {
        log.info("Starting fuseki synchronization");
        updateFuseki();
    }

}
