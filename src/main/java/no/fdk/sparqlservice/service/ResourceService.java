package no.fdk.sparqlservice.service;

import lombok.RequiredArgsConstructor;
import no.fdk.sparqlservice.model.CatalogResource;
import no.fdk.sparqlservice.model.CatalogType;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private final CatalogTypeRegistry catalogTypeRegistry;

    private static final byte[] EMPTY_BYTES = new byte[0];

    private static byte[] toBytes(String value) {
        return value != null ? value.getBytes(StandardCharsets.UTF_8) : EMPTY_BYTES;
    }

    @Transactional
    public void save(
            CatalogType type,
            String fdkId,
            String graph,
            long timestamp,
            String harvestRunId,
            String catalogGraph
    ) {
        saveEntity(
                catalogTypeRegistry.get(type),
                fdkId,
                toBytes(graph),
                toBytes(catalogGraph),
                timestamp,
                harvestRunId
        );
    }

    @Transactional
    public void remove(CatalogType type, String fdkId, long timestamp, String harvestRunId) {
        removeEntity(catalogTypeRegistry.get(type), fdkId, timestamp, harvestRunId);
    }

    public List<String> findNonSynced(CatalogType type, Pageable pageable) {
        return catalogTypeRegistry.get(type).findNonSynced(pageable);
    }

    public Optional<? extends CatalogResource> findById(CatalogType type, String fdkId) {
        return catalogTypeRegistry.get(type).repository().findById(fdkId);
    }

    @Transactional
    public void clearPendingHarvestEvent(String fdkId, CatalogType type) {
        clearPendingHarvestEventEntity(catalogTypeRegistry.get(type), fdkId);
    }

    public boolean timestampIsHigherThanSaved(String fdkId, long timestamp, CatalogType type) {
        Optional<? extends CatalogResource> existing = findById(type, fdkId);
        if (existing.isEmpty()) {
            return true;
        }
        return timestamp > existing.get().getTimestamp();
    }

    private <T extends CatalogResource> void saveEntity(
            CatalogResourceAccessor<T> accessor,
            String fdkId,
            byte[] graphBytes,
            byte[] catalogGraphBytes,
            long timestamp,
            String harvestRunId
    ) {
        boolean pendingHarvestEvent = harvestRunId != null;
        accessor.repository().findById(fdkId).ifPresentOrElse(
                entity -> {
                    applySaveFields(entity, graphBytes, catalogGraphBytes, timestamp, harvestRunId, pendingHarvestEvent);
                    accessor.repository().save(entity);
                },
                () -> accessor.repository().save(accessor.newEntity(
                        fdkId, graphBytes, catalogGraphBytes, timestamp, false, harvestRunId, pendingHarvestEvent
                ))
        );
    }

    private <T extends CatalogResource> void removeEntity(
            CatalogResourceAccessor<T> accessor,
            String fdkId,
            long timestamp,
            String harvestRunId
    ) {
        boolean pendingHarvestEvent = harvestRunId != null;
        accessor.repository().findById(fdkId).ifPresentOrElse(
                entity -> {
                    applyRemoveFields(entity, timestamp, harvestRunId, pendingHarvestEvent);
                    accessor.repository().save(entity);
                },
                () -> accessor.repository().save(accessor.newEntity(
                        fdkId, EMPTY_BYTES, EMPTY_BYTES, timestamp, true, harvestRunId, pendingHarvestEvent
                ))
        );
    }

    private <T extends CatalogResource> void clearPendingHarvestEventEntity(
            CatalogResourceAccessor<T> accessor,
            String fdkId
    ) {
        accessor.repository().findById(fdkId).ifPresent(entity -> {
            entity.setPendingHarvestEvent(false);
            accessor.repository().save(entity);
        });
    }

    private static void applySaveFields(
            CatalogResource entity,
            byte[] graphBytes,
            byte[] catalogGraphBytes,
            long timestamp,
            String harvestRunId,
            boolean pendingHarvestEvent
    ) {
        entity.setGraph(graphBytes);
        entity.setCatalogGraph(catalogGraphBytes);
        entity.setTimestamp(timestamp);
        entity.setRemoved(false);
        entity.setHarvestRunId(harvestRunId);
        entity.setPendingHarvestEvent(pendingHarvestEvent);
    }

    private static void applyRemoveFields(
            CatalogResource entity,
            long timestamp,
            String harvestRunId,
            boolean pendingHarvestEvent
    ) {
        entity.setGraph(EMPTY_BYTES);
        entity.setCatalogGraph(EMPTY_BYTES);
        entity.setTimestamp(timestamp);
        entity.setRemoved(true);
        entity.setHarvestRunId(harvestRunId);
        entity.setPendingHarvestEvent(pendingHarvestEvent);
    }
}
