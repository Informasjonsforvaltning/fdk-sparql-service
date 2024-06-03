package no.fdk.sparqlservice.fuseki.action;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.fuseki.servlets.BaseActionREST;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class CompactAction extends BaseActionREST {
    private static final Logger log = LoggerFactory.getLogger(CompactAction.class);
    @Override
    protected void doGet(HttpAction action) {
        compact(action.getDataset());
    }

    private void compact(DatasetGraph datasetGraph) {
        DatabaseMgr.compact(datasetGraph);

        removeUnusedFiles(datasetGraph);

        log.info("Finished compaction process");
    }

    public void compact(String datasetPath) {
        DatasetGraphSwitchable datasetGraph = (DatasetGraphSwitchable) DatabaseMgr.connectDatasetGraph(Location.create(datasetPath));
        compact(datasetGraph);
    }

    private void removeUnusedFiles(DatasetGraph datasetGraph) {
        String databasePath = TDBInternal.getDatabaseContainer(datasetGraph).getLocation().getDirectoryPath();
        String currentDatasetPath = TDBInternal.getDatasetGraphTDB(datasetGraph).getLocation().getDirectoryPath();

        Set<String> excludedPaths = Set.of(
            currentDatasetPath.replaceAll("/$", ""),
            Path.of(databasePath, "tdb.lock").toString()
        );

        Stream
            .ofNullable(new File(databasePath))
            .map(File::listFiles)
            .filter(Objects::nonNull)
            .flatMap(Arrays::stream)
            .filter(file -> isDeletable(file, excludedPaths))
            .forEach(FileSystemUtils::deleteRecursively);
    }

    private boolean isDeletable(File file, Set<String> exclusions) {
        return exclusions
            .stream()
            .noneMatch(file.getAbsolutePath()::equals);
    }
}
