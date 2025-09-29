package no.fdk.sparqlservice.repository;

import no.fdk.sparqlservice.model.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, String> {
    @Query(
            value = """
                    SELECT s.id
                    FROM services s
                    LEFT JOIN metadata m 
                      ON m.id = CONCAT('latest-sync-service-', s.id)
                    WHERE m.id IS NULL
                       OR (m.value ~ '^[0-9]+$' AND s.timestamp > m.value::bigint)
                    """,
            nativeQuery = true
    )
    List<String> findNonSynchronizedServices(Pageable pageable);
}
