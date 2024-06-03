package no.fdk.sparqlservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class KafkaManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaManager.class);

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    public void pause(String id) {
        LOGGER.debug("Pausing kafka listener containers with id: " + id);
        registry.getListenerContainers()
            .forEach(it -> {
                if (Objects.equals(it.getListenerId(), id)) {
                    it.pause();
                }
            });
    }

    public void resume(String id) {
        LOGGER.debug("Resuming kafka listener containers with id: " + id);
        registry.getListenerContainers()
            .forEach(it -> {
                if (Objects.equals(it.getListenerId(), id)) {
                    it.resume();
                }
            });
    }
}
