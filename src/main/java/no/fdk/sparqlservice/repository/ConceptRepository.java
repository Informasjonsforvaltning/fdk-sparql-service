package no.fdk.sparqlservice.repository;

import no.fdk.sparqlservice.model.Concept;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConceptRepository extends JpaRepository<Concept, String> {
    @Query(
            value = """
                    SELECT c.id, c.graph, c.removed, c.timestamp
                    FROM concepts c
                    LEFT JOIN metadata m
                      ON m.id = CONCAT('latest-sync-concept-', c.id)
                    WHERE m.id IS NULL
                       OR (m.value ~ '^[0-9]+$' AND c.timestamp > m.value::bigint)
                    """,
            nativeQuery = true
    )
    List<Concept> findNonSynchronizedConcepts(Pageable pageable);
}
