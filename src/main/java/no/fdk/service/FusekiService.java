package no.fdk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fdk.configuration.FusekiConfiguration;
import no.fdk.model.fuseki.action.CompactAction;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb2.TDB2Factory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FusekiService {
    private final FusekiConfiguration fusekiConfiguration;

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
            .enableCors(true)
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
        datasetGraph.getContext().setTrue(TDB.symUnionDefaultGraph);

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
