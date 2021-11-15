package no.fdk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fdk.configuration.HarvestGraphNames;
import no.fdk.configuration.HarvestURI;
import no.fdk.model.fuseki.action.CatalogType;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateService {
    private final HarvestGraphNames graphNames;
    private final HarvestURI uris;

    private RDFConnection harvestedConnection() {
        return RDFConnectionFuseki.create()
                .destination(uris.getFusekiHarvested())
                .build();
    }

    public void updateForCatalogType(String catalogKey) {
        CatalogType catalogType = catalogTypeFromKey(catalogKey);
        if (catalogType != null) {
            log.debug("updating the {} graph", catalogType.name());
            try (RDFConnection conn = harvestedConnection() ) {
                Model catalogModel = RDFDataMgr.loadModel(uriForCatalogType(catalogType), Lang.TURTLE);
                String graphName = graphNameForCatalogType(catalogType);

                conn.begin(ReadWrite.WRITE);
                conn.put(graphName, catalogModel);
            } catch (Exception exception) {
                log.error("update of {} failed", catalogType.name(), exception);
            }
        }
    }

    private CatalogType catalogTypeFromKey(String catalogKey) {
        CatalogType catalogType;
        switch (catalogKey) {
            case "concepts.reasoned" -> catalogType = CatalogType.CONCEPTS;
            case "dataservices.reasoned" -> catalogType = CatalogType.DATA_SERVICES;
            case "datasets.reasoned" -> catalogType = CatalogType.DATASETS;
            case "events.reasoned" -> catalogType = CatalogType.EVENTS;
            case "informationmodels.reasoned" -> catalogType = CatalogType.INFORMATION_MODELS;
            case "public_services.reasoned" -> catalogType = CatalogType.PUBLIC_SERVICES;
            default -> catalogType = null;
        }
        return catalogType;
    }

    private String uriForCatalogType(CatalogType catalogType) {
        String uri;
        switch (catalogType) {
            case CONCEPTS -> uri = uris.getConcepts();
            case DATA_SERVICES -> uri = uris.getDataservices();
            case DATASETS -> uri = uris.getDatasets();
            case EVENTS -> uri = uris.getEvents();
            case INFORMATION_MODELS -> uri = uris.getInformationmodels();
            case PUBLIC_SERVICES -> uri = uris.getPublicservices();
            default -> uri = null;
        }
        return uri;
    }

    private String graphNameForCatalogType(CatalogType catalogType) {
        String graphName;
        switch (catalogType) {
            case CONCEPTS -> graphName = graphNames.getConcepts();
            case DATA_SERVICES -> graphName = graphNames.getDataservices();
            case DATASETS -> graphName = graphNames.getDatasets();
            case EVENTS -> graphName = graphNames.getEvents();
            case INFORMATION_MODELS -> graphName = graphNames.getInformationmodels();
            case PUBLIC_SERVICES -> graphName = graphNames.getPublicservices();
            default -> graphName = null;
        }
        return graphName;
    }

}
