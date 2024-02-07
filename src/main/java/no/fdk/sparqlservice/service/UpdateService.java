package no.fdk.sparqlservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fdk.sparqlservice.configuration.FusekiConfiguration;
import no.fdk.sparqlservice.configuration.HarvestGraphProperties;
import no.fdk.sparqlservice.fuseki.action.CompactAction;
import no.fdk.sparqlservice.model.CatalogType;
import no.fdk.sparqlservice.model.Concept;
import no.fdk.sparqlservice.model.DataService;
import no.fdk.sparqlservice.model.Dataset;
import no.fdk.sparqlservice.model.Event;
import no.fdk.sparqlservice.model.InformationModel;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.riot.Lang;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateService {
    private final HarvestGraphProperties graphProperties;
    private final FusekiConfiguration fusekiConfiguration;
    private final CompactAction compactAction;
    private final ResourceService resourceService;

    private RDFConnection fusekiConnection() {
        return RDFConnectionFuseki.create()
                .destination(graphProperties.getUri())
                .build();
    }

    private void updateGraph(String graphName, Model catalogModel) {
        try (RDFConnection conn = fusekiConnection() ) {
            conn.begin(ReadWrite.WRITE);
            conn.put(graphName, catalogModel);
        } catch (Exception exception) {
            log.error("update of graph {} failed", graphName, exception);
        }
    }

    public Model readTurtle(CatalogType type, String id, String turtle) {
        Model m = ModelFactory.createDefaultModel();
        try {
            StringReader reader = new StringReader(turtle);
            m.read(reader, "", Lang.TURTLE.getName());
        } catch (Exception ex) {
            log.error("unable to read turtle for {} with id {}", type, id);
        }
        return m;
    }

    public void updateConcepts() {
        List<Concept> concepts = resourceService.findAllConcepts();
        Model m = ModelFactory.createDefaultModel();
        for (Concept concept : concepts) {
            m.add(readTurtle(CatalogType.CONCEPTS, concept.getId(), concept.getGraph()));
        }

        updateGraph(graphProperties.getConcepts(), m);
    }

    public void updateDataServices() {
        List<DataService> dataServices = resourceService.findAllDataServices();
        Model m = ModelFactory.createDefaultModel();
        for (DataService dataService : dataServices) {
            m.add(readTurtle(CatalogType.DATA_SERVICES, dataService.getId(), dataService.getGraph()));
        }

        updateGraph(graphProperties.getDataservices(), m);
    }

    public void updateDatasets() {
        List<Dataset> datasets = resourceService.findAllDatasets();
        Model m = ModelFactory.createDefaultModel();
        for (Dataset dataset : datasets) {
            m.add(readTurtle(CatalogType.DATASETS, dataset.getId(), dataset.getGraph()));
        }

        updateGraph(graphProperties.getDatasets(), m);
    }

    public void updateEvents() {
        List<Event> events = resourceService.findAllEvents();
        Model m = ModelFactory.createDefaultModel();
        for (Event event : events) {
            m.add(readTurtle(CatalogType.EVENTS, event.getId(), event.getGraph()));
        }

        updateGraph(graphProperties.getEvents(), m);
    }

    public void updateInformationModels() {
        List<InformationModel> infoModels = resourceService.findAllInformationModels();
        Model m = ModelFactory.createDefaultModel();
        for (InformationModel infoModel : infoModels) {
            m.add(readTurtle(CatalogType.INFORMATION_MODELS, infoModel.getId(), infoModel.getGraph()));
        }

        updateGraph(graphProperties.getInformationmodels(), m);
    }

    public void updateServices() {
        List<no.fdk.sparqlservice.model.Service> services = resourceService.findAllServices();
        Model m = ModelFactory.createDefaultModel();
        for (no.fdk.sparqlservice.model.Service service : services) {
            m.add(readTurtle(CatalogType.SERVICES, service.getId(), service.getGraph()));
        }

        updateGraph(graphProperties.getServices(), m);
    }

    @Scheduled(initialDelay = 5, fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    private void scheduledUpdate() {
        Set<CatalogType> updatedCatalogs = resourceService.getAndResetUpdatedCatalogs();
        for (CatalogType type : updatedCatalogs) {
            switch (type) {
                case CONCEPTS -> updateConcepts();
                case DATA_SERVICES -> updateDataServices();
                case DATASETS -> updateDatasets();
                case EVENTS -> updateEvents();
                case INFORMATION_MODELS -> updateInformationModels();
                case SERVICES -> updateServices();
            }
        }
        if (!updatedCatalogs.isEmpty()) {
            String path = "%s/%s".formatted(fusekiConfiguration.getStorePath(), fusekiConfiguration.getDatasetName());
            compactAction.compact(path);
        }
    }

}
