package no.fdk;

import no.fdk.configuration.FusekiConfiguration;
import no.fdk.configuration.HarvestGraphNames;
import no.fdk.configuration.HarvestURI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    FusekiConfiguration.class,
    HarvestGraphNames.class,
    HarvestURI.class
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
