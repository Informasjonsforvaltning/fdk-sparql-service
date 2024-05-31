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
import no.fdk.sparqlservice.kafka.KafkaEventCircuitBreakers;
import no.fdk.sparqlservice.kafka.KafkaEventConsumers;
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
import org.apache.avro.specific.SpecificRecord;
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

    @BeforeEach
    void setup() {
        resourceService.findAllDatasets().forEach(dataset -> resourceService.deleteDataset(dataset.getId()));
        resourceService.findAllDataServices().forEach(dataService -> resourceService.deleteDataService(dataService.getId()));
        resourceService.findAllConcepts().forEach(concept -> resourceService.deleteConcept(concept.getId()));
        resourceService.findAllEvents().forEach(event -> resourceService.deleteEvent(event.getId()));
        resourceService.findAllInformationModels().forEach(informationModel -> resourceService.deleteInformationModel(informationModel.getId()));
        resourceService.findAllServices().forEach(service -> resourceService.deleteService(service.getId()));

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

    @Test
    void consumeKafkaDatasetEventOfTypeReasoned() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        DatasetEvent event = new DatasetEvent();
        event.setFdkId("2");
        event.setType(DatasetEventType.DATASET_REASONED);
        event.setGraph(ResourceReader.readFile("dataset2.ttl"));
        event.setTimestamp(124);
        kafkaEventConsumers.datasetListener(new ConsumerRecord<String, DatasetEvent>("dataset-events", 0, 0L, "key", event), ack);
        updateService.updateDatasets();

        String response = TestQuery.sendQuery(countQuery("dcat:Dataset"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(3, result);
    }

    @Test
    void consumeKafkaDatasetEventOfTypeRemoved() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        DatasetEvent event = new DatasetEvent();
        event.setFdkId("1");
        event.setType(DatasetEventType.DATASET_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.datasetListener(new ConsumerRecord<>("dataset-events", 0, 0L, "key", event), ack);
        updateService.updateDatasets();

        String response = TestQuery.sendQuery(countQuery("dcat:Dataset"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void consumeKafkaDataServiceEventOfTypeReasoned() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        DataServiceEvent event = new DataServiceEvent();
        event.setFdkId("2");
        event.setType(DataServiceEventType.DATA_SERVICE_REASONED);
        event.setGraph(ResourceReader.readFile("dataservice2.ttl"));
        event.setTimestamp(124);
        kafkaEventConsumers.dataServiceListener(new ConsumerRecord<>("data-service-events", 0, 0L, "key", event), ack);
        updateService.updateDataServices();

        String response = TestQuery.sendQuery(countQuery("dcat:DataService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(3, result);
    }

    @Test
    void consumeKafkaDataServiceEventOfTypeRemoved() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        DataServiceEvent event = new DataServiceEvent();
        event.setFdkId("1");
        event.setType(DataServiceEventType.DATA_SERVICE_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.dataServiceListener(new ConsumerRecord<>("data-service-events", 0, 0L, "key", event), ack);
        updateService.updateDataServices();

        String response = TestQuery.sendQuery(countQuery("dcat:DataService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void consumeKafkaConceptEventOfTypeReasoned() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        ConceptEvent event = new ConceptEvent();
        event.setFdkId("2");
        event.setType(ConceptEventType.CONCEPT_REASONED);
        event.setGraph(ResourceReader.readFile("concept2.ttl"));
        event.setTimestamp(124);
        kafkaEventConsumers.conceptListener(new ConsumerRecord<>("concept-events", 0, 0L, "key", event), ack);
        updateService.updateConcepts();

        String response = TestQuery.sendQuery(countQuery("skos:Concept"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(3, result);
    }

    @Test
    void consumeKafkaConceptEventOfTypeRemoved() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        ConceptEvent event = new ConceptEvent();
        event.setFdkId("1");
        event.setType(ConceptEventType.CONCEPT_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.conceptListener(new ConsumerRecord<>("concept-events", 0, 0L, "key", event), ack);
        updateService.updateConcepts();

        String response = TestQuery.sendQuery(countQuery("skos:Concept"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void consumeKafkaInformationModelEventOfTypeReasoned() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        InformationModelEvent event = new InformationModelEvent();
        event.setFdkId("2");
        event.setType(InformationModelEventType.INFORMATION_MODEL_REASONED);
        event.setGraph(ResourceReader.readFile("infomodel2.ttl"));
        event.setTimestamp(124);
        kafkaEventConsumers.infoModelListener(new ConsumerRecord<>("information-model-events", 0, 0L, "key", event), ack);
        updateService.updateInformationModels();

        String response = TestQuery.sendQuery(countQuery("modelldcatno:InformationModel"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(3, result);
    }

    @Test
    void consumeKafkaInformationModelEventOfTypeRemoved() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        InformationModelEvent event = new InformationModelEvent();
        event.setFdkId("1");
        event.setType(InformationModelEventType.INFORMATION_MODEL_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.infoModelListener(new ConsumerRecord<>("information-model-events", 0, 0L, "key", event), ack);
        updateService.updateInformationModels();

        String response = TestQuery.sendQuery(countQuery("modelldcatno:InformationModel"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }

    @Test
    void consumeKafkaEventEventOfTypeReasoned() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        EventEvent event = new EventEvent();
        event.setFdkId("2");
        event.setType(EventEventType.EVENT_REASONED);
        event.setGraph(ResourceReader.readFile("event2.ttl"));
        event.setTimestamp(124);
        kafkaEventConsumers.eventListener(new ConsumerRecord<>("event-events", 0, 0L, "key", event), ack);
        updateService.updateEvents();

        String response = TestQuery.sendQuery(countQuery("cv:BusinessEvent"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(2, result);
    }

    @Test
    void consumeKafkaEventEventOfTypeRemoved() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        EventEvent event = new EventEvent();
        event.setFdkId("0");
        event.setType(EventEventType.EVENT_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.eventListener(new ConsumerRecord<>("event-events", 0, 0L, "key", event), ack);
        updateService.updateEvents();

        String response = TestQuery.sendQuery(countQuery("cv:BusinessEvent"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(0, result);
    }

    @Test
    void consumeKafkaServiceEventOfTypeReasoned() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        ServiceEvent event = new ServiceEvent();
        event.setFdkId("2");
        event.setType(ServiceEventType.SERVICE_REASONED);
        event.setGraph(ResourceReader.readFile("service2.ttl"));
        event.setTimestamp(124);
        kafkaEventConsumers.serviceListener(new ConsumerRecord<>("service-events", 0, 0L, "key", event), ack);
        updateService.updateServices();

        String response = TestQuery.sendQuery(countQuery("cpsv:PublicService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(3, result);
    }

    @Test
    void consumeKafkaServiceEventOfTypeRemoved() throws JsonProcessingException {
        Acknowledgment ack = Mockito.mock(Acknowledgment.class);

        ServiceEvent event = new ServiceEvent();
        event.setFdkId("1");
        event.setType(ServiceEventType.SERVICE_REMOVED);
        event.setGraph("");
        event.setTimestamp(124);
        kafkaEventConsumers.serviceListener(new ConsumerRecord<>("service-events", 0, 0L, "key", event), ack);
        updateService.updateServices();

        String response = TestQuery.sendQuery(countQuery("cpsv:PublicService"));
        Integer result = getCountFromSelectResponse(response);
        Assertions.assertEquals(1, result);
    }
}
