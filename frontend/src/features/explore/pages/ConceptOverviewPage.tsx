import React, { useEffect, useState } from "react";
import { useSearchParams, Link, useNavigate } from "react-router-dom";
import { Search, Network, ArrowRight, ArrowLeft, ShieldCheck, Link2, RefreshCw } from "lucide-react";

import { exploreApi, PublicConceptExploreResponse } from "../api/exploreApi";
import { useKnowledgeTrail } from "../hooks/useKnowledgeTrail";

export function ConceptOverviewPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { addStep } = useKnowledgeTrail();
  const query = searchParams.get("q") || "";
  const [searchInput, setSearchInput] = useState(query);
  const [data, setData] = useState<PublicConceptExploreResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchConceptData = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await exploreApi.exploreConcepts(query, 0, 30);
      setData(result);
    } catch (err: any) {
      setError(err?.response?.data?.message || "Failed to load concept overview");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    setSearchInput(query);
    if (query) {
      addStep({ type: "concept", label: `Concept: ${query}`, url: `/explore/concepts?q=${encodeURIComponent(query)}` });
      fetchConceptData();
    } else {
      setIsLoading(false);
      setData(null);
    }
  }, [query]);


  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchInput.trim()) {
      navigate(`/explore/concepts?q=${encodeURIComponent(searchInput.trim())}`);
    }
  };

  return (
    <div className="min-h-screen bg-[#080c14] text-slate-100 p-6 md:p-10 space-y-8">
      <div className="max-w-5xl mx-auto space-y-6">
        {/* Navigation Breadcrumb */}
        <div className="flex items-center gap-2 text-xs text-slate-400">
          <Link to="/explore" className="hover:text-cyan-300 transition flex items-center gap-1">
            <ArrowLeft size={14} /> Back to Explore Hub
          </Link>
          <span>/</span>
          <span className="text-slate-200">Concept Overview</span>
        </div>

        {/* Search Header */}
        <div className="space-y-4">
          <form onSubmit={handleSearchSubmit} className="relative max-w-2xl">
            <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder="Search concepts across public networks..."
              className="w-full pl-11 pr-24 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 text-sm shadow-glass backdrop-blur-xl"
            />
            <button
              type="submit"
              className="absolute right-2 top-1/2 -translate-y-1/2 px-3 py-1.5 bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-semibold text-xs rounded-lg transition"
            >
              Search
            </button>
          </form>

          {query && (
            <div className="space-y-1 border-b border-white/10 pb-4">
              <h1 className="text-3xl font-extrabold text-white capitalize">
                Concept: <span className="text-cyan-300">"{query}"</span>
              </h1>
              <p className="text-xs text-slate-400">
                {data ? `Appears across ${data.totalPublicNetworks} public Knowledge Network${data.totalPublicNetworks === 1 ? "" : "s"}` : "Searching concept occurrences..."}
              </p>
            </div>
          )}
        </div>

        {/* Content Section */}
        {isLoading ? (
          <div className="flex min-h-[250px] items-center justify-center">
            <div className="h-8 w-8 animate-spin rounded-full border-2 border-cyan-400 border-t-transparent" />
          </div>
        ) : error ? (
          <div className="flex flex-col items-center justify-center p-8 rounded-2xl border border-red-500/20 bg-red-500/5 text-center space-y-4">
            <p className="text-red-400 text-sm">{error}</p>
            <button
              onClick={fetchConceptData}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-red-500/20 text-red-200 hover:bg-red-500/30 text-xs font-medium transition"
            >
              <RefreshCw size={14} /> Retry
            </button>
          </div>
        ) : !query ? (
          <div className="text-center py-12 text-slate-400 text-sm">
            Enter a concept name in the search box above to explore public occurrences.
          </div>
        ) : !data || data.occurrences.length === 0 ? (
          <div className="flex flex-col items-center justify-center p-12 rounded-2xl border border-white/10 bg-white/5 text-center space-y-3">
            <Network size={36} className="text-slate-600" />
            <h3 className="text-base font-semibold text-white">No Public Occurrences Found</h3>
            <p className="text-slate-400 text-xs max-w-sm">
              No public knowledge networks currently contain the concept "{query}".
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Occurrences List */}
            <div className="lg:col-span-2 space-y-4">
              <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400">
                Networks Containing This Concept
              </h2>

              <div className="space-y-4">
                {data.occurrences.map((occ) => (
                  <div
                    key={`${occ.networkId}-${occ.nodeId}`}
                    className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-xl border border-white/10 bg-white/5 hover:border-cyan-500/30 transition"
                  >
                    <div className="space-y-1.5">
                      <div className="flex items-center gap-2">
                        <span className="px-2 py-0.5 rounded bg-cyan-500/10 border border-cyan-500/20 text-cyan-300 text-[10px] font-mono">
                          {occ.nodeType || "Concept"}
                        </span>
                        <span className="inline-flex items-center gap-1 text-[11px] text-slate-400">
                          <ShieldCheck size={12} /> {occ.licenseType?.replace(/_/g, " ") || "CC BY 4.0"}
                        </span>
                      </div>

                      <Link to={`/graphs/${occ.networkId}?fromConcept=${encodeURIComponent(query)}`}>
                        <h3 className="text-base font-bold text-white hover:text-cyan-300 transition">
                          {occ.networkTitle}
                        </h3>
                      </Link>

                      <div className="text-xs text-slate-400">
                        Published by{" "}
                        <Link
                          to={`/creator/${encodeURIComponent(occ.creatorUsername)}`}
                          className="text-cyan-400 hover:underline font-medium"
                        >
                          @{occ.creatorUsername}
                        </Link>
                      </div>
                    </div>

                    <Link
                      to={`/graphs/${occ.networkId}?fromConcept=${encodeURIComponent(query)}`}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white/10 hover:bg-cyan-500 hover:text-slate-950 text-white font-medium text-xs rounded-lg transition self-start sm:self-center"
                    >
                      Open Graph <ArrowRight size={13} />
                    </Link>

                  </div>
                ))}
              </div>
            </div>

            {/* Related Concepts Panel */}
            <div className="space-y-4">
              <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400">
                Co-occurring Concepts
              </h2>

              {data.relatedConcepts.length === 0 ? (
                <div className="p-4 rounded-xl border border-white/5 bg-white/5 text-slate-500 text-xs text-center">
                  No co-occurring concept relationships found.
                </div>
              ) : (
                <div className="p-4 rounded-xl border border-white/10 bg-white/5 space-y-2">
                  {data.relatedConcepts.map((rel) => (
                    <Link
                      key={rel.label}
                      to={`/explore/concepts?q=${encodeURIComponent(rel.label)}`}
                      className="flex items-center justify-between p-2.5 rounded-lg hover:bg-white/10 text-xs transition group"
                    >
                      <span className="font-medium text-slate-200 group-hover:text-cyan-300 flex items-center gap-2">
                        <Link2 size={13} className="text-slate-500 group-hover:text-cyan-400" />
                        {rel.label}
                      </span>
                      <span className="px-2 py-0.5 rounded bg-white/5 text-slate-400 text-[10px] font-mono">
                        {rel.coOccurrenceCount} link{rel.coOccurrenceCount === 1 ? "" : "s"}
                      </span>
                    </Link>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
