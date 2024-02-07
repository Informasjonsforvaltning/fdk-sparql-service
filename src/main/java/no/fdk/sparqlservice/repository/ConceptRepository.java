package no.fdk.sparqlservice.repository;

import no.fdk.sparqlservice.model.Concept;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConceptRepository extends JpaRepository<Concept, String> { }
