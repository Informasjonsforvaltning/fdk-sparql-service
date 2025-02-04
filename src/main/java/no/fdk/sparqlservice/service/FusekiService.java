package no.fdk.sparqlservice.service;

import lombok.RequiredArgsConstructor;
import no.fdk.sparqlservice.configuration.CorsConfig;
import no.fdk.sparqlservice.configuration.FusekiConfiguration;
import no.fdk.sparqlservice.fuseki.action.CompactAction;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FusekiService {
    private static final Logger log = LoggerFactory.getLogger(FusekiService.class);
    private final FusekiConfiguration fusekiConfiguration;
    private final CorsConfig corsConfig;

    private static final Symbol nameSymbol = Symbol.create("name");

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
        startFusekiServer();
    }

    private void startFusekiServer() {
        log.info("Starting Fuseki server");

        FusekiServer.Builder builder = FusekiServer
                .create()
                .realm(fusekiConfiguration.getRealm())
                .port(fusekiConfiguration.getPort())
                .verbose(fusekiConfiguration.getEnableVerboseLogging())
                .enableCors(true, corsConfig.generateCorsConfig())
                .enablePing(true)
                .enableStats(true)
                .enableMetrics(true)
                .contextPath(fusekiConfiguration.getContextPath());

        createDataServices()
                .forEach(dataService -> builder.add(dataService.getDataset().getContext().get(nameSymbol), dataService));

        builder
                .build()
                .start();
    }

    private Set<DataService> createDataServices() {
        Path path = Path.of(fusekiConfiguration.getStorePath(), fusekiConfiguration.getDatasetName());

        Dataset dataset = TDB2Factory.connectDataset(path.toString());
        DatasetGraph datasetGraph = dataset.asDatasetGraph();
        datasetGraph.getContext().set(nameSymbol, fusekiConfiguration.getDatasetName());
        datasetGraph.getContext().setTrue(TDB2.symUnionDefaultGraph);

        DataService.Builder dataServiceBuilder = DataService.newBuilder();
        dataServiceBuilder.dataset(datasetGraph);
        dataServiceBuilder.addEndpoint(createCompactEndpoint());
        dataServiceBuilder.withStdServices(true);

        Set<DataService> dataServices = new HashSet<>();
        dataServices.add(dataServiceBuilder.build());

        return dataServices;
    }

    private Endpoint createCompactEndpoint() {
        return Endpoint
                .create()
                .operation(Operation.NoOp)
                .endpointName("compact")
                .processor(new CompactAction())
                .build();
    }
}
