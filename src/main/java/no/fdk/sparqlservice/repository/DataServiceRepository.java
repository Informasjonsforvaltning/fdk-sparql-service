package no.fdk.sparqlservice.repository;

import no.fdk.sparqlservice.model.DataService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataServiceRepository extends JpaRepository<DataService, String> { }
