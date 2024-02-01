package no.fdk.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.fdk.model.fuseki.action.CatalogType;
import no.fdk.service.UpdateService;
import no.fdk.utils.TestQuery;
import no.fdk.utils.ResourceReader;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(properties = "spring.profiles.active=test")
@Tag("integration")
public class IntegrationTest {
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    UpdateService updateService;

    @BeforeEach
    void setup() {
        updateService.updateGraph(CatalogType.CONCEPTS, "0", ResourceReader.readFile("concept0.ttl"));
        updateService.updateGraph(CatalogType.CONCEPTS, "1", ResourceReader.readFile("concept1.ttl"));
        updateService.updateGraph(CatalogType.DATA_SERVICES, "0", ResourceReader.readFile("dataservice0.ttl"));
        updateService.updateGraph(CatalogType.DATA_SERVICES, "1", ResourceReader.readFile("dataservice1.ttl"));
        updateService.updateGraph(CatalogType.DATASETS, "0", ResourceReader.readFile("dataset0.ttl"));
        updateService.updateGraph(CatalogType.DATASETS, "1", ResourceReader.readFile("dataset1.ttl"));
        updateService.updateGraph(CatalogType.EVENTS, "0", ResourceReader.readFile("event0.ttl"));
        updateService.updateGraph(CatalogType.EVENTS, "1", ResourceReader.readFile("event1.ttl"));
        updateService.updateGraph(CatalogType.INFORMATION_MODELS, "0", ResourceReader.readFile("infomodel0.ttl"));
        updateService.updateGraph(CatalogType.INFORMATION_MODELS, "1", ResourceReader.readFile("infomodel1.ttl"));
        updateService.updateGraph(CatalogType.SERVICES, "0", ResourceReader.readFile("service0.ttl"));
        updateService.updateGraph(CatalogType.SERVICES, "1", ResourceReader.readFile("service1.ttl"));
    }

    private String countQuery(String rdfType) {
        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX cpsv: <http://purl.org/vocab/cpsv#>\n" +
                "PREFIX dcat: <http://www.w3.org/ns/dcat#>\n" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX cv: <http://data.europa.eu/m8g/>\n" +
                "PREFIX modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#>\n" +
                "SELECT (COUNT(DISTINCT ?resource) AS ?count)\n" +
                "WHERE {\n" +
                "?record a dcat:CatalogRecord .\n" +
                "?record foaf:primaryTopic ?resource .\n" +
                "?resource a " + rdfType + " .\n" +
                "}\n";
    }

    private Integer getCountFromSelectResponse(String response) throws JsonProcessingException {
        JsonNode jsonNode = mapper.readTree(response);
        return jsonNode.get("results").get("bindings").get(0).get("count").get("value").asInt();
    }

    @Test
    void countConcepts() throws JsonProcessingException {
        String response = TestQuery.sendQuery(countQuery("skos:Concept"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(2, result);
    }

    @Test
    void deleteConcept() throws JsonProcessingException {
        updateService.deleteGraph(CatalogType.CONCEPTS, "1");

        String response = TestQuery.sendQuery(countQuery("skos:Concept"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void countDataServices() throws JsonProcessingException {
        String response = TestQuery.sendQuery(countQuery("dcat:DataService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(2, result);
    }

    @Test
    void deleteDataService() throws JsonProcessingException {
        updateService.deleteGraph(CatalogType.DATA_SERVICES, "1");

        String response = TestQuery.sendQuery(countQuery("dcat:DataService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void countDatasets() throws JsonProcessingException {
        String response = TestQuery.sendQuery(countQuery("dcat:Dataset"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(2, result);
    }

    @Test
    void deleteDataset() throws JsonProcessingException {
        updateService.deleteGraph(CatalogType.DATASETS, "1");

        String response = TestQuery.sendQuery(countQuery("dcat:Dataset"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void countEvents() throws JsonProcessingException {
        String lifeResponse = TestQuery.sendQuery(countQuery("cv:LifeEvent"));
        Integer lifeResult = getCountFromSelectResponse(lifeResponse);
        Assertions.assertEquals(1, lifeResult);

        String businessResponse = TestQuery.sendQuery(countQuery("cv:BusinessEvent"));
        Integer businessResult = getCountFromSelectResponse(businessResponse);
        Assertions.assertEquals(1, businessResult);
    }

    @Test
    void deleteEvent() throws JsonProcessingException {
        updateService.deleteGraph(CatalogType.EVENTS, "0");

        String businessResponse = TestQuery.sendQuery(countQuery("cv:BusinessEvent"));
        Integer businessResult = getCountFromSelectResponse(businessResponse);
        Assertions.assertEquals(0, businessResult);
    }

    @Test
    void countInformationModels() throws JsonProcessingException {
        String response = TestQuery.sendQuery(countQuery("modelldcatno:InformationModel"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(2, result);
    }

    @Test
    void deleteInformationModel() throws JsonProcessingException {
        updateService.deleteGraph(CatalogType.INFORMATION_MODELS, "1");

        String response = TestQuery.sendQuery(countQuery("modelldcatno:InformationModel"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void countServices() throws JsonProcessingException {
        String response = TestQuery.sendQuery(countQuery("cpsv:PublicService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(2, result);
    }

    @Test
    void deleteService() throws JsonProcessingException {
        updateService.deleteGraph(CatalogType.SERVICES, "1");

        String response = TestQuery.sendQuery(countQuery("cpsv:PublicService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

}
