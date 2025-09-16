package no.fdk.sparqlservice.service;

import lombok.RequiredArgsConstructor;
import no.fdk.sparqlservice.model.*;
import no.fdk.sparqlservice.repository.MetadataRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncDataService {
    private final MetadataRepository metadataRepository;

    private String syncId(CatalogType catalogType, String fdkId) {
        String base = switch (catalogType) {
            case CONCEPTS -> "latest-sync-concept-";
            case DATA_SERVICES -> "latest-sync-data-service-";
            case DATASETS -> "latest-sync-dataset-";
            case EVENTS -> "latest-sync-event-";
            case INFORMATION_MODELS -> "latest-sync-information-model-";
            case SERVICES -> "latest-sync-service-";
        };

        return base + fdkId;
    }

    public void updateLatestSync(CatalogType catalogType, String fdkId, long timestamp) {
        Metadata metadata = new Metadata(syncId(catalogType, fdkId), Long.toString(timestamp));
        metadataRepository.save(metadata);
    }

}
