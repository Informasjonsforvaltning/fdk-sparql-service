package no.fdk.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fdk.configuration.FusekiConfiguration;
import no.fdk.model.fuseki.action.CompactAction;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQListener {
    private final CompactAction compactAction;
    private final FusekiConfiguration fusekiConfiguration;

    @RabbitListener(queues = "#{queue.name}")
    public void receiveConceptPublisher(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        log.info("Received message from key: {}'", routingKey);

        fusekiConfiguration.getDatasetNames()
                .stream()
                .map(dataset -> "%s/%s".formatted(fusekiConfiguration.getStorePath(), dataset))
                .forEach(compactAction::compact);
    }
}
