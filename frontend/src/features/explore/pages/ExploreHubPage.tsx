import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Search, Compass, Network, Share2, GitFork, User, ShieldCheck, ArrowRight, RefreshCw } from "lucide-react";
import { exploreApi, PublicNetworkItem } from "../api/exploreApi";
import { useKnowledgeTrail } from "../hooks/useKnowledgeTrail";

export function ExploreHubPage() {
  const navigate = useNavigate();
  const { addStep } = useKnowledgeTrail();
  const [query, setQuery] = useState("");
  const [licenseFilter, setLicenseFilter] = useState<string>("ALL");
  const [allowDerivatives, setAllowDerivatives] = useState<boolean>(false);
  const [networks, setNetworks] = useState<PublicNetworkItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    addStep({ type: "hub", label: "Explore Hub", url: "/explore" });
  }, []);


  const fetchNetworks = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await exploreApi.discoverPublicNetworks({
        concept: query || undefined,
        license: licenseFilter !== "ALL" ? licenseFilter : undefined,
        allowDerivatives: allowDerivatives || undefined,
        page: 0,
        size: 24
      });
      setNetworks(data.content);
    } catch (err: any) {
      setError(err?.response?.data?.message || "Failed to load public knowledge networks");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchNetworks();
  }, [licenseFilter, allowDerivatives]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (query.trim()) {
      navigate(`/explore/concepts?q=${encodeURIComponent(query.trim())}`);
    } else {
      fetchNetworks();
    }
  };

  return (
    <div className="min-h-screen bg-[#080c14] text-slate-100 p-6 md:p-10 space-y-8">
      {/* Header Banner */}
      <div className="max-w-6xl mx-auto space-y-6">
        <div className="space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 border border-cyan-500/20 text-cyan-300 text-xs font-semibold tracking-wider uppercase">
            <Compass size={14} /> Public Knowledge Ecosystem
          </div>
          <h1 className="text-3xl md:text-5xl font-extrabold tracking-tight text-white">
            Discover, Explore & Connect Knowledge
          </h1>
          <p className="text-slate-400 max-w-2xl text-sm md:text-base leading-relaxed">
            Traverse interconnected public knowledge networks, explore concept relationships, track intellectual lineage, and build upon open knowledge.
          </p>
        </div>

        {/* Search Bar */}
        <form onSubmit={handleSearchSubmit} className="relative max-w-3xl">
          <Search size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search public concepts or network topics (e.g., Recursion, Neural Networks)..."
            className="w-full pl-12 pr-28 py-4 bg-white/5 border border-white/10 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 transition text-sm md:text-base shadow-glass backdrop-blur-xl"
          />
          <button
            type="submit"
            className="absolute right-2 top-1/2 -translate-y-1/2 px-4 py-2 bg-gradient-to-r from-cyan-500 to-violet-600 hover:from-cyan-400 hover:to-violet-500 text-white font-medium text-xs md:text-sm rounded-lg shadow-glow transition flex items-center gap-1.5"
          >
            Explore <ArrowRight size={14} />
          </button>
        </form>

        {/* Filters */}
        <div className="flex flex-wrap items-center justify-between gap-4 pt-2 border-b border-white/10 pb-4">
          <div className="flex items-center gap-2 overflow-x-auto text-xs font-medium">
            <span className="text-slate-400 mr-1">License:</span>
            {["ALL", "CC_BY_4_0", "CC_BY_SA_4_0", "CC_BY_NC_4_0"].map((lic) => (
              <button
                key={lic}
                onClick={() => setLicenseFilter(lic)}
                className={`px-3 py-1.5 rounded-lg border transition ${
                  licenseFilter === lic
                    ? "bg-cyan-500/20 border-cyan-500/40 text-cyan-200"
                    : "bg-white/5 border-white/10 text-slate-400 hover:text-slate-200"
                }`}
              >
                {lic === "ALL" ? "All Licenses" : lic.replace(/_/g, " ")}
              </button>
            ))}
          </div>

          <label className="flex items-center gap-2 text-xs text-slate-300 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={allowDerivatives}
              onChange={(e) => setAllowDerivatives(e.target.checked)}
              className="rounded border-white/20 bg-white/5 text-cyan-500 focus:ring-cyan-500/50"
            />
            Derivable / Forkable Only
          </label>
        </div>
      </div>

      {/* Main Grid Content */}
      <div className="max-w-6xl mx-auto">
        {isLoading ? (
          <div className="flex min-h-[300px] items-center justify-center">
            <div className="h-8 w-8 animate-spin rounded-full border-2 border-cyan-400 border-t-transparent" />
          </div>
        ) : error ? (
          <div className="flex flex-col items-center justify-center p-8 rounded-2xl border border-red-500/20 bg-red-500/5 text-center space-y-4">
            <p className="text-red-400 text-sm">{error}</p>
            <button
              onClick={fetchNetworks}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-red-500/20 text-red-200 hover:bg-red-500/30 text-xs font-medium transition"
            >
              <RefreshCw size={14} /> Retry
            </button>
          </div>
        ) : networks.length === 0 ? (
          <div className="flex flex-col items-center justify-center p-12 rounded-2xl border border-white/10 bg-white/5 text-center space-y-3">
            <Network size={40} className="text-slate-600" />
            <h3 className="text-lg font-semibold text-white">No Public Knowledge Networks Found</h3>
            <p className="text-slate-400 text-xs max-w-sm">
              No published public networks match your selected search criteria.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {networks.map((network) => {
              const creatorUsername = network.ownerEmail && network.ownerEmail.includes("@")
                ? network.ownerEmail.substring(0, network.ownerEmail.indexOf("@"))
                : "creator";
              return (
                <div
                  key={network.id}
                  className="group flex flex-col justify-between rounded-2xl border border-white/10 bg-gradient-to-b from-white/5 to-transparent p-6 hover:border-cyan-500/40 hover:shadow-glow transition duration-300"
                >
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-violet-500/10 border border-violet-500/20 text-violet-300 text-[11px] font-medium">
                        <ShieldCheck size={12} /> {network.licenseType?.replace(/_/g, " ") || "CC BY 4.0"}
                      </span>
                      <span className="text-[11px] text-slate-500">
                        {network.publishedAt ? new Date(network.publishedAt).toLocaleDateString() : "Published"}
                      </span>
                    </div>

                    <Link to={`/graphs/${network.id}`}>
                      <h3 className="text-lg font-bold text-white group-hover:text-cyan-300 transition line-clamp-1">
                        {network.title || network.name}
                      </h3>
                    </Link>

                    <p className="text-slate-400 text-xs line-clamp-2 leading-relaxed">
                      {network.description || "No description provided for this knowledge network."}
                    </p>

                    <div className="pt-2">
                      <Link
                        to={`/creator/${encodeURIComponent(creatorUsername)}`}
                        className="inline-flex items-center gap-1.5 text-xs text-slate-300 hover:text-cyan-300 transition font-medium"
                      >
                        <User size={13} className="text-slate-500" />
                        <span>{network.ownerName || creatorUsername}</span>
                      </Link>
                    </div>
                  </div>

                  <div className="mt-6 pt-4 border-t border-white/10 flex items-center justify-between text-xs text-slate-400">
                    <div className="flex items-center gap-4">
                      <span className="flex items-center gap-1" title="Concepts (Nodes)">
                        <Network size={13} className="text-cyan-400" /> {network.nodeCount || 0}
                      </span>
                      <span className="flex items-center gap-1" title="Relationships (Edges)">
                        <Share2 size={13} className="text-violet-400" /> {network.edgeCount || 0}
                      </span>
                      <span className="flex items-center gap-1" title="Derivatives (Forks)">
                        <GitFork size={13} className="text-amber-400" /> {network.derivativeCount || 0}
                      </span>
                    </div>

                    <Link
                      to={`/graphs/${network.id}`}
                      className="text-cyan-400 hover:text-cyan-300 font-semibold inline-flex items-center gap-1 transition"
                    >
                      View <ArrowRight size={13} />
                    </Link>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
