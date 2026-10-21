package no.fdk.sparqlservice.configuration;


import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.security.protocol}")
    private String securityProtocol;

    @Value("${spring.kafka.properties.sasl.mechanism}")
    private String saslMechanism;

    @Value("${spring.kafka.properties.sasl.jaas.config}")
    private String saslJaasConfig;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    /**
     * Single consumer factory for all event topics. Uses generic Avro deserialization (no specific reader)
     * so that both generic and specific record payloads are supported.
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        props.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("specific.avro.reader", false);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> conceptListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        // Keep retrying instead of stopping the listeners for good on an auth failure.
        props.setAuthExceptionRetryInterval(Duration.ofSeconds(30));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> dataServiceListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        // Keep retrying instead of stopping the listeners for good on an auth failure.
        props.setAuthExceptionRetryInterval(Duration.ofSeconds(30));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> datasetListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        // Keep retrying instead of stopping the listeners for good on an auth failure.
        props.setAuthExceptionRetryInterval(Duration.ofSeconds(30));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> eventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        // Keep retrying instead of stopping the listeners for good on an auth failure.
        props.setAuthExceptionRetryInterval(Duration.ofSeconds(30));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> infoModelListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        // Keep retrying instead of stopping the listeners for good on an auth failure.
        props.setAuthExceptionRetryInterval(Duration.ofSeconds(30));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> serviceListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        // Keep retrying instead of stopping the listeners for good on an auth failure.
        props.setAuthExceptionRetryInterval(Duration.ofSeconds(30));
        return factory;
    }
}
