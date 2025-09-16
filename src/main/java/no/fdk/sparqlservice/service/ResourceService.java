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
    public void saveConcept(String fdkId, String graph, long timestamp) {
        conceptRepository.findById(fdkId).ifPresentOrElse(
                concept -> {
                    concept.setGraph(graph.getBytes(StandardCharsets.UTF_8));
                    concept.setTimestamp(timestamp);
                    concept.setRemoved(false);
                    conceptRepository.save(concept);
                },
                () -> conceptRepository.save(new Concept(fdkId, graph.getBytes(StandardCharsets.UTF_8), timestamp, false))
        );
    }

    @Transactional
    public void removeConcept(String fdkId, long timestamp) {
        conceptRepository.findById(fdkId).ifPresentOrElse(
                concept -> {
                    concept.setGraph("".getBytes(StandardCharsets.UTF_8));
                    concept.setTimestamp(timestamp);
                    concept.setRemoved(true);
                    conceptRepository.save(concept);
                },
                () -> conceptRepository.save(new Concept(fdkId, "".getBytes(StandardCharsets.UTF_8), timestamp, true))
        );
    }

    public List<Concept> findNonSyncedConcepts(Pageable pageable) {
        return conceptRepository.findNonSynchronizedConcepts(pageable);
    }

    @Transactional
    public void saveDataService(String fdkId, String graph, long timestamp) {
        dataServiceRepository.findById(fdkId).ifPresentOrElse(
                dataService -> {
                    dataService.setGraph(graph.getBytes(StandardCharsets.UTF_8));
                    dataService.setTimestamp(timestamp);
                    dataService.setRemoved(false);
                    dataServiceRepository.save(dataService);
                },
                () -> dataServiceRepository.save(new DataService(fdkId, graph.getBytes(StandardCharsets.UTF_8), timestamp, false))
        );
    }

    @Transactional
    public void removeDataService(String fdkId, long timestamp) {
        dataServiceRepository.findById(fdkId).ifPresentOrElse(
                dataService -> {
                    dataService.setGraph("".getBytes(StandardCharsets.UTF_8));
                    dataService.setTimestamp(timestamp);
                    dataService.setRemoved(true);
                    dataServiceRepository.save(dataService);
                },
                () -> dataServiceRepository.save(new DataService(fdkId, "".getBytes(StandardCharsets.UTF_8), timestamp, true))
        );
    }

    public List<DataService> findNonSyncedDataServices(Pageable pageable) {
        return dataServiceRepository.findNonSynchronizedDataServices(pageable);
    }

    @Transactional
    public void saveDataset(String fdkId, String graph, long timestamp) {
        datasetRepository.findById(fdkId).ifPresentOrElse(
                dataset -> {
                    dataset.setGraph(graph.getBytes(StandardCharsets.UTF_8));
                    dataset.setTimestamp(timestamp);
                    dataset.setRemoved(false);
                    datasetRepository.save(dataset);
                },
                () -> datasetRepository.save(new Dataset(fdkId, graph.getBytes(StandardCharsets.UTF_8), timestamp, false))
        );
    }

    @Transactional
    public void removeDataset(String fdkId, long timestamp) {
        datasetRepository.findById(fdkId).ifPresentOrElse(
                dataset -> {
                    dataset.setGraph("".getBytes(StandardCharsets.UTF_8));
                    dataset.setTimestamp(timestamp);
                    dataset.setRemoved(true);
                    datasetRepository.save(dataset);
                },
                () -> datasetRepository.save(new Dataset(fdkId, "".getBytes(StandardCharsets.UTF_8), timestamp, true))
        );
    }

    public List<Dataset> findNonSyncedDatasets(Pageable pageable) {
        return datasetRepository.findNonSynchronizedDatasets(pageable);
    }

    @Transactional
    public void saveEvent(String fdkId, String graph, long timestamp) {
        eventRepository.findById(fdkId).ifPresentOrElse(
                fdkEvent -> {
                    fdkEvent.setGraph(graph.getBytes(StandardCharsets.UTF_8));
                    fdkEvent.setTimestamp(timestamp);
                    fdkEvent.setRemoved(false);
                    eventRepository.save(fdkEvent);
                },
                () -> eventRepository.save(new Event(fdkId, graph.getBytes(StandardCharsets.UTF_8), timestamp, false))
        );
    }

    @Transactional
    public void removeEvent(String fdkId, long timestamp) {
        eventRepository.findById(fdkId).ifPresentOrElse(
                fdkEvent -> {
                    fdkEvent.setGraph("".getBytes(StandardCharsets.UTF_8));
                    fdkEvent.setTimestamp(timestamp);
                    fdkEvent.setRemoved(true);
                    eventRepository.save(fdkEvent);
                },
                () -> eventRepository.save(new Event(fdkId, "".getBytes(StandardCharsets.UTF_8), timestamp, true))
        );
    }

    public List<Event> findNonSyncedEvents(Pageable pageable) {
        return eventRepository.findNonSynchronizedEvents(pageable);
    }

    @Transactional
    public void saveInformationModel(String fdkId, String graph, long timestamp) {
        informationModelRepository.findById(fdkId).ifPresentOrElse(
                infoModel -> {
                    infoModel.setGraph(graph.getBytes(StandardCharsets.UTF_8));
                    infoModel.setTimestamp(timestamp);
                    infoModel.setRemoved(false);
                    informationModelRepository.save(infoModel);
                },
                () -> informationModelRepository.save(new InformationModel(fdkId, graph.getBytes(StandardCharsets.UTF_8), timestamp, false))
        );
    }

    @Transactional
    public void removeInformationModel(String fdkId, long timestamp) {
        informationModelRepository.findById(fdkId).ifPresentOrElse(
                infoModel -> {
                    infoModel.setGraph("".getBytes(StandardCharsets.UTF_8));
                    infoModel.setTimestamp(timestamp);
                    infoModel.setRemoved(true);
                    informationModelRepository.save(infoModel);
                },
                () -> informationModelRepository.save(new InformationModel(fdkId, "".getBytes(StandardCharsets.UTF_8), timestamp, true))
        );
    }

    public List<InformationModel> findNonSyncedInformationModels(Pageable pageable) {
        return informationModelRepository.findNonSynchronizedInformationModels(pageable);
    }

    @Transactional
    public void saveService(String fdkId, String graph, long timestamp) {
        serviceRepository.findById(fdkId).ifPresentOrElse(
                service -> {
                    service.setGraph(graph.getBytes(StandardCharsets.UTF_8));
                    service.setTimestamp(timestamp);
                    service.setRemoved(false);
                    serviceRepository.save(service);
                },
                () -> serviceRepository.save(new no.fdk.sparqlservice.model.Service(fdkId, graph.getBytes(StandardCharsets.UTF_8), timestamp, false))
        );
    }

    @Transactional
    public void removeService(String fdkId, long timestamp) {
        serviceRepository.findById(fdkId).ifPresentOrElse(
                service -> {
                    service.setGraph("".getBytes(StandardCharsets.UTF_8));
                    service.setTimestamp(timestamp);
                    service.setRemoved(true);
                    serviceRepository.save(service);
                },
                () -> serviceRepository.save(new no.fdk.sparqlservice.model.Service(fdkId, "".getBytes(StandardCharsets.UTF_8), timestamp, true))
        );
    }

    public List<no.fdk.sparqlservice.model.Service> findNonSyncedServices(Pageable pageable) {
        return serviceRepository.findNonSynchronizedServices(pageable);
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
