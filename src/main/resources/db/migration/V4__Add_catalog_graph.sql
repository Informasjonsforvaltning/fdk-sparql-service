-- Add catalog_graph column to all resource tables that have a graph column

ALTER TABLE concepts
    ADD COLUMN IF NOT EXISTS catalog_graph BYTEA;

ALTER TABLE datasets
    ADD COLUMN IF NOT EXISTS catalog_graph BYTEA;

ALTER TABLE data_services
    ADD COLUMN IF NOT EXISTS catalog_graph BYTEA;

ALTER TABLE events
    ADD COLUMN IF NOT EXISTS catalog_graph BYTEA;

ALTER TABLE information_models
    ADD COLUMN IF NOT EXISTS catalog_graph BYTEA;

ALTER TABLE services
    ADD COLUMN IF NOT EXISTS catalog_graph BYTEA;
