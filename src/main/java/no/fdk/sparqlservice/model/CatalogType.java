package no.fdk.sparqlservice.model;

public enum CatalogType {
    CONCEPTS(
            "CONCEPT_REASONED",
            "CONCEPT_REMOVED",
            "latest-sync-concept-",
            "concept",
            "concept-event-cb"
    ),
    DATA_SERVICES(
            "DATA_SERVICE_REASONED",
            "DATA_SERVICE_REMOVED",
            "latest-sync-data-service-",
            "data service",
            "data-service-event-cb"
    ),
    DATASETS(
            "DATASET_REASONED",
            "DATASET_REMOVED",
            "latest-sync-dataset-",
            "dataset",
            "dataset-event-cb"
    ),
    EVENTS(
            "EVENT_REASONED",
            "EVENT_REMOVED",
            "latest-sync-event-",
            "event",
            "event-event-cb"
    ),
    INFORMATION_MODELS(
            "INFORMATION_MODEL_REASONED",
            "INFORMATION_MODEL_REMOVED",
            "latest-sync-information-model-",
            "information model",
            "information-model-event-cb"
    ),
    SERVICES(
            "SERVICE_REASONED",
            "SERVICE_REMOVED",
            "latest-sync-service-",
            "service",
            "service-event-cb"
    );

    private final String reasonedEventType;
    private final String removedEventType;
    private final String syncKeyPrefix;
    private final String logLabel;
    private final String circuitBreakerName;

    CatalogType(
            String reasonedEventType,
            String removedEventType,
            String syncKeyPrefix,
            String logLabel,
            String circuitBreakerName
    ) {
        this.reasonedEventType = reasonedEventType;
        this.removedEventType = removedEventType;
        this.syncKeyPrefix = syncKeyPrefix;
        this.logLabel = logLabel;
        this.circuitBreakerName = circuitBreakerName;
    }

    public String reasonedEventType() {
        return reasonedEventType;
    }

    public String removedEventType() {
        return removedEventType;
    }

    public String syncKeyPrefix() {
        return syncKeyPrefix;
    }

    public String logLabel() {
        return logLabel;
    }

    public String circuitBreakerName() {
        return circuitBreakerName;
    }
}
