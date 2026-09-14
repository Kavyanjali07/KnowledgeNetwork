-- Migration: V15__update_audit_logs_table.sql
-- Description: Update audit_logs schema to support nullable workspace_id and entity_id for system/auth events, and add performance indexes

-- 1. Relax NOT NULL constraints for workspace_id and entity_id
ALTER TABLE audit_logs ALTER COLUMN workspace_id DROP NOT NULL;
ALTER TABLE audit_logs ALTER COLUMN entity_id DROP NOT NULL;

-- 2. Add performance indexes for common query dimensions
CREATE INDEX idx_audit_logs_workspace_timestamp ON audit_logs(workspace_id, timestamp DESC);
CREATE INDEX idx_audit_logs_user_timestamp ON audit_logs(user_id, timestamp DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
