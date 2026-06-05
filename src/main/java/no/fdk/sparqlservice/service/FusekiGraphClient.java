package no.fdk.sparqlservice.service;

import lombok.RequiredArgsConstructor;
import no.fdk.sparqlservice.configuration.GraphProperties;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
@RequiredArgsConstructor
public class FusekiGraphClient {
    private static final Logger log = LoggerFactory.getLogger(FusekiGraphClient.class);

    private final GraphProperties graphProperties;

    public Model graphAsModelForInsert(byte[] ttlGraph, byte[] ttlCatalog) {
        Model model = ModelFactory.createDefaultModel();
        try {
            model.read(new ByteArrayInputStream(ttlGraph), null, Lang.TURTLE.getName());
            if (ttlCatalog != null) {
                model.read(new ByteArrayInputStream(ttlCatalog), null, Lang.TURTLE.getName());
            }
            return model;
        } catch (Exception e) {
            log.error("creation of insert graph failed, skipping update", e);
            model.close();
            return ModelFactory.createDefaultModel();
        }
    }

    public void initFusekiGraphIfEmpty() {
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

    public boolean insert(Model model) {
        if (model == null || model.isEmpty()) {
            return true;
        }
        try (RDFConnection conn = fusekiConnection()) {
            conn.begin(ReadWrite.WRITE);
            conn.load(graphProperties.getName(), model);
            conn.commit();
            return true;
        } catch (Exception e) {
            log.error("Insert failed", e);
            return false;
        }
    }

    public boolean removeByIdentifier(String fdkId) {
        if (fdkId == null || fdkId.isBlank()) {
            return true;
        }

        String graphName = graphProperties.getName();
        String updateEndpoint = graphProperties.getUri() + "/update";

        String updateSparql = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + "PREFIX dcterms: <http://purl.org/dc/terms/> "
                + "DELETE { "
                + "  GRAPH <" + graphName + "> { "
                + "    ?record ?recordPred ?recordObj . "
                + "    ?topic  ?topicPred  ?topicObj  . "
                + "  } "
                + "} "
                + "WHERE { "
                + "  GRAPH <" + graphName + "> { "
                + "    ?record dcterms:identifier ?id . "
                + "    FILTER(STR(?id) = " + escapeStringLiteral(fdkId) + ") . "
                + "    ?record ?recordPred ?recordObj . "
                + "    OPTIONAL { "
                + "      ?record foaf:primaryTopic ?topic . "
                + "      ?topic  ?topicPred  ?topicObj  . "
                + "    } "
                + "  } "
                + "}";

        try {
            UpdateRequest updateRequest = UpdateFactory.create(updateSparql);
            UpdateExecutionFactory.createRemote(updateRequest, updateEndpoint).execute();
            return true;
        } catch (Exception e) {
            log.error(
                    "Delete failed for identifier={} endpoint={}",
                    fdkId,
                    updateEndpoint,
                    e
            );
            return false;
        }
    }

    private RDFConnection fusekiConnection() {
        return RDFConnectionFuseki.create()
                .destination(graphProperties.getUri())
                .build();
    }

    private static String escapeStringLiteral(String value) {
        if (value == null) {
            return "''";
        }

        String escaped = value
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        return "'" + escaped + "'";
    }
}
