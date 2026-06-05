package no.fdk.sparqlservice.service;

import lombok.RequiredArgsConstructor;
import no.fdk.sparqlservice.model.CatalogType;
import no.fdk.sparqlservice.model.Metadata;
import no.fdk.sparqlservice.repository.MetadataRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncDataService {
    private final MetadataRepository metadataRepository;

    private String syncId(CatalogType catalogType, String fdkId) {
        return catalogType.syncKeyPrefix() + fdkId;
    }

    public void updateLatestSync(CatalogType catalogType, String fdkId, long timestamp) {
        Metadata metadata = new Metadata(syncId(catalogType, fdkId), Long.toString(timestamp));
        metadataRepository.save(metadata);
    }

}
