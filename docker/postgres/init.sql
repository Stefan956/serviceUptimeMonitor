-- PostgreSQL initialisation script
-- Runs once on first container start (when the data volume is empty).
-- The primary database (uptime_monitor) and the user (uptime_user) are
-- already created by the POSTGRES_DB / POSTGRES_USER env vars.
-- This script creates the second database used by alert-service.

CREATE DATABASE alert_db;
GRANT ALL PRIVILEGES ON DATABASE alert_db TO uptime_user;
