package no.fdk.sparqlservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
@Table(name = "services")
public class Service {
    @Id
    private String id;

    @Lob
    private String graph;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;
}
