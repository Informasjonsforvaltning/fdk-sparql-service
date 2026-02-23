package no.fdk.sparqlservice.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class KafkaEventConsumers {

    private final KafkaEventCircuitBreakers kafkaEventCircuitBreakers;

    @KafkaListener(
            topics = "service-events",
            groupId = "fdk-sparql-service",
            concurrency = "4",
            containerFactory = "serviceListenerContainerFactory",
            id = "service-event-consumer"
    )
    public void serviceListener(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            kafkaEventCircuitBreakers.processServiceEvent(record);
            ack.acknowledge();
        } catch (Exception exception) {
            ack.nack(Duration.ZERO);
        }
    }

    @KafkaListener(
            topics = "information-model-events",
            groupId = "fdk-sparql-service",
            concurrency = "4",
            containerFactory = "infoModelListenerContainerFactory",
            id = "information-model-event-consumer"
    )
    public void infoModelListener(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            kafkaEventCircuitBreakers.processInformationModelEvent(record);
            ack.acknowledge();
        } catch (Exception exception) {
            ack.nack(Duration.ZERO);
        }
    }

    @KafkaListener(
            topics = "event-events",
            groupId = "fdk-sparql-service",
            concurrency = "4",
            containerFactory = "eventListenerContainerFactory",
            id = "event-event-consumer"
    )
    public void eventListener(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            kafkaEventCircuitBreakers.processEventEvent(record);
            ack.acknowledge();
        } catch (Exception exception) {
            ack.nack(Duration.ZERO);
        }
    }

    @KafkaListener(
            topics = "dataset-events",
            groupId = "fdk-sparql-service",
            concurrency = "4",
            containerFactory = "datasetListenerContainerFactory",
            id = "dataset-event-consumer"
    )
    public void datasetListener(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            kafkaEventCircuitBreakers.processDatasetEvent(record);
            ack.acknowledge();
        } catch (Exception exception) {
            ack.nack(Duration.ZERO);
        }
    }

    @KafkaListener(
            topics = "data-service-events",
            groupId = "fdk-sparql-service",
            concurrency = "4",
            containerFactory = "dataServiceListenerContainerFactory",
            id = "data-service-event-consumer"
    )
    public void dataServiceListener(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            kafkaEventCircuitBreakers.processDataServiceEvent(record);
            ack.acknowledge();
        } catch (Exception exception) {
            ack.nack(Duration.ZERO);
        }
    }

    @KafkaListener(
            topics = "concept-events",
            groupId = "fdk-sparql-service",
            concurrency = "4",
            containerFactory = "conceptListenerContainerFactory",
            id = "concept-event-consumer"
    )
    public void conceptListener(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            kafkaEventCircuitBreakers.processConceptEvent(record);
            ack.acknowledge();
        } catch (Exception exception) {
            ack.nack(Duration.ZERO);
        }
    }

}
