package no.fdk.sparqlservice.service;

import lombok.RequiredArgsConstructor;
import no.fdk.sparqlservice.model.*;
import no.fdk.sparqlservice.repository.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ConceptRepository conceptRepository;
    private final DataServiceRepository dataServiceRepository;
    private final DatasetRepository datasetRepository;
    private final EventRepository eventRepository;
    private final InformationModelRepository informationModelRepository;
    private final ServiceRepository serviceRepository;

    @Transactional
    public void saveConcept(String fdkId, String graph, long timestamp, String harvestRunId) {
        conceptRepository.findById(fdkId).ifPresentOrElse(
                concept -> {
                    concept.setGraph(graph.getBytes(StandardCharsets.UTF_8));
                    concept.setTimestamp(timestamp);
                    concept.setRemoved(false);
                    concept.setHarvestRunId(harvestRunId);
                    concept.setPendingHarvestEvent(harvestRunId != null);
                    conceptRepository.save(concept);
                },
                () -> conceptRepository.save(new Concept(fdkId, graph.getBytes(StandardCharsets.UTF_8), timestamp, false, harvestRunId, harvestRunId != null))
        );
    }

    @Transactional
    public void removeConcept(String fdkId, long timestamp, String harvestRunId) {
        conceptRepository.findById(fdkId).ifPresentOrElse(
                concept -> {
                    concept.setTimestamp(timestamp);
                    concept.setRemoved(true);
                    concept.setHarvestRunId(harvestRunId);
                    concept.setPendingHarvestEvent(harvestRunId != null);
                    conceptRepository.save(concept);
                },
                () -> conceptRepository.save(new Concept(fdkId, "".getBytes(StandardCharsets.UTF_8), timestamp, true, harvestRunId, harvestRunId != null))
        );
    }

    public List<String> findNonSyncedConcepts(Pageable pageable) {
        return conceptRepository.findNonSynchronizedConcepts(pageable);
    }

    public Optional<Concept> findConceptById(String fdkId) {
        return conceptRepository.findById(fdkId);
    }

    @Transactional
    public void saveDataService(String fdkId, String graph, long timestamp, String harvestRunId) {
        dataServiceRepository.findById(fdkId).ifPresentOrElse(
                dataService -> {
                    dataService.setGraph(graph.getBytes(StandardCharsets.UTF_8));
                    dataService.setTimestamp(timestamp);
                    dataService.setRemoved(false);
                    dataService.setHarvestRunId(harvestRunId);
                    dataService.setPendingHarvestEvent(harvestRunId != null);
                    dataServiceRepository.save(dataService);
                },
                () -> dataServiceRepository.save(new DataService(fdkId, graph.getBytes(StandardCharsets.UTF_8), timestamp, false, harvestRunId, harvestRunId != null))
        );
    }

    @Transactional
    public void removeDataService(String fdkId, long timestamp, String harvestRunId) {
        dataServiceRepository.findById(fdkId).ifPresentOrElse(
                dataService -> {
                    dataService.setTimestamp(timestamp);
                    dataService.setRemoved(true);
                    dataService.setHarvestRunId(harvestRunId);
                    dataService.setPendingHarvestEvent(harvestRunId != null);
                    dataServiceRepository.save(dataService);
                },
                () -> dataServiceRepository.save(new DataService(fdkId, "".getBytes(StandardCharsets.UTF_8), timestamp, true, harvestRunId, harvestRunId != null))
        );
    }

    public List<String> findNonSyncedDataServices(Pageable pageable) {
        return dataServiceRepository.findNonSynchronizedDataServices(pageable);
    }

    public Optional<DataService> findDataServiceById(String fdkId) {
        return dataServiceRepository.findById(fdkId);
    }

    @Transactional
    public void saveDataset(String fdkId, String graph, long timestamp, String harvestRunId) {
        datasetRepository.findById(fdkId).ifPresentOrElse(
                dataset -> {
                    dataset.setGraph(graph.getBytes(StandardCharsets.UTF_8));
                    dataset.setTimestamp(timestamp);
                    dataset.setRemoved(false);
                    dataset.setHarvestRunId(harvestRunId);
                    dataset.setPendingHarvestEvent(harvestRunId != null);
                    datasetRepository.save(dataset);
                },
                () -> datasetRepository.save(new Dataset(fdkId, graph.getBytes(StandardCharsets.UTF_8), timestamp, false, harvestRunId, harvestRunId != null))
        );
    }

    @Transactional
    public void removeDataset(String fdkId, long timestamp, String harvestRunId) {
        datasetRepository.findById(fdkId).ifPresentOrElse(
                dataset -> {
                    dataset.setTimestamp(timestamp);
                    dataset.setRemoved(true);
                    dataset.setHarvestRunId(harvestRunId);
                    dataset.setPendingHarvestEvent(harvestRunId != null);
                    datasetRepository.save(dataset);
                },
                () -> datasetRepository.save(new Dataset(fdkId, "".getBytes(StandardCharsets.UTF_8), timestamp, true, harvestRunId, harvestRunId != null))
        );
    }

    public List<String> findNonSyncedDatasets(Pageable pageable) {
        return datasetRepository.findNonSynchronizedDatasets(pageable);
    }

    public Optional<Dataset> findDatasetById(String fdkId) {
        return datasetRepository.findById(fdkId);
    }

    @Transactional
    public void saveEvent(String fdkId, String graph, long timestamp, String harvestRunId) {
        eventRepository.findById(fdkId).ifPresentOrElse(
                fdkEvent -> {
                    fdkEvent.setGraph(graph.getBytes(StandardCharsets.UTF_8));
                    fdkEvent.setTimestamp(timestamp);
                    fdkEvent.setRemoved(false);
                    fdkEvent.setHarvestRunId(harvestRunId);
                    fdkEvent.setPendingHarvestEvent(harvestRunId != null);
                    eventRepository.save(fdkEvent);
                },
                () -> eventRepository.save(new Event(fdkId, graph.getBytes(StandardCharsets.UTF_8), timestamp, false, harvestRunId, harvestRunId != null))
        );
    }

    @Transactional
    public void removeEvent(String fdkId, long timestamp, String harvestRunId) {
        eventRepository.findById(fdkId).ifPresentOrElse(
                fdkEvent -> {
                    fdkEvent.setTimestamp(timestamp);
                    fdkEvent.setRemoved(true);
                    fdkEvent.setHarvestRunId(harvestRunId);
                    fdkEvent.setPendingHarvestEvent(harvestRunId != null);
                    eventRepository.save(fdkEvent);
                },
                () -> eventRepository.save(new Event(fdkId, "".getBytes(StandardCharsets.UTF_8), timestamp, true, harvestRunId, harvestRunId != null))
        );
    }

    public List<String> findNonSyncedEvents(Pageable pageable) {
        return eventRepository.findNonSynchronizedEvents(pageable);
    }

    public Optional<Event> findEventById(String fdkId) {
        return eventRepository.findById(fdkId);
    }

    @Transactional
    public void saveInformationModel(String fdkId, String graph, long timestamp, String harvestRunId) {
        informationModelRepository.findById(fdkId).ifPresentOrElse(
                infoModel -> {
                    infoModel.setGraph(graph.getBytes(StandardCharsets.UTF_8));
                    infoModel.setTimestamp(timestamp);
                    infoModel.setRemoved(false);
                    infoModel.setHarvestRunId(harvestRunId);
                    infoModel.setPendingHarvestEvent(harvestRunId != null);
                    informationModelRepository.save(infoModel);
                },
                () -> informationModelRepository.save(new InformationModel(fdkId, graph.getBytes(StandardCharsets.UTF_8), timestamp, false, harvestRunId, harvestRunId != null))
        );
    }

    @Transactional
    public void removeInformationModel(String fdkId, long timestamp, String harvestRunId) {
        informationModelRepository.findById(fdkId).ifPresentOrElse(
                infoModel -> {
                    infoModel.setTimestamp(timestamp);
                    infoModel.setRemoved(true);
                    infoModel.setHarvestRunId(harvestRunId);
                    infoModel.setPendingHarvestEvent(harvestRunId != null);
                    informationModelRepository.save(infoModel);
                },
                () -> informationModelRepository.save(new InformationModel(fdkId, "".getBytes(StandardCharsets.UTF_8), timestamp, true, harvestRunId, harvestRunId != null))
        );
    }

    public List<String> findNonSyncedInformationModels(Pageable pageable) {
        return informationModelRepository.findNonSynchronizedInformationModels(pageable);
    }

    public Optional<InformationModel> findInformationModelById(String fdkId) {
        return informationModelRepository.findById(fdkId);
    }

    @Transactional
    public void saveService(String fdkId, String graph, long timestamp, String harvestRunId) {
        serviceRepository.findById(fdkId).ifPresentOrElse(
                service -> {
                    service.setGraph(graph.getBytes(StandardCharsets.UTF_8));
                    service.setTimestamp(timestamp);
                    service.setRemoved(false);
                    service.setHarvestRunId(harvestRunId);
                    service.setPendingHarvestEvent(harvestRunId != null);
                    serviceRepository.save(service);
                },
                () -> serviceRepository.save(new no.fdk.sparqlservice.model.Service(fdkId, graph.getBytes(StandardCharsets.UTF_8), timestamp, false, harvestRunId, harvestRunId != null))
        );
    }

    @Transactional
    public void removeService(String fdkId, long timestamp, String harvestRunId) {
        serviceRepository.findById(fdkId).ifPresentOrElse(
                service -> {
                    service.setTimestamp(timestamp);
                    service.setRemoved(true);
                    service.setHarvestRunId(harvestRunId);
                    service.setPendingHarvestEvent(harvestRunId != null);
                    serviceRepository.save(service);
                },
                () -> serviceRepository.save(new no.fdk.sparqlservice.model.Service(fdkId, "".getBytes(StandardCharsets.UTF_8), timestamp, true, harvestRunId, harvestRunId != null))
        );
    }

    public List<String> findNonSyncedServices(Pageable pageable) {
        return serviceRepository.findNonSynchronizedServices(pageable);
    }

    public Optional<no.fdk.sparqlservice.model.Service> findServiceById(String fdkId) {
        return serviceRepository.findById(fdkId);
    }

    @Transactional
    public void clearPendingHarvestEvent(String fdkId, CatalogType type) {
        switch (type) {
            case CONCEPTS:
                conceptRepository.findById(fdkId).ifPresent(concept -> {
                    concept.setPendingHarvestEvent(false);
                    conceptRepository.save(concept);
                });
                break;
            case DATA_SERVICES:
                dataServiceRepository.findById(fdkId).ifPresent(dataService -> {
                    dataService.setPendingHarvestEvent(false);
                    dataServiceRepository.save(dataService);
                });
                break;
            case DATASETS:
                datasetRepository.findById(fdkId).ifPresent(dataset -> {
                    dataset.setPendingHarvestEvent(false);
                    datasetRepository.save(dataset);
                });
                break;
            case EVENTS:
                eventRepository.findById(fdkId).ifPresent(event -> {
                    event.setPendingHarvestEvent(false);
                    eventRepository.save(event);
                });
                break;
            case INFORMATION_MODELS:
                informationModelRepository.findById(fdkId).ifPresent(infoModel -> {
                    infoModel.setPendingHarvestEvent(false);
                    informationModelRepository.save(infoModel);
                });
                break;
            case SERVICES:
                serviceRepository.findById(fdkId).ifPresent(service -> {
                    service.setPendingHarvestEvent(false);
                    serviceRepository.save(service);
                });
                break;
        }
    }

    public boolean timestampIsHigherThanSaved(String fdkId, long timestamp, CatalogType type) {
        Long dbTimestamp = null;
        switch (type) {
            case CONCEPTS:
                Optional<Concept> concept = conceptRepository.findById(fdkId);
                if (concept.isPresent()) {
                    dbTimestamp = concept.get().getTimestamp();
                }
                break;
            case DATA_SERVICES:
                Optional<DataService> dataService = dataServiceRepository.findById(fdkId);
                if (dataService.isPresent()) {
                    dbTimestamp = dataService.get().getTimestamp();
                }
                break;
            case DATASETS:
                Optional<Dataset> dataset = datasetRepository.findById(fdkId);
                if (dataset.isPresent()) {
                    dbTimestamp = dataset.get().getTimestamp();
                }
                break;
            case EVENTS:
                Optional<Event> event = eventRepository.findById(fdkId);
                if (event.isPresent()) {
                    dbTimestamp = event.get().getTimestamp();
                }
                break;
            case INFORMATION_MODELS:
                Optional<InformationModel> informationModel = informationModelRepository.findById(fdkId);
                if (informationModel.isPresent()) {
                    dbTimestamp = informationModel.get().getTimestamp();
                }
                break;
            case SERVICES:
                Optional<no.fdk.sparqlservice.model.Service> service = serviceRepository.findById(fdkId);
                if (service.isPresent()) {
                    dbTimestamp = service.get().getTimestamp();
                }
                break;
        }

        if (dbTimestamp == null) {
            return true;
        } else {
            return timestamp > dbTimestamp;
        }
    }

}
