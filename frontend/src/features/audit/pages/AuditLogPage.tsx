import { motion } from "framer-motion";
import {
  Activity,
  AlertCircle,
  Calendar,
  ChevronLeft,
  ChevronRight,
  Database,
  FileCode,
  FolderGit2,
  RefreshCw,
  Search,
  ShieldCheck,
  User
} from "lucide-react";
import { useEffect, useState } from "react";
import { Button } from "../../../components/ui/button";
import { Input } from "../../../components/ui/input";
import { auditLogApi, type AuditLogResponse } from "../../../services/auditLogApi";
import { workspaceApi, type WorkspaceResponse } from "../../../services/graphApi";

const ACTION_CATEGORIES = [
  { label: "All Events", value: "ALL" },
  { label: "Authentication", value: "AUTH" },
  { label: "Workspace", value: "WORKSPACE" },
  { label: "Graph & Model", value: "GRAPH" },
  { label: "Nodes & Edges", value: "NODE" }
];

export function AuditLogPage() {
  const [workspaces, setWorkspaces] = useState<WorkspaceResponse[]>([]);
  const [selectedWorkspaceId, setSelectedWorkspaceId] = useState<string>("");
  const [logs, setLogs] = useState<AuditLogResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [actionFilter, setActionFilter] = useState("");
  const [entityTypeFilter, setEntityTypeFilter] = useState("");
  const [startDateFilter, setStartDateFilter] = useState("");
  const [endDateFilter, setEndDateFilter] = useState("");

  // Pagination
  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Metadata drawer / preview
  const [expandedLogId, setExpandedLogId] = useState<string | null>(null);

  useEffect(() => {
    fetchWorkspaces();
  }, []);

  const fetchWorkspaces = async () => {
    try {
      const res = await workspaceApi.list();
      const list = res.data || [];
      setWorkspaces(list);
      if (list.length > 0 && list[0]?.id && !selectedWorkspaceId) {
        setSelectedWorkspaceId(list[0].id);
      }
    } catch {
      // Allow general audit log querying if workspace fetch fails
    }
  };

  useEffect(() => {
    fetchAuditLogs();
  }, [selectedWorkspaceId, page, actionFilter, entityTypeFilter]);

  const fetchAuditLogs = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        workspaceId: selectedWorkspaceId || undefined,
        actorId: undefined,
        action: actionFilter || undefined,
        entityType: entityTypeFilter || undefined,
        startDate: startDateFilter ? new Date(startDateFilter).toISOString() : undefined,
        endDate: endDateFilter ? new Date(endDateFilter).toISOString() : undefined,
        page,
        size: pageSize
      };
      const response = await auditLogApi.getLogs(params);
      setLogs(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
    } catch (err: any) {
      setError(err?.message || "Failed to load audit events.");
    } finally {
      setLoading(false);
    }
  };

  const handleApplyFilters = () => {
    setPage(0);
    fetchAuditLogs();
  };

  const handleResetFilters = () => {
    setActionFilter("");
    setEntityTypeFilter("");
    setStartDateFilter("");
    setEndDateFilter("");
    setPage(0);
    fetchAuditLogs();
  };

  const getActionBadgeColor = (action: string) => {
    if (action.includes("LOGIN") || action.includes("VERIFIED")) return "bg-emerald-500/10 text-emerald-400 border-emerald-500/30";
    if (action.includes("DELETED") || action.includes("RESTORED") || action.includes("FORKED")) return "bg-rose-500/10 text-rose-400 border-rose-500/30";
    if (action.includes("UPDATED") || action.includes("MEMBER")) return "bg-amber-500/10 text-amber-400 border-amber-500/30";
    return "bg-cyan-500/10 text-cyan-400 border-cyan-500/30";
  };

  const getEntityIcon = (type: string) => {
    switch (type) {
      case "AUTH": return <User className="text-purple-400" size={14} />;
      case "WORKSPACE": return <FolderGit2 className="text-cyan-400" size={14} />;
      case "GRAPH": return <Activity className="text-emerald-400" size={14} />;
      case "NODE": return <Database className="text-blue-400" size={14} />;
      case "EDGE": return <Activity className="text-indigo-400" size={14} />;
      default: return <FileCode className="text-slate-400" size={14} />;
    }
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl border border-cyan-500/30 bg-cyan-500/10 text-cyan-400">
              <ShieldCheck size={22} />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-slate-100">Production Audit Log</h1>
              <p className="text-xs text-muted-foreground">Immutable compliance trail for system security, governance, and graph operations.</p>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="secondary"
            onClick={fetchAuditLogs}
            disabled={loading}
            className="h-9 border-white/10 bg-white/[0.03] text-xs text-slate-300 hover:bg-white/[0.08]"
          >
            <RefreshCw size={14} className={loading ? "animate-spin" : ""} />
            Refresh
          </Button>
        </div>
      </div>

      {/* Filter Bar */}
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        className="mt-6 rounded-2xl border border-white/10 bg-white/[0.03] p-4 backdrop-blur-xl"
      >
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {/* Workspace Filter */}
          <div>
            <label className="mb-1 block text-[11px] font-medium uppercase tracking-wider text-slate-400">Workspace Scope</label>
            <select
              value={selectedWorkspaceId}
              onChange={(e) => {
                setSelectedWorkspaceId(e.target.value);
                setPage(0);
              }}
              className="w-full rounded-lg border border-white/10 bg-black/40 px-3 py-2 text-xs text-slate-200 focus:border-cyan-500 focus:outline-none"
            >
              <option value="">All Accessible Workspaces</option>
              {workspaces.map((ws) => (
                <option key={ws.id} value={ws.id}>
                  {ws.name || ws.title}
                </option>
              ))}
            </select>
          </div>

          {/* Action Filter */}
          <div>
            <label className="mb-1 block text-[11px] font-medium uppercase tracking-wider text-slate-400">Event Action</label>
            <Input
              placeholder="e.g. LOGIN_SUCCESS, GRAPH_CREATED"
              value={actionFilter}
              onChange={(e) => setActionFilter(e.target.value)}
              className="h-9 border-white/10 bg-black/40 text-xs text-slate-200 placeholder:text-slate-500"
            />
          </div>

          {/* Entity Type Filter */}
          <div>
            <label className="mb-1 block text-[11px] font-medium uppercase tracking-wider text-slate-400">Entity Category</label>
            <select
              value={entityTypeFilter}
              onChange={(e) => setEntityTypeFilter(e.target.value)}
              className="w-full rounded-lg border border-white/10 bg-black/40 px-3 py-2 text-xs text-slate-200 focus:border-cyan-500 focus:outline-none"
            >
              <option value="">All Entity Types</option>
              <option value="AUTH">Authentication (AUTH)</option>
              <option value="WORKSPACE">Workspace</option>
              <option value="GRAPH">Graph / Workspace</option>
              <option value="NODE">Node</option>
              <option value="EDGE">Edge / Relationship</option>
              <option value="SYSTEM">System</option>
            </select>
          </div>

          {/* Date Range Start */}
          <div>
            <label className="mb-1 block text-[11px] font-medium uppercase tracking-wider text-slate-400">Date Range</label>
            <div className="flex items-center gap-2">
              <Input
                type="date"
                value={startDateFilter}
                onChange={(e) => setStartDateFilter(e.target.value)}
                className="h-9 border-white/10 bg-black/40 text-xs text-slate-200"
              />
              <span className="text-xs text-slate-500">to</span>
              <Input
                type="date"
                value={endDateFilter}
                onChange={(e) => setEndDateFilter(e.target.value)}
                className="h-9 border-white/10 bg-black/40 text-xs text-slate-200"
              />
            </div>
          </div>
        </div>

        {/* Filter Action Buttons */}
        <div className="mt-4 flex items-center justify-between border-t border-white/5 pt-3">
          <div className="flex items-center gap-2">
            {ACTION_CATEGORIES.map((cat) => (
              <button
                key={cat.value}
                onClick={() => {
                  setEntityTypeFilter(cat.value === "ALL" ? "" : cat.value);
                  setPage(0);
                }}
                className={`rounded-full px-3 py-1 text-[11px] font-medium transition-all ${
                  (cat.value === "ALL" && !entityTypeFilter) || entityTypeFilter === cat.value
                    ? "border border-cyan-500/40 bg-cyan-500/20 text-cyan-300"
                    : "border border-white/5 bg-white/[0.02] text-slate-400 hover:bg-white/[0.06] hover:text-slate-200"
                }`}
              >
                {cat.label}
              </button>
            ))}
          </div>

          <div className="flex items-center gap-2">
            <Button
              variant="ghost"
              onClick={handleResetFilters}
              className="h-9 text-xs text-slate-400 hover:text-slate-200"
            >
              Reset
            </Button>
            <Button
              variant="primary"
              onClick={handleApplyFilters}
              className="h-9 border border-cyan-500/40 bg-cyan-500/20 text-xs text-cyan-300 hover:bg-cyan-500/30"
            >
              <Search size={14} />
              Filter Trail
            </Button>
          </div>
        </div>
      </motion.div>

      {/* Error state */}
      {error && (
        <div className="mt-4 flex items-center gap-2 rounded-xl border border-rose-500/30 bg-rose-500/10 p-4 text-xs text-rose-300">
          <AlertCircle size={16} />
          <span>{error}</span>
        </div>
      )}

      {/* Main Audit Log Table */}
      <div className="mt-6 overflow-hidden rounded-2xl border border-white/10 bg-black/30 backdrop-blur-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="border-b border-white/10 bg-white/[0.02] text-[11px] font-semibold uppercase tracking-wider text-slate-400">
              <tr>
                <th className="px-4 py-3">Timestamp</th>
                <th className="px-4 py-3">Actor / User</th>
                <th className="px-4 py-3">Event Action</th>
                <th className="px-4 py-3">Entity Type</th>
                <th className="px-4 py-3">Entity ID</th>
                <th className="px-4 py-3 text-right">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5 text-slate-300">
              {loading && logs.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-12 text-center text-slate-500">
                    <RefreshCw size={20} className="mx-auto mb-2 animate-spin text-cyan-400" />
                    Loading audit trail entries...
                  </td>
                </tr>
              ) : logs.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-12 text-center text-slate-500">
                    <ShieldCheck size={28} className="mx-auto mb-2 text-slate-600" />
                    No audit log entries found matching your query criteria.
                  </td>
                </tr>
              ) : (
                logs.map((log) => {
                  const isExpanded = expandedLogId === log.id;
                  return (
                    <tr key={log.id} className="group hover:bg-white/[0.02] transition-colors">
                      <td className="whitespace-nowrap px-4 py-3 text-slate-400 font-mono text-[11px]">
                        <div className="flex items-center gap-1.5">
                          <Calendar size={12} className="text-slate-500" />
                          {new Date(log.timestamp).toLocaleString()}
                        </div>
                      </td>

                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          <div className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full border border-white/10 bg-white/5 text-[10px] font-semibold text-slate-300">
                            {log.actorName ? log.actorName.charAt(0).toUpperCase() : "A"}
                          </div>
                          <div className="truncate">
                            <p className="font-medium text-slate-200 truncate max-w-[160px]">{log.actorName || "Anonymous"}</p>
                            <p className="text-[10px] text-slate-500 truncate max-w-[160px]">{log.actorEmail}</p>
                          </div>
                        </div>
                      </td>

                      <td className="whitespace-nowrap px-4 py-3">
                        <span className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-[10px] font-medium ${getActionBadgeColor(log.action)}`}>
                          {log.action}
                        </span>
                      </td>

                      <td className="whitespace-nowrap px-4 py-3">
                        <div className="flex items-center gap-1.5 font-medium text-slate-300">
                          {getEntityIcon(log.entityType)}
                          <span>{log.entityType}</span>
                        </div>
                      </td>

                      <td className="whitespace-nowrap px-4 py-3 font-mono text-[10px] text-slate-500">
                        {log.entityId ? log.entityId.substring(0, 8) + "..." : "N/A"}
                      </td>

                      <td className="whitespace-nowrap px-4 py-3 text-right">
                        <Button
                          variant="ghost"
                          onClick={() => setExpandedLogId(isExpanded ? null : log.id)}
                          className="h-7 px-2 text-[11px] text-cyan-400 hover:bg-cyan-500/10 hover:text-cyan-300"
                        >
                          {isExpanded ? "Hide Metadata" : "View Details"}
                        </Button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Bar */}
        <div className="flex items-center justify-between border-t border-white/10 bg-white/[0.01] px-4 py-3 text-xs text-slate-400">
          <div>
            Showing <span className="font-semibold text-slate-200">{logs.length}</span> of <span className="font-semibold text-slate-200">{totalElements}</span> audit records
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="secondary"
              disabled={page === 0 || loading}
              onClick={() => setPage(page - 1)}
              className="h-8 border-white/10 bg-white/[0.03] text-xs text-slate-300 hover:bg-white/[0.08]"
            >
              <ChevronLeft size={14} />
              Previous
            </Button>
            <span className="text-xs text-slate-400">
              Page {page + 1} of {totalPages || 1}
            </span>
            <Button
              variant="secondary"
              disabled={page >= totalPages - 1 || loading}
              onClick={() => setPage(page + 1)}
              className="h-8 border-white/10 bg-white/[0.03] text-xs text-slate-300 hover:bg-white/[0.08]"
            >
              Next
              <ChevronRight size={14} />
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
