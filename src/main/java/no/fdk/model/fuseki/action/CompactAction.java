package no.fdk.model.fuseki.action;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.fuseki.servlets.BaseActionREST;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class CompactAction extends BaseActionREST {
    @Override
    protected void doGet(HttpAction action) {
        log.info("Started compaction process");

        DatasetGraph datasetGraph = action.getDataset();

        DatabaseMgr.compact(datasetGraph);

        removeUnusedFiles(datasetGraph);

        log.info("Finished compaction process");
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
