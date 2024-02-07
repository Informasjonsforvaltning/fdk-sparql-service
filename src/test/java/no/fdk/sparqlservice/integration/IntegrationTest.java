package no.fdk.sparqlservice.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.fdk.sparqlservice.model.CatalogType;
import no.fdk.sparqlservice.model.Concept;
import no.fdk.sparqlservice.model.DataService;
import no.fdk.sparqlservice.model.Dataset;
import no.fdk.sparqlservice.model.Event;
import no.fdk.sparqlservice.model.InformationModel;
import no.fdk.sparqlservice.model.Service;
import no.fdk.sparqlservice.repository.ConceptRepository;
import no.fdk.sparqlservice.repository.DataServiceRepository;
import no.fdk.sparqlservice.repository.DatasetRepository;
import no.fdk.sparqlservice.repository.EventRepository;
import no.fdk.sparqlservice.repository.InformationModelRepository;
import no.fdk.sparqlservice.repository.ServiceRepository;
import no.fdk.sparqlservice.service.ResourceService;
import no.fdk.sparqlservice.service.UpdateService;
import no.fdk.sparqlservice.utils.AbstractContainerTest;
import no.fdk.sparqlservice.utils.ResourceReader;
import no.fdk.sparqlservice.utils.TestQuery;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(properties = "spring.profiles.active=test")
@Tag("integration")
public class IntegrationTest extends AbstractContainerTest {
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    UpdateService updateService;

    @Autowired
    ResourceService resourceService;

    @BeforeEach
    void setup() {
        resourceService.saveConcept("0", ResourceReader.readFile("concept0.ttl"), 123);
        resourceService.saveConcept("1", ResourceReader.readFile("concept1.ttl"), 123);
        resourceService.saveDataService("0", ResourceReader.readFile("dataservice0.ttl"), 123);
        resourceService.saveDataService("1", ResourceReader.readFile("dataservice1.ttl"), 123);
        resourceService.saveDataset("0", ResourceReader.readFile("dataset0.ttl"), 123);
        resourceService.saveDataset("1", ResourceReader.readFile("dataset1.ttl"), 123);
        resourceService.saveEvent("0", ResourceReader.readFile("event0.ttl"), 123);
        resourceService.saveEvent("1", ResourceReader.readFile("event1.ttl"), 123);
        resourceService.saveInformationModel("0", ResourceReader.readFile("infomodel0.ttl"), 123);
        resourceService.saveInformationModel("1", ResourceReader.readFile("infomodel1.ttl"), 123);
        resourceService.saveService("0", ResourceReader.readFile("service0.ttl"), 123);
        resourceService.saveService("1", ResourceReader.readFile("service1.ttl"), 123);

        updateService.updateConcepts();
        updateService.updateDataServices();
        updateService.updateDatasets();
        updateService.updateEvents();
        updateService.updateInformationModels();
        updateService.updateServices();
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
        resourceService.deleteConcept("1");
        updateService.updateConcepts();

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
        resourceService.deleteDataService("1");
        updateService.updateDataServices();

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
        resourceService.deleteDataset("1");
        updateService.updateDatasets();

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
        resourceService.deleteEvent("0");
        updateService.updateEvents();

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
        resourceService.deleteInformationModel("1");
        updateService.updateInformationModels();

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
        resourceService.deleteService("1");
        updateService.updateServices();

        String response = TestQuery.sendQuery(countQuery("cpsv:PublicService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

}
