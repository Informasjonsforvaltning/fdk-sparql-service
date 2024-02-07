package no.fdk.sparqlservice.repository;

import no.fdk.sparqlservice.model.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<Dataset, String> { }
