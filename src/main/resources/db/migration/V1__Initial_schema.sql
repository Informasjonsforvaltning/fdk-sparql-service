-- Initial schema creation
-- This migration creates the base tables for the application

CREATE TABLE IF NOT EXISTS concepts (
    id VARCHAR(255) PRIMARY KEY,
    graph BYTEA,
    timestamp BIGINT NOT NULL,
    removed BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS datasets (
    id VARCHAR(255) PRIMARY KEY,
    graph BYTEA,
    timestamp BIGINT NOT NULL,
    removed BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS data_services (
    id VARCHAR(255) PRIMARY KEY,
    graph BYTEA,
    timestamp BIGINT NOT NULL,
    removed BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS events (
    id VARCHAR(255) PRIMARY KEY,
    graph BYTEA,
    timestamp BIGINT NOT NULL,
    removed BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS information_models (
    id VARCHAR(255) PRIMARY KEY,
    graph BYTEA,
    timestamp BIGINT NOT NULL,
    removed BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS services (
    id VARCHAR(255) PRIMARY KEY,
    graph BYTEA,
    timestamp BIGINT NOT NULL,
    removed BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS metadata (
    id VARCHAR(255) PRIMARY KEY,
    value VARCHAR(255) NOT NULL
);
