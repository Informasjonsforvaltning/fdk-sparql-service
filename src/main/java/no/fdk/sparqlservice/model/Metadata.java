package no.fdk.sparqlservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
@Table(name = "metadata")
public class Metadata {
    @Id
    private String id;

    @Column(name = "value", nullable = false)
    private String value;
}
