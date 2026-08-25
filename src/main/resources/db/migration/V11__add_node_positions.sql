-- Migration: V11__add_node_positions.sql
-- Description: Add position_x and position_y columns to nodes table for 2D spatial layout persistence

ALTER TABLE nodes ADD COLUMN IF NOT EXISTS position_x DOUBLE PRECISION NOT NULL DEFAULT 0.0;
ALTER TABLE nodes ADD COLUMN IF NOT EXISTS position_y DOUBLE PRECISION NOT NULL DEFAULT 0.0;

CREATE INDEX IF NOT EXISTS idx_nodes_workspace_deleted ON nodes(workspace_id, is_deleted);
