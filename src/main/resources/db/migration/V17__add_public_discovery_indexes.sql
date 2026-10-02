-- Migration: V17__add_public_discovery_indexes.sql
-- Description: Add lower case expression index on node labels for fast public concept aggregation

CREATE INDEX IF NOT EXISTS idx_nodes_label_lower ON nodes (LOWER(label)) WHERE is_deleted = FALSE;
