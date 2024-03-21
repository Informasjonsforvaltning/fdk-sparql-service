package no.fdk.sparqlservice.configuration;


import no.fdk.concept.ConceptEvent;
import no.fdk.dataservice.DataServiceEvent;
import no.fdk.dataset.DatasetEvent;
import no.fdk.event.EventEvent;
import no.fdk.informationmodel.InformationModelEvent;
import no.fdk.service.ServiceEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ConceptEvent> conceptListenerContainerFactory(
            ConsumerFactory<String, ConceptEvent> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ConceptEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DataServiceEvent> dataServiceListenerContainerFactory(
            ConsumerFactory<String, DataServiceEvent> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, DataServiceEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DatasetEvent> datasetListenerContainerFactory(
            ConsumerFactory<String, DatasetEvent> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, DatasetEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventEvent> eventListenerContainerFactory(
            ConsumerFactory<String, EventEvent> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, EventEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InformationModelEvent> infoModelListenerContainerFactory(
            ConsumerFactory<String, InformationModelEvent> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, InformationModelEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ServiceEvent> serviceListenerContainerFactory(
            ConsumerFactory<String, ServiceEvent> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ServiceEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

}
