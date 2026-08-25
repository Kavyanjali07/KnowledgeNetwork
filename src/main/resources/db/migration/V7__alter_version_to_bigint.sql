-- Migration: V7__alter_version_to_bigint.sql
-- Description: Alter version column type from INTEGER to BIGINT across all entities extending BaseEntity

ALTER TABLE users ALTER COLUMN version TYPE BIGINT;
ALTER TABLE workspaces ALTER COLUMN version TYPE BIGINT;
ALTER TABLE node_types ALTER COLUMN version TYPE BIGINT;
ALTER TABLE nodes ALTER COLUMN version TYPE BIGINT;
ALTER TABLE edge_types ALTER COLUMN version TYPE BIGINT;
ALTER TABLE edges ALTER COLUMN version TYPE BIGINT;
ALTER TABLE social_posts ALTER COLUMN version TYPE BIGINT;
ALTER TABLE social_comments ALTER COLUMN version TYPE BIGINT;
