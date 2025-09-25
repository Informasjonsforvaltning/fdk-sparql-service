package no.fdk.sparqlservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("application.graph")
public class GraphProperties {
    private String name;
    private String uri;
}
