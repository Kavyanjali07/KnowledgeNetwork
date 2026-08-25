-- Migration: V1__init_schema.sql
-- Description: KnowledgeNetwork Phase 1 Core Database Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. USERS TABLE
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 1,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 2. WORKSPACES TABLE
CREATE TABLE workspaces (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 1,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 3. WORKSPACE_MEMBERS TABLE
CREATE TABLE workspace_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER')),
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_workspace_user UNIQUE (workspace_id, user_id)
);

-- 4. NODE_TYPES TABLE
CREATE TABLE node_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    color_code VARCHAR(10) DEFAULT '#3B82F6',
    icon VARCHAR(50) DEFAULT 'default-node',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 1,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_workspace_nodetype UNIQUE (workspace_id, name)
);

-- 5. NODES TABLE (GRAPH ENTITIES)
CREATE TABLE nodes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    node_type_id UUID NOT NULL REFERENCES node_types(id) ON DELETE RESTRICT,
    label VARCHAR(255) NOT NULL,
    attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 1,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 6. EDGE_TYPES TABLE
CREATE TABLE edge_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    is_directed BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 1,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_workspace_edgetype UNIQUE (workspace_id, name)
);

-- 7. EDGES TABLE (GRAPH RELATIONSHIPS)
CREATE TABLE edges (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    edge_type_id UUID NOT NULL REFERENCES edge_types(id) ON DELETE RESTRICT,
    source_node_id UUID NOT NULL REFERENCES nodes(id) ON DELETE CASCADE,
    target_node_id UUID NOT NULL REFERENCES nodes(id) ON DELETE CASCADE,
    weight DOUBLE PRECISION DEFAULT 1.0,
    attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 1,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_self_loop CHECK (source_node_id <> target_node_id)
);

-- 8. AUDIT_LOGS TABLE
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    snapshot_delta JSONB,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- INDEXES FOR MULTI-TENANCY & GRAPH TRAVERSAL
CREATE INDEX idx_nodes_workspace ON nodes(workspace_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_edges_workspace ON edges(workspace_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_edges_source ON edges(source_node_id, workspace_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_edges_target ON edges(target_node_id, workspace_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_edges_composite_traversal ON edges(source_node_id, target_node_id, workspace_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_nodes_attributes_gin ON nodes USING gin (attributes);
CREATE INDEX idx_edges_attributes_gin ON edges USING gin (attributes);
CREATE INDEX idx_nodes_label_fts ON nodes USING gin (to_tsvector('english', label));
