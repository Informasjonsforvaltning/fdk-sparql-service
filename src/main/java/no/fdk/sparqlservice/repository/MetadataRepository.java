package no.fdk.sparqlservice.repository;

import no.fdk.sparqlservice.model.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetadataRepository extends JpaRepository<Metadata, String> { }
