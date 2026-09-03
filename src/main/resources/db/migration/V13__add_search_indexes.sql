-- Migration: V13__add_search_indexes.sql
-- Description: Add case-insensitive indexes for unified search across graphs and nodes

-- Workspace search indexes (graph title/description search)
CREATE INDEX IF NOT EXISTS idx_workspaces_name_lower ON workspaces(LOWER(name));
CREATE INDEX IF NOT EXISTS idx_workspaces_description_lower ON workspaces(LOWER(description));

-- Node label index already exists from V4 (idx_nodes_label_lower)
-- Adding workspace_id composite index for faster node search joins
CREATE INDEX IF NOT EXISTS idx_nodes_workspace_label ON nodes(workspace_id, LOWER(label)) WHERE is_deleted = false;
