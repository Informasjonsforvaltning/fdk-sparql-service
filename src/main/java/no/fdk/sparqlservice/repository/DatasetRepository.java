package no.fdk.sparqlservice.repository;

import no.fdk.sparqlservice.model.Dataset;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DatasetRepository extends JpaRepository<Dataset, String> {
    @Query(
            value = """
                    SELECT d.id
                    FROM datasets d
                    LEFT JOIN metadata m 
                      ON m.id = CONCAT('latest-sync-dataset-', d.id)
                    WHERE m.id IS NULL
                       OR (m.value ~ '^[0-9]+$' AND d.timestamp > m.value::bigint)
                    """,
            nativeQuery = true
    )
    List<String> findNonSynchronizedDatasets(Pageable pageable);
}
