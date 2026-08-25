import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { AlertCircle, CheckCircle2, Edit3, LogOut, Save, Share2, UserCheck, UserPlus, X } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Button } from "../../../components/ui/button";
import { EmptyState } from "../../../components/ui/empty-state";
import { Input } from "../../../components/ui/input";
import { Skeleton } from "../../../components/ui/skeleton";
import { useToast } from "../../../components/ui/toast";
import { profileApi, userApi } from "../../../services";
import { useAuth } from "../../../lib/auth-context";
import { GraphCard } from "../../graphs/components/GraphCard";
import { mapWorkspaceResponseToManagedGraph } from "../../graphs/data/graphs";
import type { ContributionDay, ProfileStat } from "../types";
import { AchievementsGrid } from "../components/AchievementsGrid";
import { ContributionGraph } from "../components/ContributionGraph";
import { ProfileAnalytics } from "../components/ProfileAnalytics";

type ProfileFormState = {
  firstName: string;
  lastName: string;
  bio: string;
  status: string;
};

function formatNumber(value: number) {
  return new Intl.NumberFormat("en", { notation: "compact", maximumFractionDigits: 1 }).format(value);
}

function buildContributionDays(total: number): ContributionDay[] {
  const days: ContributionDay[] = [];
  const today = new Date();
  const seed = Math.max(total, 1);

  for (let index = 139; index >= 0; index--) {
    const date = new Date(today);
    date.setDate(date.getDate() - index);
    const active = (index + seed) % 5 !== 0;
    const count = active ? ((index * 7 + seed) % 4) + 1 : 0;
    days.push({ date: date.toISOString(), count });
  }

  return days;
}

function initialsFor(firstName?: string, lastName?: string, email?: string) {
  const initials = [firstName, lastName]
    .filter(Boolean)
    .map((part) => part?.trim()[0])
    .join("")
    .toUpperCase();
  return initials || email?.trim()[0]?.toUpperCase() || "?";
}

export function ProfilePage() {
  const { currentUser, updateCurrentUser, logout } = useAuth();
  const { addToast } = useToast();
  const queryClient = useQueryClient();
  const [searchParams] = useSearchParams();
  const viewedUserId = searchParams.get("userId");
  const isOwnProfile = !viewedUserId || viewedUserId === currentUser?.id;
  const [isEditing, setIsEditing] = useState(false);
  const [form, setForm] = useState<ProfileFormState>({ firstName: "", lastName: "", bio: "", status: "online" });

  const profileQuery = useQuery({
    queryKey: ["profile", isOwnProfile ? "me" : viewedUserId],
    queryFn: () => (isOwnProfile || !viewedUserId ? profileApi.me() : profileApi.getUser(viewedUserId)).then((response) => response.data),
    enabled: isOwnProfile || Boolean(viewedUserId)
  });

  const profile = profileQuery.data;
  const graphs = useMemo(() => (profile?.graphs ?? []).map(mapWorkspaceResponseToManagedGraph), [profile?.graphs]);

  const followStatusQuery = useQuery({
    queryKey: ["profile-follow-status", profile?.id],
    queryFn: () => userApi.isFollowing(profile!.id).then((response) => response.data),
    enabled: Boolean(profile?.id && !isOwnProfile)
  });

  useEffect(() => {
    if (!profile) return;
    setForm({
      firstName: profile.firstName ?? "",
      lastName: profile.lastName ?? "",
      bio: profile.bio ?? "",
      status: profile.status ?? "online"
    });
  }, [profile]);

  const saveProfile = useMutation({
    mutationFn: () => profileApi.updateMe(form),
    onSuccess: ({ data }) => {
      queryClient.setQueryData(["profile", "me"], data);
      if (currentUser) {
        updateCurrentUser({
          ...currentUser,
          firstName: data.firstName,
          lastName: data.lastName,
          bio: data.bio,
          status: data.status
        });
      }
      setIsEditing(false);
      addToast({ type: "success", title: "Profile saved", description: "Your profile changes were persisted." });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Profile save failed", description: error.message });
    }
  });

  const followMutation = useMutation({
    mutationFn: async () => {
      if (!profile) return;
      if (followStatusQuery.data) {
        await userApi.unfollowUser(profile.id);
      } else {
        await userApi.followUser(profile.id);
      }
    },
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ["profile-follow-status", profile?.id] });
      const previous = followStatusQuery.data;
      queryClient.setQueryData(["profile-follow-status", profile?.id], !previous);
      return { previous };
    },
    onError: (_error, _variables, context) => {
      queryClient.setQueryData(["profile-follow-status", profile?.id], context?.previous);
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["profile-follow-status", profile?.id] });
      queryClient.invalidateQueries({ queryKey: ["profile", viewedUserId] });
    }
  });

  const statistics = profile?.statistics;
  const analytics: ProfileStat[] = [
    { label: "Graphs owned", value: formatNumber(statistics?.graphsOwned ?? 0), change: `${graphs.length} visible graphs`, accent: "from-cyan-300 to-violet-400" },
    { label: "Nodes created", value: formatNumber(statistics?.nodesCreated ?? 0), change: "Across owned graphs", accent: "from-emerald-300 to-cyan-400" },
    { label: "Edges authored", value: formatNumber(statistics?.edgesAuthored ?? 0), change: "Across owned graphs", accent: "from-violet-300 to-fuchsia-400" },
    { label: "Posts", value: formatNumber(statistics?.posts ?? 0), change: `${formatNumber(statistics?.followers ?? 0)} followers`, accent: "from-amber-300 to-rose-400" }
  ];
  const contributionDays = useMemo(
    () => buildContributionDays((statistics?.nodesCreated ?? 0) + (statistics?.edgesAuthored ?? 0) + (statistics?.posts ?? 0)),
    [statistics?.edgesAuthored, statistics?.nodesCreated, statistics?.posts]
  );

  if (profileQuery.isLoading) {
    return (
      <section className="mx-auto mt-6 max-w-7xl pt-4">
        <Skeleton className="h-28 w-full" />
        <div className="mt-6 grid gap-6 md:grid-cols-[240px_1fr]">
          <Skeleton className="h-64 w-full" />
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {Array.from({ length: 4 }).map((_, index) => <Skeleton key={index} className="h-32 w-full" />)}
          </div>
        </div>
      </section>
    );
  }

  if (profileQuery.isError || !profile) {
    return (
      <section className="mx-auto mt-6 max-w-7xl pt-4">
        <EmptyState
          icon={<UserPlus size={28} className="text-rose-300" />}
          title="Unable to load profile"
          description={(profileQuery.error as any)?.message || "The requested profile could not be loaded."}
          action={<Button variant="secondary" onClick={() => profileQuery.refetch()}>Retry</Button>}
        />
      </section>
    );
  }

  const displayName = `${profile.firstName} ${profile.lastName}`.trim() || profile.email;
  const bio = profile.bio?.trim() || "No bio added yet.";
  const status = profile.status?.trim() || "online";

  return (
    <section className="mx-auto mt-6 max-w-7xl pt-4">
      <motion.div
        className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between"
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.28, ease: [0.16, 1, 0.3, 1] }}
      >
        <div className="flex items-center gap-4">
          <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-gradient-to-br from-violet-500 to-cyan-400 text-2xl font-bold text-slate-950 shadow-glow">
            {initialsFor(profile.firstName, profile.lastName, profile.email)}
          </div>
          <div>
            <p className="text-sm text-muted-foreground">Profile</p>
            <h1 className="text-2xl font-semibold tracking-[-0.015em] md:text-4xl">{displayName}</h1>
            <div className="mt-1 flex flex-wrap items-center gap-2">
              <span className="text-sm font-medium text-slate-300">{profile.email}</span>
              {profile.emailVerified ? (
                <span className="inline-flex items-center gap-1 rounded-full border border-emerald-500/30 bg-emerald-500/10 px-2 py-0.5 text-[11px] font-medium text-emerald-300">
                  <CheckCircle2 size={12} /> Verified
                </span>
              ) : (
                <span className="inline-flex items-center gap-1 rounded-full border border-amber-500/30 bg-amber-500/10 px-2 py-0.5 text-[11px] font-medium text-amber-300">
                  <AlertCircle size={12} /> Unverified
                </span>
              )}
            </div>
          </div>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <Button variant="secondary" className="h-10 px-3 text-xs" onClick={() => navigator.clipboard?.writeText(window.location.href)}>
            <Share2 size={15} />
            Share
          </Button>
          {!isOwnProfile && (
            <Button className="h-10 px-3 text-xs" disabled={followMutation.isPending} onClick={() => followMutation.mutate()}>
              {followStatusQuery.data ? <UserCheck size={15} /> : <UserPlus size={15} />}
              {followStatusQuery.data ? "Following" : "Follow"}
            </Button>
          )}
          {isOwnProfile && !isEditing && (
            <>
              <Button className="h-10 px-3 text-xs" onClick={() => setIsEditing(true)}>
                <Edit3 size={15} />
                Edit profile
              </Button>
              <Button
                variant="secondary"
                className="h-10 px-3 text-xs border border-rose-500/30 bg-rose-500/10 text-rose-300 hover:bg-rose-500/20 hover:text-rose-200"
                onClick={logout}
              >
                <LogOut size={15} />
                Log out
              </Button>
            </>
          )}
          {isOwnProfile && isEditing && (
            <>
              <Button variant="secondary" className="h-10 px-3 text-xs" onClick={() => setIsEditing(false)}>
                <X size={15} />
                Cancel
              </Button>
              <Button className="h-10 px-3 text-xs" disabled={saveProfile.isPending} onClick={() => saveProfile.mutate()}>
                <Save size={15} />
                Save
              </Button>
            </>
          )}
        </div>
      </motion.div>

      <div className="grid gap-6 md:grid-cols-[260px_1fr]">
        <aside className="space-y-6 md:block">
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.05 }}
            className="rounded-2xl border border-white/10 bg-white/[0.03] p-5"
          >
            <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">Bio</p>
            {isEditing ? (
              <div className="mt-3 space-y-3">
                <Input value={form.firstName} onChange={(event) => setForm((value) => ({ ...value, firstName: event.target.value }))} placeholder="First name" />
                <Input value={form.lastName} onChange={(event) => setForm((value) => ({ ...value, lastName: event.target.value }))} placeholder="Last name" />
                <textarea
                  value={form.bio}
                  onChange={(event) => setForm((value) => ({ ...value, bio: event.target.value }))}
                  maxLength={500}
                  placeholder="Bio"
                  className="min-h-28 w-full rounded-lg border border-white/10 bg-white/[0.055] px-3 py-2 text-sm text-slate-100 outline-none transition placeholder:text-muted-foreground focus:border-cyan-300/45"
                />
                <select
                  value={form.status}
                  onChange={(event) => setForm((value) => ({ ...value, status: event.target.value }))}
                  className="h-11 w-full rounded-lg border border-white/10 bg-[rgba(8,12,20,0.96)] px-3 text-sm text-slate-100 outline-none focus:border-cyan-300/45"
                >
                  <option value="online">Online</option>
                  <option value="focused">Focused</option>
                  <option value="away">Away</option>
                </select>
              </div>
            ) : (
              <p className="mt-2 text-sm leading-6 text-slate-300">{bio}</p>
            )}
            <div className="mt-4 grid grid-cols-3 gap-2 border-t border-white/10 pt-4 text-center">
              <div>
                <p className="text-sm font-semibold">{formatNumber(statistics?.posts ?? 0)}</p>
                <p className="text-xs text-muted-foreground">Posts</p>
              </div>
              <div>
                <p className="text-sm font-semibold">{formatNumber(statistics?.followers ?? 0)}</p>
                <p className="text-xs text-muted-foreground">Followers</p>
              </div>
              <div>
                <p className="text-sm font-semibold">{formatNumber(statistics?.following ?? 0)}</p>
                <p className="text-xs text-muted-foreground">Following</p>
              </div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="rounded-2xl border border-white/10 bg-white/[0.03] p-5"
          >
            <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">Status</p>
            <div className="mt-3 flex items-center gap-2 text-xs capitalize text-slate-300">
              <span className="h-2 w-2 rounded-full bg-emerald-400" />
              {status}
            </div>
          </motion.div>
        </aside>

        <div className="space-y-6">
          <ProfileAnalytics stats={analytics} />
          <ContributionGraph days={contributionDays} />
          <div>
            <h2 className="mb-3 text-sm font-semibold text-slate-200">Graphs</h2>
            {graphs.length > 0 ? (
              <div className="columns-1 gap-4 lg:columns-2">
                {graphs.map((graph) => <GraphCard key={graph.id} graph={graph} />)}
              </div>
            ) : (
              <EmptyState title="No graphs yet" description="Owned graphs will appear here after they are created." />
            )}
          </div>
          <div>
            <h2 className="mb-3 text-sm font-semibold text-slate-200">Achievements</h2>
            <AchievementsGrid />
          </div>
        </div>
      </div>
    </section>
  );
}
