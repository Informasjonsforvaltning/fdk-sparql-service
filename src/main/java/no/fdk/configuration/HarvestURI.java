package no.fdk.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@ConfigurationProperties("application.uri")
public class HarvestURI {
    private String datasets;
    private String dataservices;
    private String concepts;
    private String informationmodels;
    private String events;
    private String publicservices;
    private String fusekiHarvested;
}
