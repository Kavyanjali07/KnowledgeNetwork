export type ProfileStat = {
  label: string;
  value: string;
  change: string;
  accent: string;
};

export type Achievement = {
  id: string;
  title: string;
  description: string;
  icon: string;
  unlocked: boolean;
  unlockedAt?: string;
};

export type ContributionDay = {
  date: string;
  count: number;
};

export type ThemePreference = "dark" | "light" | "system";

export type SecuritySettings = {
  twoFactorEnabled: boolean;
  twoFactorMethod: "app" | "sms" | null;
  lastPasswordChange: string;
  activeSessions: number;
  loginAlertsEnabled: boolean;
};
