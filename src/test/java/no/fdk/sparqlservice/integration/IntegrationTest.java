package no.fdk.sparqlservice.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.fdk.concept.ConceptEvent;
import no.fdk.concept.ConceptEventType;
import no.fdk.dataservice.DataServiceEvent;
import no.fdk.dataservice.DataServiceEventType;
import no.fdk.dataset.DatasetEvent;
import no.fdk.dataset.DatasetEventType;
import no.fdk.event.EventEvent;
import no.fdk.event.EventEventType;
import no.fdk.informationmodel.InformationModelEvent;
import no.fdk.informationmodel.InformationModelEventType;
import no.fdk.service.ServiceEvent;
import no.fdk.service.ServiceEventType;
import no.fdk.sparqlservice.kafka.KafkaEventConsumers;
import no.fdk.sparqlservice.repository.*;
import no.fdk.sparqlservice.service.ResourceService;
import no.fdk.sparqlservice.service.UpdateService;
import no.fdk.sparqlservice.utils.AbstractContainerTest;
import no.fdk.sparqlservice.utils.ResourceReader;
import no.fdk.sparqlservice.utils.TestQuery;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.Acknowledgment;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(properties = "spring.profiles.active=test")
@Tag("integration")
public class IntegrationTest extends AbstractContainerTest {
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    UpdateService updateService;

    @Autowired
    ResourceService resourceService;

    @Autowired
    KafkaEventConsumers kafkaEventConsumers;

    @Autowired
    MetadataRepository metadataRepository;

    @BeforeEach
    void setup() {
        resourceService.saveConcept("fdk-0", ResourceReader.readFile("concept0.ttl"), 123);
        resourceService.saveConcept("fdk-1", ResourceReader.readFile("concept1.ttl"), 123);
        resourceService.removeConcept("fdk-2", 10);

        resourceService.saveDataService("fdk-0", ResourceReader.readFile("dataservice0.ttl"), 123);
        resourceService.saveDataService("fdk-1", ResourceReader.readFile("dataservice1.ttl"), 123);
        resourceService.removeDataService("fdk-2", 10);

        resourceService.saveDataset("fdk-0", ResourceReader.readFile("dataset0.ttl"), 123);
        resourceService.saveDataset("fdk-1", ResourceReader.readFile("dataset1.ttl"), 123);
        resourceService.removeDataset("fdk-2", 10);

        resourceService.saveEvent("fdk-0", ResourceReader.readFile("event0.ttl"), 123);
        resourceService.saveEvent("fdk-1", ResourceReader.readFile("event1.ttl"), 123);
        resourceService.removeEvent("fdk-2", 10);

        resourceService.saveInformationModel("fdk-0", ResourceReader.readFile("infomodel0.ttl"), 123);
        resourceService.saveInformationModel("fdk-1", ResourceReader.readFile("infomodel1.ttl"), 123);
        resourceService.removeInformationModel("fdk-2", 10);

        resourceService.saveService("fdk-0", ResourceReader.readFile("service0.ttl"), 123);
        resourceService.saveService("fdk-1", ResourceReader.readFile("service1.ttl"), 123);
        resourceService.removeService("fdk-2", 10);

        metadataRepository.deleteAll();

        updateService.updateFuseki();
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
                "GRAPH <https://test.fellesdatakatalog.digdir.no> {\n" +
                "?record a dcat:CatalogRecord .\n" +
                "?record foaf:primaryTopic ?resource .\n" +
                "?resource a " + rdfType + " .\n" +
                "}}\n";
    }

    private String countPropertiesOfResource(String uri) {
        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX cpsv: <http://purl.org/vocab/cpsv#>\n" +
                "PREFIX dcat: <http://www.w3.org/ns/dcat#>\n" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX cv: <http://data.europa.eu/m8g/>\n" +
                "PREFIX modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#>\n" +
                "SELECT (COUNT(DISTINCT ?object) AS ?count)\n" +
                "WHERE {\n" +
                "GRAPH <https://test.fellesdatakatalog.digdir.no> {\n" +
                "<" + uri + "> ?predicate ?object .\n" +
                "}}\n";
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
        resourceService.removeConcept("fdk-1", 124);
        updateService.updateFusekiForChangedConcepts();

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
        resourceService.removeDataService("fdk-1", 124);
        updateService.updateFusekiForChangedDataServices();

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
        resourceService.removeDataset("fdk-1", 124);
        updateService.updateFusekiForChangedDatasets();

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
        resourceService.removeEvent("fdk-0", 124);
        updateService.updateFusekiForChangedEvents();

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
        resourceService.removeInformationModel("fdk-1", 124);
        updateService.updateFusekiForChangedInformationModels();

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
        resourceService.removeService("fdk-1", 124);
        updateService.updateFusekiForChangedServices();

        String response = TestQuery.sendQuery(countQuery("cpsv:PublicService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void consumeKafkaDatasetEventOfTypeReasoned() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        DatasetEvent event = new DatasetEvent();
        event.setFdkId("fdk-2");
        event.setType(DatasetEventType.DATASET_REASONED);
        event.setGraph(ResourceReader.readFile("dataset2.ttl"));
        event.setTimestamp(124);
        kafkaEventConsumers.datasetListener(new ConsumerRecord<String, DatasetEvent>("dataset-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedDatasets();

        String response = TestQuery.sendQuery(countQuery("dcat:Dataset"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(3, result);
    }

    @Test
    void consumeKafkaDatasetEventOfTypeRemoved() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        DatasetEvent event = new DatasetEvent();
        event.setFdkId("fdk-1");
        event.setType(DatasetEventType.DATASET_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.datasetListener(new ConsumerRecord<>("dataset-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedDatasets();

        String response = TestQuery.sendQuery(countQuery("dcat:Dataset"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void consumeKafkaDataServiceEventOfTypeReasoned() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        DataServiceEvent event = new DataServiceEvent();
        event.setFdkId("fdk-2");
        event.setType(DataServiceEventType.DATA_SERVICE_REASONED);
        event.setGraph(ResourceReader.readFile("dataservice2.ttl"));
        event.setTimestamp(124);
        kafkaEventConsumers.dataServiceListener(new ConsumerRecord<>("data-service-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedDataServices();

        String response = TestQuery.sendQuery(countQuery("dcat:DataService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(3, result);
    }

    @Test
    void consumeKafkaDataServiceEventOfTypeRemoved() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        DataServiceEvent event = new DataServiceEvent();
        event.setFdkId("fdk-1");
        event.setType(DataServiceEventType.DATA_SERVICE_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.dataServiceListener(new ConsumerRecord<>("data-service-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedDataServices();

        String response = TestQuery.sendQuery(countQuery("dcat:DataService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void consumeKafkaConceptEventOfTypeReasoned() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        ConceptEvent event = new ConceptEvent();
        event.setFdkId("fdk-2");
        event.setType(ConceptEventType.CONCEPT_REASONED);
        event.setGraph(ResourceReader.readFile("concept2.ttl"));
        event.setTimestamp(124);
        kafkaEventConsumers.conceptListener(new ConsumerRecord<>("concept-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedConcepts();

        String response = TestQuery.sendQuery(countQuery("skos:Concept"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(3, result);
    }

    @Test
    void consumeKafkaConceptEventOfTypeRemoved() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        ConceptEvent event = new ConceptEvent();
        event.setFdkId("fdk-1");
        event.setType(ConceptEventType.CONCEPT_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.conceptListener(new ConsumerRecord<>("concept-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedConcepts();

        String response = TestQuery.sendQuery(countQuery("skos:Concept"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void consumeKafkaInformationModelEventOfTypeReasoned() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        InformationModelEvent event = new InformationModelEvent();
        event.setFdkId("fdk-2");
        event.setType(InformationModelEventType.INFORMATION_MODEL_REASONED);
        event.setGraph(ResourceReader.readFile("infomodel2.ttl"));
        event.setTimestamp(124);
        kafkaEventConsumers.infoModelListener(new ConsumerRecord<>("information-model-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedInformationModels();

        String response = TestQuery.sendQuery(countQuery("modelldcatno:InformationModel"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(3, result);
    }

    @Test
    void consumeKafkaInformationModelEventOfTypeRemoved() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        InformationModelEvent event = new InformationModelEvent();
        event.setFdkId("fdk-1");
        event.setType(InformationModelEventType.INFORMATION_MODEL_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.infoModelListener(new ConsumerRecord<>("information-model-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedInformationModels();

        String response = TestQuery.sendQuery(countQuery("modelldcatno:InformationModel"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void consumeKafkaEventEventOfTypeReasoned() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        EventEvent event = new EventEvent();
        event.setFdkId("fdk-2");
        event.setType(EventEventType.EVENT_REASONED);
        event.setGraph(ResourceReader.readFile("event2.ttl"));
        event.setTimestamp(124);
        kafkaEventConsumers.eventListener(new ConsumerRecord<>("event-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedEvents();

        String response = TestQuery.sendQuery(countQuery("cv:BusinessEvent"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(2, result);
    }

    @Test
    void consumeKafkaEventEventOfTypeRemoved() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        EventEvent event = new EventEvent();
        event.setFdkId("fdk-0");
        event.setType(EventEventType.EVENT_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.eventListener(new ConsumerRecord<>("event-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedEvents();

        String response = TestQuery.sendQuery(countQuery("cv:BusinessEvent"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(0, result);
    }

    @Test
    void consumeKafkaServiceEventOfTypeReasoned() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        ServiceEvent event = new ServiceEvent();
        event.setFdkId("fdk-2");
        event.setType(ServiceEventType.SERVICE_REASONED);
        event.setGraph(ResourceReader.readFile("service2.ttl"));
        event.setTimestamp(124);
        kafkaEventConsumers.serviceListener(new ConsumerRecord<>("service-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedServices();

        String response = TestQuery.sendQuery(countQuery("cpsv:PublicService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(3, result);
    }

    @Test
    void consumeKafkaServiceEventOfTypeRemoved() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        ServiceEvent event = new ServiceEvent();
        event.setFdkId("fdk-1");
        event.setType(ServiceEventType.SERVICE_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.serviceListener(new ConsumerRecord<>("service-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedServices();

        String response = TestQuery.sendQuery(countQuery("cpsv:PublicService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void removalOfResourceDoesNotRemoveTriplesFromOthers() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        EventEvent event = new EventEvent();
        event.setFdkId("fdk-1");
        event.setType(EventEventType.EVENT_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.eventListener(new ConsumerRecord<>("event-events", 0, 0L, "key", event), ack);
        updateService.updateFusekiForChangedEvents();

        String response = TestQuery.sendQuery(countPropertiesOfResource("https://organization-catalog.fellesdatakatalog.digdir.no/organizations/123456789"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(7, result);
    }
}
