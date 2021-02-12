package no.fdk.rabbit;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    @Bean
    public Queue queue() {
        return new AnonymousQueue();
    }

    @Bean
    public TopicExchange updatesTopicExchange() {
        return new TopicExchange("fdk-sparql-service-compact", false, false);
    }

    @Bean
    Binding updatesBinding(Queue queue, TopicExchange topicExchange) {
        return BindingBuilder
                .bind(queue)
                .to(topicExchange)
                .with("fuseki.compact");
    }
}
