import type { InputHTMLAttributes } from "react";
import { Label } from "../../../components/ui/label";
import { Input } from "../../../components/ui/input";

type AuthFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  error?: string;
};

export function AuthField({ id, label, error, ...props }: AuthFieldProps) {
  const fieldId = id || props.name || label.toLowerCase().replace(/\s+/g, "-");
  return (
    <div className="space-y-2">
      <Label htmlFor={fieldId}>{label}</Label>
      <Input id={fieldId} invalid={Boolean(error)} {...props} />
      <p className="min-h-4 text-xs text-rose-200">{error}</p>
    </div>
  );
}
