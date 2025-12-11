-- Add indexes for performance optimization
-- These indexes support the queries used to find non-synchronized resources

-- Indexes on timestamp columns for efficient timestamp comparisons
CREATE INDEX IF NOT EXISTS idx_concepts_timestamp ON concepts(timestamp);
CREATE INDEX IF NOT EXISTS idx_datasets_timestamp ON datasets(timestamp);
CREATE INDEX IF NOT EXISTS idx_data_services_timestamp ON data_services(timestamp);
CREATE INDEX IF NOT EXISTS idx_events_timestamp ON events(timestamp);
CREATE INDEX IF NOT EXISTS idx_information_models_timestamp ON information_models(timestamp);
CREATE INDEX IF NOT EXISTS idx_services_timestamp ON services(timestamp);

-- Indexes on harvest_run_id for filtering by harvest run
CREATE INDEX IF NOT EXISTS idx_concepts_harvest_run_id ON concepts(harvest_run_id);
CREATE INDEX IF NOT EXISTS idx_datasets_harvest_run_id ON datasets(harvest_run_id);
CREATE INDEX IF NOT EXISTS idx_data_services_harvest_run_id ON data_services(harvest_run_id);
CREATE INDEX IF NOT EXISTS idx_events_harvest_run_id ON events(harvest_run_id);
CREATE INDEX IF NOT EXISTS idx_information_models_harvest_run_id ON information_models(harvest_run_id);
CREATE INDEX IF NOT EXISTS idx_services_harvest_run_id ON services(harvest_run_id);

-- Indexes on pending_harvest_event for filtering resources that need harvest events
CREATE INDEX IF NOT EXISTS idx_concepts_pending_harvest_event ON concepts(pending_harvest_event);
CREATE INDEX IF NOT EXISTS idx_datasets_pending_harvest_event ON datasets(pending_harvest_event);
CREATE INDEX IF NOT EXISTS idx_data_services_pending_harvest_event ON data_services(pending_harvest_event);
CREATE INDEX IF NOT EXISTS idx_events_pending_harvest_event ON events(pending_harvest_event);
CREATE INDEX IF NOT EXISTS idx_information_models_pending_harvest_event ON information_models(pending_harvest_event);
CREATE INDEX IF NOT EXISTS idx_services_pending_harvest_event ON services(pending_harvest_event);

-- Composite index on timestamp and removed for common query patterns
CREATE INDEX IF NOT EXISTS idx_concepts_timestamp_removed ON concepts(timestamp, removed);
CREATE INDEX IF NOT EXISTS idx_datasets_timestamp_removed ON datasets(timestamp, removed);
CREATE INDEX IF NOT EXISTS idx_data_services_timestamp_removed ON data_services(timestamp, removed);
CREATE INDEX IF NOT EXISTS idx_events_timestamp_removed ON events(timestamp, removed);
CREATE INDEX IF NOT EXISTS idx_information_models_timestamp_removed ON information_models(timestamp, removed);
CREATE INDEX IF NOT EXISTS idx_services_timestamp_removed ON services(timestamp, removed);
