import { Check, Circle } from "lucide-react";
import { cn } from "../../../lib/cn";
import { getPasswordStrength } from "../utils/password-strength";

export function PasswordStrengthMeter({ password }: { password: string }) {
  const strength = getPasswordStrength(password);
  const progress = `${Math.max(strength.score, password ? 1 : 0) * 20}%`;

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between text-xs">
        <span className="text-muted-foreground">Password strength</span>
        <span className="font-medium text-slate-200">{password ? strength.label : "Not started"}</span>
      </div>
      <div className="h-1.5 overflow-hidden rounded-full bg-white/10">
        <div
          className={cn("h-full rounded-full bg-gradient-to-r transition-all duration-300", strength.color)}
          style={{ width: progress }}
        />
      </div>
      <div className="grid grid-cols-2 gap-2">
        {strength.checks.map((check) => (
          <div key={check.label} className="flex items-center gap-1.5 text-xs text-muted-foreground">
            {check.valid ? <Check size={13} className="text-emerald-300" /> : <Circle size={13} />}
            {check.label}
          </div>
        ))}
      </div>
    </div>
  );
}
