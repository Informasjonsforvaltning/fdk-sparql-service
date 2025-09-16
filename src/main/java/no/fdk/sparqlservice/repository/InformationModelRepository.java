package no.fdk.sparqlservice.repository;

import no.fdk.sparqlservice.model.InformationModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InformationModelRepository extends JpaRepository<InformationModel, String> {
    @Query(
            value = """
                    SELECT i.id, i.graph, i.removed, i.timestamp
                    FROM information_models i
                    LEFT JOIN metadata m
                      ON m.id = CONCAT('latest-sync-information-model-', i.id)
                    WHERE m.id IS NULL
                       OR (m.value ~ '^[0-9]+$' AND i.timestamp > m.value::bigint)
                    """,
            nativeQuery = true
    )
    List<InformationModel> findNonSynchronizedInformationModels(Pageable pageable);
}
