-- Migration: V10__add_workspace_visibility.sql
-- Description: Add visibility column and indexes to workspaces table for Knowledge Graph CRUD

ALTER TABLE workspaces ADD COLUMN IF NOT EXISTS visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE';

CREATE INDEX IF NOT EXISTS idx_workspaces_visibility ON workspaces(visibility) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_workspaces_owner ON workspaces(owner_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_workspaces_created_at ON workspaces(created_at DESC) WHERE is_deleted = FALSE;
