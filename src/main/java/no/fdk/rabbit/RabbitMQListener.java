package no.fdk.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fdk.configuration.FusekiConfiguration;
import no.fdk.model.fuseki.action.CompactAction;
import no.fdk.service.UpdateService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQListener {
    private final CompactAction compactAction;
    private final FusekiConfiguration fusekiConfiguration;
    private final UpdateService updateService;

    @RabbitListener(queues = "#{compactQueue.name}")
    public void receiveConceptPublisher(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        log.info("Received message from compact exchange with key: {}'", routingKey);

        fusekiConfiguration.getDatasetNames()
                .stream()
                .map(dataset -> "%s/%s".formatted(fusekiConfiguration.getStorePath(), dataset))
                .forEach(compactAction::compact);
    }

    @RabbitListener(queues = "#{harvestsQueue.name}")
    public void harvestsListener(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        log.debug("Received message from harvests exchange with key: {}'", routingKey);
        updateService.updateForCatalogType(routingKey);
    }
}
