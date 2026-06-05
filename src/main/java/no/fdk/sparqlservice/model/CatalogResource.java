package no.fdk.sparqlservice.model;

public interface CatalogResource {
    String getId();

    byte[] getGraph();

    void setGraph(byte[] graph);

    byte[] getCatalogGraph();

    void setCatalogGraph(byte[] catalogGraph);

    long getTimestamp();

    void setTimestamp(long timestamp);

    boolean isRemoved();

    void setRemoved(boolean removed);

    String getHarvestRunId();

    void setHarvestRunId(String harvestRunId);

    boolean isPendingHarvestEvent();

    void setPendingHarvestEvent(boolean pendingHarvestEvent);
}
