package no.fdk.sparqlservice.repository;

import no.fdk.sparqlservice.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, String> { }
