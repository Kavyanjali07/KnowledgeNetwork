-- Migration: V12__add_edge_unique_constraint_and_indexes.sql
-- Description: Enforce duplicate relationship prevention and optimize edge traversal queries

-- Partial unique index to prevent duplicate active directional relationships within a workspace/graph
CREATE UNIQUE INDEX IF NOT EXISTS uk_active_edge_relationship 
ON edges (workspace_id, source_node_id, target_node_id, edge_type_id) 
WHERE is_deleted = FALSE;

-- Ensure b-tree index on edge_type_id for fast relationship filtering
CREATE INDEX IF NOT EXISTS idx_edges_edge_type ON edges(edge_type_id) WHERE is_deleted = FALSE;
