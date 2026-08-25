-- Migration: V3__add_graph_versioning.sql
-- Description: Add graph snapshot, version history, fork, and fork-ownership tables

CREATE TABLE graph_snapshots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    snapshot_data JSONB NOT NULL DEFAULT '{}'::jsonb,
    node_count INTEGER NOT NULL DEFAULT 0,
    edge_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system'
);

CREATE TABLE graph_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    snapshot_id UUID NOT NULL REFERENCES graph_snapshots(id) ON DELETE RESTRICT,
    parent_version_id UUID REFERENCES graph_versions(id) ON DELETE SET NULL,
    version_number BIGINT NOT NULL,
    label VARCHAR(150) NOT NULL,
    description TEXT,
    change_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_workspace_version_number UNIQUE (workspace_id, version_number)
);

CREATE TABLE graph_forks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    source_version_id UUID NOT NULL REFERENCES graph_versions(id) ON DELETE RESTRICT,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_workspace_fork_name UNIQUE (workspace_id, name)
);

CREATE TABLE graph_fork_owners (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    fork_id UUID NOT NULL REFERENCES graph_forks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_graph_snapshots_workspace_created ON graph_snapshots(workspace_id, created_at DESC);
CREATE INDEX idx_graph_versions_workspace_number ON graph_versions(workspace_id, version_number DESC);
CREATE INDEX idx_graph_versions_parent ON graph_versions(parent_version_id);
CREATE INDEX idx_graph_forks_workspace ON graph_forks(workspace_id);
CREATE INDEX idx_graph_fork_owners_fork ON graph_fork_owners(fork_id);
CREATE INDEX idx_graph_snapshots_data_gin ON graph_snapshots USING gin (snapshot_data);
