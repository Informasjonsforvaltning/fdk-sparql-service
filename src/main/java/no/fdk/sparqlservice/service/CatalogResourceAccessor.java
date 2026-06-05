package no.fdk.sparqlservice.service;

import no.fdk.sparqlservice.model.CatalogResource;
import no.fdk.sparqlservice.model.CatalogType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogResourceAccessor<T extends CatalogResource> {
    CatalogType type();

    JpaRepository<T, String> repository();

    T newEntity(
            String id,
            byte[] graph,
            byte[] catalogGraph,
            long timestamp,
            boolean removed,
            String harvestRunId,
            boolean pendingHarvestEvent
    );

    List<String> findNonSynced(Pageable pageable);
}
