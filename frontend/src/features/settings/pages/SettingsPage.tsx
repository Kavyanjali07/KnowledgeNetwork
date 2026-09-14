import { motion } from "framer-motion";
import { LogOut, ShieldCheck } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "../../../components/ui/button";
import { useTheme } from "../../../components/theme/ThemeProvider";
import { useAuth } from "../../../lib/auth-context";
import { defaultSecuritySettings } from "../../profile/data/profile";
import type { ThemePreference } from "../../profile/types";

const themeOptions: Array<{ value: ThemePreference; label: string; description: string }> = [
  { value: "dark", label: "Dark", description: "Deep glassmorphism dark theme" },
  { value: "light", label: "Light", description: "Clean light theme for daytime" },
  { value: "system", label: "System", description: "Match your OS preference" }
];

export function SettingsPage() {
  const { theme, setTheme } = useTheme();
  const { currentUser, logout } = useAuth();
  const security = defaultSecuritySettings;
  const account = currentUser
    ? { name: `${currentUser.firstName} ${currentUser.lastName}`, email: currentUser.email }
    : { name: "Kavya Rao", email: "kavya@knowledgenetwork.local" };

  return (
    <section className="mx-auto mt-6 max-w-4xl pt-4">
      <motion.div
        className="mb-6"
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.28, ease: [0.16, 1, 0.3, 1] }}
      >
        <p className="text-sm text-muted-foreground">Settings</p>
        <h1 className="mt-2 text-2xl font-semibold tracking-[-0.015em] md:text-4xl">Profile & settings</h1>
        <p className="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">
          Manage your appearance, security preferences, and account details.
        </p>
      </motion.div>

      <div className="grid gap-4 sm:gap-6">
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.03 }}
          className="rounded-2xl border border-white/10 bg-white/[0.03] p-4 sm:p-5"
        >
          <h2 className="text-sm font-semibold text-slate-200">Account</h2>
          <div className="mt-3 grid gap-3 sm:grid-cols-2">
            <div className="rounded-xl border border-white/10 bg-white/[0.04] p-3">
              <p className="text-xs text-muted-foreground">Name</p>
              <p className="mt-1 text-sm font-medium text-slate-100">{account.name}</p>
            </div>
            <div className="rounded-xl border border-white/10 bg-white/[0.04] p-3">
              <p className="text-xs text-muted-foreground">Email</p>
              <p className="mt-1 text-sm font-medium text-slate-100">{account.email}</p>
            </div>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.05 }}
          className="rounded-2xl border border-white/10 bg-white/[0.03] p-4 sm:p-5"
        >
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-sm font-semibold text-slate-200">Appearance</h2>
              <p className="text-xs text-muted-foreground">Choose your preferred theme mode.</p>
            </div>
          </div>
          <div className="mt-4 grid gap-3 sm:grid-cols-3">
            {themeOptions.map((option) => {
              const isActive = theme === option.value;
              return (
                <button
                  key={option.value}
                  onClick={() => setTheme(option.value)}
                  className={`rounded-xl border p-4 text-left transition ${
                    isActive
                      ? "border-cyan-500/40 bg-cyan-500/[0.08] shadow-glow"
                      : "border-white/10 bg-white/[0.03] hover:border-white/16 hover:bg-white/[0.06]"
                  }`}
                >
                  <p className="text-sm font-semibold text-slate-100">{option.label}</p>
                  <p className="mt-1 text-xs text-muted-foreground">{option.description}</p>
                  {isActive && (
                    <span className="mt-2 inline-flex rounded-full border border-cyan-500/40 bg-cyan-500/15 px-2 py-0.5 text-[10px] text-cyan-200">
                      Active
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="rounded-2xl border border-white/10 bg-white/[0.03] p-4 sm:p-5"
        >
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-sm font-semibold text-slate-200">Security</h2>
              <p className="text-xs text-muted-foreground">Protect your account with 2FA and session controls.</p>
            </div>
          </div>
          <div className="mt-4 space-y-3 sm:space-y-4">
            <div className="flex items-center justify-between rounded-xl border border-white/10 bg-white/[0.04] p-3 sm:p-4">
              <div>
                <p className="text-sm font-semibold text-slate-200">Two-factor authentication</p>
                <p className="text-xs text-muted-foreground">
                  {security.twoFactorMethod === "app" ? "Authenticator app" : security.twoFactorMethod === "sms" ? "SMS" : "Disabled"}
                </p>
              </div>
              <span
                className={`rounded-full border px-2 py-0.5 text-[10px] ${
                  security.twoFactorEnabled
                    ? "border-emerald-500/30 bg-emerald-500/10 text-emerald-200"
                    : "border-white/10 bg-white/[0.04] text-slate-400"
                }`}
              >
                {security.twoFactorEnabled ? "Enabled" : "Disabled"}
              </span>
            </div>

            <div className="flex items-center justify-between rounded-xl border border-white/10 bg-white/[0.04] p-3 sm:p-4">
              <div>
                <p className="text-sm font-semibold text-slate-200">Login alerts</p>
                <p className="text-xs text-muted-foreground">Get notified of new sign-ins.</p>
              </div>
              <span
                className={`rounded-full border px-2 py-0.5 text-[10px] ${
                  security.loginAlertsEnabled
                    ? "border-cyan-500/30 bg-cyan-500/10 text-cyan-200"
                    : "border-white/10 bg-white/[0.04] text-slate-400"
                }`}
              >
                {security.loginAlertsEnabled ? "Enabled" : "Disabled"}
              </span>
            </div>

            <div className="flex items-center justify-between rounded-xl border border-white/10 bg-white/[0.04] p-3 sm:p-4">
              <div>
                <p className="text-sm font-semibold text-slate-200">Active sessions</p>
                <p className="text-xs text-muted-foreground">Last password changed {security.lastPasswordChange}</p>
              </div>
              <span className="rounded-full border border-white/10 bg-white/[0.04] px-2 py-0.5 text-[10px] text-slate-300">
                {security.activeSessions} devices
              </span>
            </div>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.11 }}
          className="rounded-2xl border border-white/10 bg-white/[0.03] p-4 sm:p-5 backdrop-blur-xl"
        >
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <div className="flex items-center gap-2">
                <ShieldCheck size={16} className="text-cyan-400" />
                <h2 className="text-sm font-semibold text-slate-100">Audit Log & Compliance</h2>
              </div>
              <p className="mt-1 text-xs text-muted-foreground">
                View immutable history of authentication, workspace settings, graph edits, and node modifications.
              </p>
            </div>
            <Link to="/audit-log">
              <Button className="h-9 shrink-0 border border-cyan-500/30 bg-cyan-500/10 text-xs text-cyan-300 hover:bg-cyan-500/20">
                View Audit Trail
              </Button>
            </Link>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.12 }}
          className="rounded-2xl border border-rose-500/20 bg-rose-500/[0.03] p-4 sm:p-5"
        >
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="text-sm font-semibold text-rose-200">Account session</h2>
              <p className="text-xs text-muted-foreground">Sign out of your account on this device and revoke active refresh tokens.</p>
            </div>
            <Button
              className="h-10 shrink-0 border border-rose-500/30 bg-rose-500/10 text-xs text-rose-300 hover:bg-rose-500/20 hover:text-rose-200"
              onClick={logout}
            >
              <LogOut size={15} />
              Log out
            </Button>
          </div>
        </motion.div>
      </div>
    </section>
  );
}
