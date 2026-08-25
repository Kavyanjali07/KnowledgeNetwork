export type PasswordStrength = {
  score: number;
  label: string;
  color: string;
  checks: Array<{ label: string; valid: boolean }>;
};

export function getPasswordStrength(password: string): PasswordStrength {
  const checks = [
    { label: "8+ characters", valid: password.length >= 8 },
    { label: "Uppercase", valid: /[A-Z]/.test(password) },
    { label: "Lowercase", valid: /[a-z]/.test(password) },
    { label: "Number", valid: /[0-9]/.test(password) },
    { label: "Symbol", valid: /[^A-Za-z0-9]/.test(password) }
  ];

  const score = checks.filter((check) => check.valid).length;
  const label = score <= 2 ? "Weak" : score <= 4 ? "Strong" : "Excellent";
  const color =
    score <= 2
      ? "from-rose-400 to-orange-300"
      : score <= 4
        ? "from-amber-300 to-cyan-300"
        : "from-emerald-300 to-cyan-300";

  return { score, label, color, checks };
}
