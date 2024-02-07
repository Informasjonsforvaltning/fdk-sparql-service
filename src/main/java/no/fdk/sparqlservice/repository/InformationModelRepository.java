package no.fdk.sparqlservice.repository;

import no.fdk.sparqlservice.model.InformationModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformationModelRepository extends JpaRepository<InformationModel, String> { }
