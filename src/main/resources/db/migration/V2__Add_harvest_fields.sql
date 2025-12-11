-- Add harvest_run_id and pending_harvest_event columns to all resource tables

ALTER TABLE concepts 
    ADD COLUMN IF NOT EXISTS harvest_run_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS pending_harvest_event BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE datasets 
    ADD COLUMN IF NOT EXISTS harvest_run_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS pending_harvest_event BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE data_services 
    ADD COLUMN IF NOT EXISTS harvest_run_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS pending_harvest_event BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE events 
    ADD COLUMN IF NOT EXISTS harvest_run_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS pending_harvest_event BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE information_models 
    ADD COLUMN IF NOT EXISTS harvest_run_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS pending_harvest_event BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE services 
    ADD COLUMN IF NOT EXISTS harvest_run_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS pending_harvest_event BOOLEAN NOT NULL DEFAULT false;
