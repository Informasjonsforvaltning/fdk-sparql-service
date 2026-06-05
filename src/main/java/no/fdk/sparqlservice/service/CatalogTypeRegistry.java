package no.fdk.sparqlservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import no.fdk.sparqlservice.model.*;
import no.fdk.sparqlservice.repository.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class CatalogTypeRegistry {
    private final ConceptRepository conceptRepository;
    private final DataServiceRepository dataServiceRepository;
    private final DatasetRepository datasetRepository;
    private final EventRepository eventRepository;
    private final InformationModelRepository informationModelRepository;
    private final ServiceRepository serviceRepository;

    private Map<CatalogType, CatalogResourceAccessor<?>> accessors;

    @PostConstruct
    void init() {
        accessors = new EnumMap<>(CatalogType.class);
        accessors.put(CatalogType.CONCEPTS, accessor(
                CatalogType.CONCEPTS,
                conceptRepository,
                Concept::new,
                conceptRepository::findNonSynchronizedConcepts
        ));
        accessors.put(CatalogType.DATA_SERVICES, accessor(
                CatalogType.DATA_SERVICES,
                dataServiceRepository,
                DataService::new,
                dataServiceRepository::findNonSynchronizedDataServices
        ));
        accessors.put(CatalogType.DATASETS, accessor(
                CatalogType.DATASETS,
                datasetRepository,
                Dataset::new,
                datasetRepository::findNonSynchronizedDatasets
        ));
        accessors.put(CatalogType.EVENTS, accessor(
                CatalogType.EVENTS,
                eventRepository,
                Event::new,
                eventRepository::findNonSynchronizedEvents
        ));
        accessors.put(CatalogType.INFORMATION_MODELS, accessor(
                CatalogType.INFORMATION_MODELS,
                informationModelRepository,
                InformationModel::new,
                informationModelRepository::findNonSynchronizedInformationModels
        ));
        accessors.put(CatalogType.SERVICES, accessor(
                CatalogType.SERVICES,
                serviceRepository,
                Service::new,
                serviceRepository::findNonSynchronizedServices
        ));
    }

    @SuppressWarnings("unchecked")
    public <T extends CatalogResource> CatalogResourceAccessor<T> get(CatalogType type) {
        CatalogResourceAccessor<?> accessor = accessors.get(type);
        if (accessor == null) {
            throw new IllegalArgumentException("Unknown catalog type: " + type);
        }
        return (CatalogResourceAccessor<T>) accessor;
    }

    private static <T extends CatalogResource> CatalogResourceAccessor<T> accessor(
            CatalogType type,
            org.springframework.data.jpa.repository.JpaRepository<T, String> repository,
            EntityFactory<T> factory,
            Function<Pageable, java.util.List<String>> findNonSynced
    ) {
        return new CatalogResourceAccessor<>() {
            @Override
            public CatalogType type() {
                return type;
            }

            @Override
            public org.springframework.data.jpa.repository.JpaRepository<T, String> repository() {
                return repository;
            }

            @Override
            public T newEntity(
                    String id,
                    byte[] graph,
                    byte[] catalogGraph,
                    long timestamp,
                    boolean removed,
                    String harvestRunId,
                    boolean pendingHarvestEvent
            ) {
                return factory.create(id, graph, catalogGraph, timestamp, removed, harvestRunId, pendingHarvestEvent);
            }

            @Override
            public java.util.List<String> findNonSynced(Pageable pageable) {
                return findNonSynced.apply(pageable);
            }
        };
    }

    @FunctionalInterface
    private interface EntityFactory<T extends CatalogResource> {
        T create(
                String id,
                byte[] graph,
                byte[] catalogGraph,
                long timestamp,
                boolean removed,
                String harvestRunId,
                boolean pendingHarvestEvent
        );
    }
}
