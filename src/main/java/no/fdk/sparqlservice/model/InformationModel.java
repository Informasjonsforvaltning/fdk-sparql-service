package no.fdk.sparqlservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
@Table(name = "information_models")
public class InformationModel {
    @Id
    private String id;

    @Lob
    private String graph;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;
}
