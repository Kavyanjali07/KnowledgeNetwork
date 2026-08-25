-- Migration: V4__add_node_search_fields.sql
-- Description: Add visibility and tags support for advanced graph search

ALTER TABLE nodes ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE';

CREATE TABLE node_tags (
    node_id UUID NOT NULL REFERENCES nodes(id) ON DELETE CASCADE,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (node_id, tag)
);

CREATE INDEX idx_nodes_visibility ON nodes(visibility);
CREATE INDEX idx_nodes_label_lower ON nodes(lower(label));
