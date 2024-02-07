package no.fdk.sparqlservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fdk.sparqlservice.model.CatalogType;
import no.fdk.sparqlservice.model.Concept;
import no.fdk.sparqlservice.model.DataService;
import no.fdk.sparqlservice.model.Dataset;
import no.fdk.sparqlservice.model.Event;
import no.fdk.sparqlservice.model.InformationModel;
import no.fdk.sparqlservice.repository.ConceptRepository;
import no.fdk.sparqlservice.repository.DataServiceRepository;
import no.fdk.sparqlservice.repository.DatasetRepository;
import no.fdk.sparqlservice.repository.EventRepository;
import no.fdk.sparqlservice.repository.InformationModelRepository;
import no.fdk.sparqlservice.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {
    private final ConceptRepository conceptRepository;
    private final DataServiceRepository dataServiceRepository;
    private final DatasetRepository datasetRepository;
    private final EventRepository eventRepository;
    private final InformationModelRepository informationModelRepository;
    private final ServiceRepository serviceRepository;

    private final Set<CatalogType> UPDATED_CATALOGS = new HashSet<>(
            Arrays.asList(
                    CatalogType.CONCEPTS,
                    CatalogType.DATA_SERVICES,
                    CatalogType.DATASETS,
                    CatalogType.EVENTS,
                    CatalogType.INFORMATION_MODELS,
                    CatalogType.SERVICES
            )
    );

    public Set<CatalogType> getAndResetUpdatedCatalogs() {
        Set<CatalogType> updatedCatalogs = new HashSet<>(UPDATED_CATALOGS);
        UPDATED_CATALOGS.clear();
        return  updatedCatalogs;
    }

    public void saveConcept(String fdkId, String graph, long timestamp) {
        Concept concept = new Concept(fdkId, graph, timestamp);
        conceptRepository.save(concept);
        UPDATED_CATALOGS.add(CatalogType.CONCEPTS);
    }
    public List<Concept> findAllConcepts() {
        return conceptRepository.findAll();
    }

    public void deleteConcept(String fdkId) {
        if (conceptRepository.existsById(fdkId)) {
            conceptRepository.deleteById(fdkId);
        }
    }

    public void saveDataService(String fdkId, String graph, long timestamp) {
        DataService dataService = new DataService(fdkId, graph, timestamp);
        dataServiceRepository.save(dataService);
        UPDATED_CATALOGS.add(CatalogType.DATA_SERVICES);
    }

    public List<DataService> findAllDataServices() {
        return dataServiceRepository.findAll();
    }

    public void deleteDataService(String fdkId) {
        if (dataServiceRepository.existsById(fdkId)) {
            dataServiceRepository.deleteById(fdkId);
        }
    }

    public void saveDataset(String fdkId, String graph, long timestamp) {
        Dataset dataset = new Dataset(fdkId, graph, timestamp);
        datasetRepository.save(dataset);
        UPDATED_CATALOGS.add(CatalogType.DATASETS);
    }

    public List<Dataset> findAllDatasets() {
        return datasetRepository.findAll();
    }

    public void deleteDataset(String fdkId) {
        if (datasetRepository.existsById(fdkId)) {
            datasetRepository.deleteById(fdkId);
        }
    }

    public void saveEvent(String fdkId, String graph, long timestamp) {
        Event fdkEvent = new Event(fdkId, graph, timestamp);
        eventRepository.save(fdkEvent);
        UPDATED_CATALOGS.add(CatalogType.EVENTS);
    }

    public List<Event> findAllEvents() {
        return eventRepository.findAll();
    }

    public void deleteEvent(String fdkId) {
        if (eventRepository.existsById(fdkId)) {
            eventRepository.deleteById(fdkId);
        }
    }

    public void saveInformationModel(String fdkId, String graph, long timestamp) {
        InformationModel infoModel = new InformationModel(fdkId, graph, timestamp);
        informationModelRepository.save(infoModel);
        UPDATED_CATALOGS.add(CatalogType.INFORMATION_MODELS);
    }

    public List<InformationModel> findAllInformationModels() {
        return informationModelRepository.findAll();
    }

    public void deleteInformationModel(String fdkId) {
        if (informationModelRepository.existsById(fdkId)) {
            informationModelRepository.deleteById(fdkId);
        }
    }

    public void saveService(String fdkId, String graph, long timestamp) {
        no.fdk.sparqlservice.model.Service service = new no.fdk.sparqlservice.model.Service(fdkId, graph, timestamp);
        serviceRepository.save(service);
        UPDATED_CATALOGS.add(CatalogType.SERVICES);
    }

    public List<no.fdk.sparqlservice.model.Service> findAllServices() {
        return serviceRepository.findAll();
    }

    public void deleteService(String fdkId) {
        if (serviceRepository.existsById(fdkId)) {
            serviceRepository.deleteById(fdkId);
        }
    }

}
