package no.fdk.sparqlservice.utils;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ContextConfiguration(initializers = AbstractContainerTest.Initializer.class)
public class AbstractContainerTest {
    private static final int POSTGRES_PORT = 5432;

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:14-alpine")
                .withExposedPorts(POSTGRES_PORT)
                .withUsername("postgres")
                .withPassword("postgres")
                .withDatabaseName("sparql_test");

        @Override
        public void initialize(@NonNull ConfigurableApplicationContext context) {
            // Start containers
            postgreSQLContainer.start();

            // Override configuration
            String host = postgreSQLContainer.getHost();
            Integer port = postgreSQLContainer.getMappedPort(POSTGRES_PORT);
            String postgresURL = "spring.datasource.url=jdbc:postgresql://" + host + ":" + port + "/sparql_test";

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context, postgresURL);
        }
    }
}
