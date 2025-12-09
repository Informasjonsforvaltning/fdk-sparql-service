package no.fdk.sparqlservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
@Table(name = "services")
public class Service {
    @Id
    private String id;

    private byte[] graph;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    @Column(name = "removed", nullable = false)
    private boolean removed;

    @Column(name = "harvest_run_id")
    private String harvestRunId;

    @Column(name = "pending_harvest_event", nullable = false)
    private boolean pendingHarvestEvent = false;
}
