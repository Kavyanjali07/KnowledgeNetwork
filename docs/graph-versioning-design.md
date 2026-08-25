# Git-like Graph Versioning Design

## Goal
Provide a scalable, workspace-scoped versioning model for knowledge graphs with:
- snapshots of graph state
- version history and ancestry
- restore-to-previous-version workflows
- forked graph branches
- explicit fork ownership and role-based access

## Core Design Principles
1. Keep the live graph tables as the primary write path.
2. Store snapshots as immutable JSONB payloads to avoid expensive row-level copies.
3. Model history as a linked list of versions, where each version points to a parent version.
4. Use fork records to represent branch lineage without duplicating the full graph state.
5. Keep restoration and branching cheap by reusing snapshots rather than rewriting the entire graph on every operation.

## Storage Model

### 1. graph_snapshots
Stores immutable snapshots of graph state.

| Column | Type | Notes |
|---|---|---|
| id | UUID PK | immutable snapshot id |
| workspace_id | UUID FK | scoped to workspace |
| version_id | UUID FK | optional reference to the owning version |
| name | VARCHAR(150) | human-friendly snapshot label |
| description | TEXT | optional description |
| snapshot_data | JSONB | serialized node/edge state |
| node_count | INTEGER | denormalized for fast listing |
| edge_count | INTEGER | denormalized for fast listing |
| created_at | TIMESTAMP | immutable |
| created_by | VARCHAR(100) | actor |

### 2. graph_versions
Represents a version in the history of a graph.

| Column | Type | Notes |
|---|---|---|
| id | UUID PK | version id |
| workspace_id | UUID FK | workspace |
| snapshot_id | UUID FK | points to snapshot payload |
| parent_version_id | UUID FK | previous version in the chain |
| version_number | BIGINT | monotonic version number |
| label | VARCHAR(150) | e.g. "snapshot-2026-07-27" |
| description | TEXT | change note |
| change_type | VARCHAR(30) | SNAPSHOT, RESTORE, FORK |
| created_at | TIMESTAMP | |
| created_by | VARCHAR(100) | |

### 3. graph_forks
Represents a derived branch of a graph.

| Column | Type | Notes |
|---|---|---|
| id | UUID PK | fork id |
| workspace_id | UUID FK | workspace |
| source_version_id | UUID FK | base version the fork starts from |
| name | VARCHAR(150) | fork branch name |
| description | TEXT | |
| created_at | TIMESTAMP | |
| created_by | VARCHAR(100) | |

### 4. graph_fork_owners
Maps users to ownership/permissions on a fork.

| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| fork_id | UUID FK | |
| user_id | UUID FK | |
| role | VARCHAR(30) | OWNER, EDITOR, VIEWER |
| granted_at | TIMESTAMP | |

## Indexing Strategy
- B-tree on workspace_id + created_at for history queries.
- B-tree on parent_version_id for ancestry traversal.
- GIN index on snapshot_data to support metadata filtering in future.
- Unique constraint on (workspace_id, version_number).
- Unique constraint on (workspace_id, name) for forks.

## Versioning Workflow
### Snapshots
- Capture a full graph snapshot of all non-deleted nodes and edges.
- Store it as immutable JSONB data.
- Create a new graph version that references the snapshot.

### Version History
- Read versions ordered by version_number descending.
- Each version points to its parent version, enabling a history tree.

### Restore Version
- Load the target snapshot.
- Rebuild the live graph from that snapshot.
- Create a new version recorded as a RESTORE operation.

### Fork Graph
- Create a fork row that references the chosen source version.
- Optionally create an initial snapshot that inherits from that version.
- The fork can later diverge independently.

### Fork Ownership
- Associate a user with a fork via graph_fork_owners.
- Support OWNER, EDITOR, and VIEWER roles.

## Scalability Notes
- The snapshot payload is intentionally compact and immutable, keeping storage overhead predictable.
- JSONB keeps the schema flexible while supporting later feature expansion such as metadata filters and diffing.
- The live graph tables remain the source of truth for active editing; snapshots are written asynchronously or at explicit checkpoints.
