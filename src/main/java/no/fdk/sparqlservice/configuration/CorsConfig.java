package no.fdk.sparqlservice.configuration;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Data
@Component
@ConfigurationProperties(prefix = "application.cors")
public class CorsConfig {
    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    private String workingDirectory;
    private String file;
    private String originPatterns;

    public String generateCorsConfig() {
        final String corsFilePath = System.getProperty(workingDirectory) + file;
        final File corsFile = new File(corsFilePath);

        try {
            Files.createDirectories(Paths.get(corsFile.getParent()));

            try (FileWriter writer = new FileWriter(corsFile)) {
                writer.write("allowedOrigins=" + originPatterns + "\n");
                writer.write("allowedMethods=GET,POST\n");
                writer.write("allowedHeaders=*\n");

                writer.flush();
            }

            log.info("CORS configuration file written successfully: {}", corsFilePath);
        } catch (IOException e) {
            log.error("Failed to write CORS configuration file", e);
        }

        return corsFilePath;
    }
}