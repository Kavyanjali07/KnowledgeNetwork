import type { ProfileStat, Achievement, ContributionDay, SecuritySettings } from "../types";

export const profileStats: ProfileStat[] = [
  { label: "Graphs owned", value: "12", change: "+2 this month", accent: "from-cyan-300 to-violet-400" },
  { label: "Nodes created", value: "1,284", change: "+18% vs last month", accent: "from-emerald-300 to-cyan-400" },
  { label: "Edges authored", value: "4,821", change: "+9.7%", accent: "from-violet-300 to-fuchsia-400" },
  { label: "Team rank", value: "#3", change: "Top contributor", accent: "from-amber-300 to-rose-400" }
];

export const achievements: Achievement[] = [
  { id: "ach-1", title: "Ontology Architect", description: "Published 3 ontology versions", icon: "🏛️", unlocked: true, unlockedAt: "2026-07-21" },
  { id: "ach-2", title: "Graph Pioneer", description: "Created 1,000+ nodes across graphs", icon: "🚀", unlocked: true, unlockedAt: "2026-07-15" },
  { id: "ach-3", title: "Collaboration Star", description: "Mentioned in 50+ discussions", icon: "⭐", unlocked: true, unlockedAt: "2026-07-10" },
  { id: "ach-4", title: "Cleanup Hero", description: "Removed 500+ orphan nodes", icon: "🧹", unlocked: false },
  { id: "ach-5", title: "Early Adopter", description: "Joined within the first 30 days", icon: "🌱", unlocked: true, unlockedAt: "2026-06-05" },
  { id: "ach-6", title: "Merge Master", description: "Merged 20 feature branches", icon: "🔀", unlocked: false }
];

function buildContributionDays(count = 140): ContributionDay[] {
  const days: ContributionDay[] = [];
  const now = new Date();
  for (let i = count - 1; i >= 0; i--) {
    const date = new Date(now);
    date.setDate(date.getDate() - i);
    const roll = Math.random();
    const value = roll < 0.55 ? 0 : roll < 0.8 ? 1 : roll < 0.94 ? 2 : roll < 0.98 ? 3 : 4;
    days.push({ date: date.toISOString(), count: value });
  }
  return days;
}

export const contributionDays = buildContributionDays();

export const defaultSecuritySettings: SecuritySettings = {
  twoFactorEnabled: true,
  twoFactorMethod: "app",
  lastPasswordChange: "2026-06-18",
  activeSessions: 3,
  loginAlertsEnabled: true
};
