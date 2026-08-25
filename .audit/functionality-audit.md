# Functionality Audit — KnowledgeNetwork

Trace format: UI component → Frontend handler → HTTP request → Controller endpoint → Service method → Repository/DB → Status / Notes

- **Login**
  - UI: `LoginForm` (`frontend/src/features/auth/components/LoginForm.tsx`)
  - Frontend handler: `onSubmit` calls `useAuth().login` → `auth-context.login`
  - HTTP request: `POST /api/v1/auth/login` (frontend `authApi.login` → `post('/auth/login', {...})`)
  - Controller: `AuthController.login` (`/api/v1/auth/login`) → calls `AuthService.login`
  - Service: `AuthService.login(LoginRequest)` → authenticates with `AuthenticationManager`, issues access + refresh tokens via `JwtTokenProvider` and `RefreshTokenService`
  - Repository/DB: `users` table (V1 migration), `refresh_tokens` via `RefreshTokenRepository` (migration V2)
  - Status: Working — flows implemented and used by frontend. Token storage in localStorage handled by `auth-context`.

- **Register**
  - UI: `RegisterForm` (`frontend/src/features/auth/components/RegisterForm.tsx`)
  - Frontend handler: `onSubmit` calls `useAuth().register` → `auth-context.register`
  - HTTP request: `POST /api/v1/auth/register` (frontend `authApi.register`)
  - Controller: `AuthController.register` → calls `AuthService.register`
  - Service: `AuthService.register(RegisterRequest)` → creates `User`, creates default workspace, node/edge types, issues tokens
  - Repository/DB: `users`, `workspaces`, `node_types`, `edge_types` (V1 and V2 migrations)
  - Status: Implemented backend; frontend registration handler exists. Verified mapping: OK. Note: frontend expects `LoginResponse` shape matching `AuthResponse` — controller returns `AuthResponse` — OK.

- **Create Graph (workspace)**
  - UI: `GraphManagementPage` 'New graph' button → `handleCreateGraph` (prompt, then `useCreateWorkspace().mutate`)
  - Frontend handler: `useCreateWorkspace` mutation → `workspaceApi.create` → `POST /api/v1/workspaces` with JSON body `{ name, description }`
  - Controller: `WorkspaceController.createWorkspace` → `WorkspaceService.createWorkspace`
  - Service: `WorkspaceService.createWorkspace(WorkspaceCreateRequest)` → saves workspace and membership, returns `WorkspaceResponse`
  - Repository/DB: `workspaces`, `workspace_members` tables
  - Status: Implemented end-to-end. UI uses prompt-based name/description — works.

- **View Graph (open editor)**
  - UI: `GraphEditorPage` (`frontend/src/features/graph/pages/GraphEditorPage.tsx`) expects `workspaceId` query param and renders `GraphEditor`
  - Frontend handler: `GraphEditor` uses React Query to call `nodeApi.list(workspaceId)` and `edgeApi.list(workspaceId)`
  - HTTP requests: `GET /api/v1/relationships/nodes/{workspaceId}` and `GET /api/v1/relationships/edges/{workspaceId}`
  - Controller: `RelationshipController.getNodes` and `getEdges` → `RelationshipService.getNodes` / `getEdges`
  - Service: `RelationshipService.getNodes/getEdges` → use `NodeRepository` / `EdgeRepository` to fetch paged results
  - Repository/DB: `nodes`, `edges` tables (V1 migration)
  - Status: Implemented on backend; frontend queries and maps results. Observed potential UI edge-cases: frontend expects paged response shape (`PageResponse` mapped in api-client wrappers). Status: Working (but needs real data in DB). 

- **Edit Graph (move node, rename, update attributes)**
  - UI: `GraphEditor` node drag stop triggers `updateNodeMutation` (calls `nodeApi.update`)
  - Frontend handler: `nodeApi.update` -> `PUT /api/v1/relationships/nodes/{workspaceId}/{nodeId}` with body `{ label, attributes, version }`
  - Controller: `RelationshipController.updateNode` -> `RelationshipService.updateNode(UUID workspaceId, UUID nodeId, NodeUpdateRequest)`
  - Service: validates optimistic lock via version, updates `Node`, saves via `NodeRepository`
  - Repository/DB: `nodes` table stores `attributes` JSONB; optimistic locking implemented using `version` integer field
  - Status: Implemented backend and frontend mutation. Needs real DB rows and correct `version` handling; UI creates temporary client-side nodes with `id` like `node-<timestamp>` until server returns persisted node ID — UX is handled by optimistic addition. Status: Implemented but requires correct server responses to replace temporary nodes.

- **Delete Graph (workspace)**
  - UI: GraphManagement or workspace menu may offer delete (not widely exposed in UI components scanned). `workspaceApi.delete` exists.
  - Frontend handler: `workspaceApi.delete(id)` -> `DELETE /api/v1/workspaces/{id}`
  - Controller: `WorkspaceController.deleteWorkspace` -> `WorkspaceService.deleteWorkspace` sets `is_deleted` flag
  - Repository/DB: `workspaces` table `is_deleted` soft-delete
  - Status: Backend implemented; frontend has API but UI action might not be present. If button exists, ensure it calls API — currently not found in scanned UI components.

- **Nodes: list/create/update/delete (graph entities)**
  - UI: `GraphEditor` lists nodes, `addNode()` triggers mutation to create node, inspector edits call update, deleteSelected calls delete
  - Frontend handlers: `nodeApi.list/create/update/delete`
  - Controller: `RelationshipController` endpoints for nodes
  - Service: `RelationshipService.createNode/updateNode/deleteNode`
  - Repository/DB: `node_types`, `nodes`
  - Status: Implemented. Frontend-to-backend mapping looks correct. Edge cases: client uses temporary IDs for new nodes (`node-...`) until server returns created entity; UI must reconcile server id on success — current code invalidates queries which reloads nodes list; but the created node may briefly appear twice until server data refreshes. Functional but could be improved.

- **Edges: list/create/update/delete**
  - UI: `GraphEditor` onConnect calls `createEdgeMutation` which calls edge API; deleteEdge mutation exists
  - Frontend: `edgeApi.create` posts JSON body to `/relationships/edges`
  - Controller: `RelationshipController.createEdge` -> `RelationshipService.createEdge`
  - Service: validates self-loop, cycles, saves via `EdgeRepository`
  - DB: `edges` table
  - Status: Implemented end-to-end. Frontend creates transient visual edge before server confirms; backend enforces domain rules and will reject invalid operations.

- **Likes (social)**
  - UI: Social components call `useLikePost`/`useUnlikePost` which call `socialApi.likePost/unlikePost`
  - Frontend: Before audit fixes, these sent JSON bodies; updated to send `POST /api/v1/social/posts/{postId}/like` with `workspaceId` as param
  - Controller: `SocialController.likePost` expects PathVariable `postId` and AuthenticationPrincipal; does not require workspaceId
  - Service: `SocialService.likePost(UUID postId)` resolves workspace via post and saves `SocialLike` in DB
  - Repository/DB: `social_likes`, `social_posts` tables (V5 migration)
  - Status: Backend implemented; frontend mapping fixed. Works when authenticated.

- **Favorites**
  - UI: `useFavoritePost`/`useUnfavoritePost` → `socialApi.favoritePost/unfavoritePost`
  - Frontend: adjusted to send POST to endpoints with params
  - Controller/Service/DB: `SocialController.favoritePost` / `SocialService.favoritePost` -> `social_favorites` repository/table
  - Status: Implemented backend; frontend mapping now consistent.

- **Comments**
  - UI: commenting uses `useCreateComment` -> `socialApi.addComment`
  - Frontend sends JSON body `{ content, parentCommentId }` to `POST /api/v1/social/posts/{postId}/comments`
  - Controller: `SocialController.addComment` accepts `@RequestBody SocialCommentRequest` -> `SocialService.addComment` -> `social_comment` repository
  - DB: `social_comments`
  - Status: Implemented; frontend previously included `workspaceId` in body — backend ignores unknown fields. Adjusted to send minimal body.

- **Follow**
  - UI: `useFollowUser` -> `socialApi.followUser(workspaceId, followeeId)`
  - Frontend: patched to send params { followeeId, workspaceId }
  - Controller: `SocialController.followUser` expects `@RequestParam UUID followeeId` and uses current user
  - Service: `SocialService.followUser(UUID followeeId)` performs checks and saves `SocialFollow`
  - DB: `social_follow`
  - Status: Implemented. Frontend now matches controller.

- **Search**
  - UI: `SearchPage` (lazy) uses `searchApi.search(workspaceId, query...)` -> `GET /api/v1/graphs/search?workspaceId=...&q=...`
  - Controller: `GraphSearchController.searchNodes` expects request params and returns paged `NodeResponse`
  - Service/DB: `GraphSearchService` uses NodeRepository with full-text indexes (V1 migration created `idx_nodes_label_fts`)
  - Status: Implemented. Verify UI uses proper response shape (PageResponse). Frontend expects `SearchResponse` type wrapping `content` etc — mapping looks consistent.

- **Notifications**
  - UI: notification hooks call `notificationApi.list`, `getUnreadCount`, `markAsRead`
  - Frontend: `getUnreadCount` originally expected `{ count }` — backend returns raw numeric ApiResponse body. Updated frontend to `get<number>`.
  - Controller: `NotificationController.getUnreadCount` returns `ApiResponse<Long>`
  - Service/DB: `NotificationService` and `notifications` table
  - Mark-as-read: frontend changed to use `PATCH /api/v1/notifications/{id}/read` via new `patch` helper
  - Status: Implemented after API client fixes.

- **Version History / Snapshot / Fork / Restore**
  - UI: `versioningApi` functions exist (`createSnapshot`, `listHistory`, `restoreVersion`, `createFork`)
  - Frontend: sends JSON bodies and path params to `/api/v1/graphs/...`
  - Controller: `GraphVersioningController` endpoints exist and call `GraphVersioningService`
  - Service/DB: `graph_versions`, `graph_forks`, and supporting snapshot storage (V3 migration)
  - Status: Implemented. Frontend mapping appears aligned.

- **Profile**
  - UI: `ProfilePage` lazy route exists, but I did not find a dedicated `ProfileController` in backend.
  - Frontend: `ProfilePage` may use user info from `AuthContext` and a `profile` feature; search did not locate a backend `ProfileController`.
  - Status: Partial — viewing profile likely uses local auth user; editing profile endpoints may be missing. Need to inspect `frontend/src/features/profile` to confirm actions.

Summary of mismatches discovered and already fixed in frontend:
- Social endpoints expecting `@RequestParam` were being sent JSON bodies — adjusted `frontend/src/lib/api.ts` to send parameters.
- Notification `markAsRead` used PUT while backend expects PATCH — added `patch` helper and switched to PATCH.
- Notification `getUnreadCount` expected `{ count }` — updated to parse numeric response.

Remaining open items to verify in-depth:
- Ensure created node reconciliation replaces temporary client IDs after server create (client currently invalidates queries which should refresh list; confirm no duplicate nodes remain).
- Review `ProfilePage` to confirm backend support for profile updates; implement endpoint if UI requires it.
- Run frontend TypeScript build and start backend tests to surface any remaining type or mapping mismatches.

Next steps:
1. Run `pnpm build` or `npm run build` for frontend to detect TypeScript errors.
2. Optionally run `mvn -DskipTests package` to ensure backend builds.

