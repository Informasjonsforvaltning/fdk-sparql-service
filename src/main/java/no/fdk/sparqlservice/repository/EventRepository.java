package no.fdk.sparqlservice.repository;

import no.fdk.sparqlservice.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, String> {
    @Query(
            value = """
                    SELECT e.id
                    FROM events e
                    LEFT JOIN metadata m 
                      ON m.id = CONCAT('latest-sync-event-', e.id)
                    WHERE m.id IS NULL
                       OR (m.value ~ '^[0-9]+$' AND e.timestamp > m.value::bigint)
                    """,
            nativeQuery = true
    )
    List<String> findNonSynchronizedEvents(Pageable pageable);
}
