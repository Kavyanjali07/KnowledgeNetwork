-- Migration: V16__add_publishing_licensing_provenance.sql
-- Description: Add publishing, licensing, attribution metadata to workspaces and graph_forks, and create network_references table

-- 1. Add publishing & licensing columns to workspaces
ALTER TABLE workspaces ADD COLUMN IF NOT EXISTS license_type VARCHAR(50) NOT NULL DEFAULT 'ALL_RIGHTS_RESERVED';
ALTER TABLE workspaces ADD COLUMN IF NOT EXISTS is_published BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE workspaces ADD COLUMN IF NOT EXISTS published_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE workspaces ADD COLUMN IF NOT EXISTS custom_attribution TEXT;

CREATE INDEX IF NOT EXISTS idx_workspaces_published_visibility ON workspaces(is_published, visibility) WHERE is_deleted = FALSE;

-- 2. Add provenance columns to graph_forks
ALTER TABLE graph_forks ADD COLUMN IF NOT EXISTS source_workspace_id UUID REFERENCES workspaces(id) ON DELETE SET NULL;
ALTER TABLE graph_forks ADD COLUMN IF NOT EXISTS source_license VARCHAR(50);
ALTER TABLE graph_forks ADD COLUMN IF NOT EXISTS original_creator_id UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE graph_forks ADD COLUMN IF NOT EXISTS is_derivative BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX IF NOT EXISTS idx_graph_forks_source_workspace ON graph_forks(source_workspace_id);

-- 3. Create network_references table
CREATE TABLE IF NOT EXISTS network_references (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    source_node_id UUID REFERENCES nodes(id) ON DELETE SET NULL,
    target_workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    target_node_id UUID REFERENCES nodes(id) ON DELETE SET NULL,
    reference_type VARCHAR(50) NOT NULL DEFAULT 'CITES',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_network_reference UNIQUE (source_workspace_id, source_node_id, target_workspace_id, target_node_id)
);

CREATE INDEX IF NOT EXISTS idx_network_refs_target_workspace ON network_references(target_workspace_id);
CREATE INDEX IF NOT EXISTS idx_network_refs_source_workspace ON network_references(source_workspace_id);
