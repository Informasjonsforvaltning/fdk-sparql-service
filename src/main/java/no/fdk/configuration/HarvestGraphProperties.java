package no.fdk.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("application.graph")
public class HarvestGraphProperties {
    private String datasets;
    private String dataservices;
    private String concepts;
    private String informationmodels;
    private String events;
    private String services;
    private String uri;
}
