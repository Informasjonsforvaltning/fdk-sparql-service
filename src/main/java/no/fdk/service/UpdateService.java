package no.fdk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fdk.configuration.HarvestGraphProperties;
import no.fdk.model.fuseki.action.CatalogType;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.riot.Lang;
import org.springframework.stereotype.Service;

import java.io.StringReader;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateService {
    private final HarvestGraphProperties graphProperties;

    private RDFConnection fusekiConnection() {
        return RDFConnectionFuseki.create()
                .destination(graphProperties.getUri())
                .build();
    }

    private Model readTurtleBody(String body) {
        Model m = ModelFactory.createDefaultModel();

        StringReader reader = new StringReader(body);
        m.read(reader, "", Lang.TURTLE.getName());

        return m;
    }

    public void updateGraph(CatalogType type, String id, String body) {
        try (RDFConnection conn = fusekiConnection() ) {
            String graphName = graphName(type, id);
            Model catalogModel = readTurtleBody(body);

            conn.begin(ReadWrite.WRITE);
            conn.put(graphName, catalogModel);
        } catch (Exception exception) {
            log.error("update for id {} with catalog type {} failed", id, type, exception);
        }
    }

    public void deleteGraph(CatalogType type, String id) {
        try (RDFConnection conn = fusekiConnection() ) {
            String graphName = graphName(type, id);

            conn.begin(ReadWrite.WRITE);
            conn.delete(graphName);
        } catch (Exception exception) {
            log.error("delete for id {} with catalog type {} failed", id, type, exception);
        }
    }

    private String graphName(CatalogType type, String id) throws Exception {
        String graphName;
        switch (type) {
            case CONCEPTS-> graphName = graphProperties.getConcepts() + id;
            case DATA_SERVICES -> graphName = graphProperties.getDataservices() + id;
            case DATASETS -> graphName = graphProperties.getDatasets() + id;
            case EVENTS -> graphName = graphProperties.getEvents() + id;
            case INFORMATION_MODELS -> graphName = graphProperties.getInformationmodels() + id;
            case SERVICES -> graphName = graphProperties.getServices() + id;
            default -> throw new Exception("Unable to create graph name for type " + type + " and id " + id);
        }
        return graphName;
    }

}
