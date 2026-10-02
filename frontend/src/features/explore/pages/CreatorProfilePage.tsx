import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { ShieldCheck, ArrowRight, ArrowLeft, RefreshCw, Tag } from "lucide-react";

import { exploreApi, PublicCreatorProfile } from "../api/exploreApi";
import { useKnowledgeTrail } from "../hooks/useKnowledgeTrail";

export function CreatorProfilePage() {
  const { username } = useParams<{ username: string }>();
  const { addStep } = useKnowledgeTrail();
  const [profile, setProfile] = useState<PublicCreatorProfile | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProfile = async () => {
    if (!username) return;
    setIsLoading(true);
    setError(null);
    try {
      const data = await exploreApi.getPublicCreatorProfile(username);
      setProfile(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || "Creator profile not found");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (username) {
      addStep({ type: "creator", label: `@${username}`, url: `/creator/${encodeURIComponent(username)}` });
      fetchProfile();
    }
  }, [username]);


  return (
    <div className="min-h-screen bg-[#080c14] text-slate-100 p-6 md:p-10 space-y-8">
      <div className="max-w-5xl mx-auto space-y-6">
        {/* Navigation Breadcrumb */}
        <div className="flex items-center gap-2 text-xs text-slate-400">
          <Link to="/explore" className="hover:text-cyan-300 transition flex items-center gap-1">
            <ArrowLeft size={14} /> Back to Explore Hub
          </Link>
          <span>/</span>
          <span className="text-slate-200">Creator Profile</span>
        </div>

        {/* Header & Bio Section */}
        {isLoading ? (
          <div className="flex min-h-[250px] items-center justify-center">
            <div className="h-8 w-8 animate-spin rounded-full border-2 border-cyan-400 border-t-transparent" />
          </div>
        ) : error ? (
          <div className="flex flex-col items-center justify-center p-8 rounded-2xl border border-red-500/20 bg-red-500/5 text-center space-y-4">
            <p className="text-red-400 text-sm">{error}</p>
            <button
              onClick={fetchProfile}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-red-500/20 text-red-200 hover:bg-red-500/30 text-xs font-medium transition"
            >
              <RefreshCw size={14} /> Retry
            </button>
          </div>
        ) : profile ? (
          <div className="space-y-8">
            <div className="p-6 md:p-8 rounded-2xl border border-white/10 bg-gradient-to-r from-white/5 to-transparent space-y-4">
              <div className="flex items-start gap-4">
                <div className="h-16 w-16 rounded-full bg-gradient-to-br from-cyan-500 to-violet-600 flex items-center justify-center text-white font-bold text-2xl shadow-glow">
                  {profile.fullName ? profile.fullName.charAt(0).toUpperCase() : profile.username.charAt(0).toUpperCase()}
                </div>
                <div className="space-y-1">
                  <h1 className="text-2xl md:text-3xl font-extrabold text-white">{profile.fullName}</h1>
                  <p className="text-xs font-mono text-cyan-400">@{profile.username}</p>
                  <p className="text-xs text-slate-400 max-w-xl pt-1 leading-relaxed">
                    {profile.bio || "No author biography provided."}
                  </p>
                </div>
              </div>

              {/* Creator Top Concepts */}
              {profile.topConcepts && profile.topConcepts.length > 0 && (
                <div className="pt-4 border-t border-white/10 flex flex-wrap items-center gap-2 text-xs">
                  <span className="text-slate-400 flex items-center gap-1 text-[11px] font-medium mr-1">
                    <Tag size={12} /> Primary Concepts:
                  </span>
                  {profile.topConcepts.map((concept) => (
                    <Link
                      key={concept}
                      to={`/explore/concepts?q=${encodeURIComponent(concept)}`}
                      className="px-2.5 py-1 rounded-md bg-white/5 hover:bg-cyan-500/20 hover:text-cyan-200 border border-white/10 text-slate-300 text-[11px] transition"
                    >
                      {concept}
                    </Link>
                  ))}
                </div>
              )}
            </div>

            {/* Published Public Networks */}
            <div className="space-y-4">
              <div className="flex items-center justify-between border-b border-white/10 pb-3">
                <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-300">
                  Published Public Networks ({profile.totalPublicNetworks})
                </h2>
              </div>

              {profile.publicNetworks.length === 0 ? (
                <div className="p-8 rounded-xl border border-white/10 bg-white/5 text-center text-slate-400 text-xs">
                  This creator has not published any public knowledge networks yet.
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {profile.publicNetworks.map((network) => (
                    <div
                      key={network.id}
                      className="flex flex-col justify-between rounded-xl border border-white/10 bg-white/5 p-5 hover:border-cyan-500/30 transition"
                    >
                      <div className="space-y-2">
                        <div className="flex items-center justify-between text-xs text-slate-400">
                          <span className="inline-flex items-center gap-1 text-[11px] text-cyan-300 font-medium">
                            <ShieldCheck size={12} /> {network.licenseType?.replace(/_/g, " ") || "CC BY 4.0"}
                          </span>
                          <span>{network.nodeCount || 0} Concepts</span>
                        </div>

                        <Link to={`/graphs/${network.id}`}>
                          <h3 className="text-base font-bold text-white hover:text-cyan-300 transition">
                            {network.title || network.name}
                          </h3>
                        </Link>

                        <p className="text-xs text-slate-400 line-clamp-2">
                          {network.description || "No description provided."}
                        </p>
                      </div>

                      <div className="mt-4 pt-3 border-t border-white/10 flex items-center justify-between text-xs">
                        <span className="text-slate-500 text-[11px]">
                          {network.publishedAt ? new Date(network.publishedAt).toLocaleDateString() : "Published"}
                        </span>
                        <Link
                          to={`/graphs/${network.id}`}
                          className="text-cyan-400 hover:text-cyan-300 font-medium inline-flex items-center gap-1 transition"
                        >
                          View Graph <ArrowRight size={13} />
                        </Link>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
}
