package no.fdk.sparqlservice.repository;

import no.fdk.sparqlservice.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, String> { }
