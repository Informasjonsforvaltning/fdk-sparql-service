package no.fdk.sparqlservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("spring.config.activate")
public class ProfileProperties {
    private String onProfile;
}
