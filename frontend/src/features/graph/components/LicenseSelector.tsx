import { LICENSE_METADATA, type LicenseType } from "../../../services/graphApi";
import { Check, Info, Shield, ExternalLink } from "lucide-react";

interface LicenseSelectorProps {
  value: LicenseType;
  onChange: (license: LicenseType) => void;
  customAttribution?: string;
  onCustomAttributionChange?: (val: string) => void;
}

const LICENSE_OPTIONS: LicenseType[] = [
  "CC_BY_4_0",
  "CC_BY_SA_4_0",
  "CC_BY_NC_4_0",
  "CC_BY_NC_SA_4_0",
  "CC_BY_ND_4_0",
  "CC_BY_NC_ND_4_0",
  "CC0_1_0",
  "ALL_RIGHTS_RESERVED"
];

export function LicenseSelector({
  value,
  onChange,
  customAttribution = "",
  onCustomAttributionChange
}: LicenseSelectorProps) {
  const selectedMeta = LICENSE_METADATA[value] || LICENSE_METADATA.CC_BY_4_0;

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300">
          Publication License
        </label>
        <a
          href="https://creativecommons.org/licenses/"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-1 text-[11px] text-cyan-400 hover:underline"
        >
          <span>CC License Guide</span>
          <ExternalLink size={11} />
        </a>
      </div>

      <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
        {LICENSE_OPTIONS.map((key) => {
          const meta = LICENSE_METADATA[key];
          const isSelected = value === key;

          return (
            <button
              key={key}
              type="button"
              onClick={() => onChange(key)}
              className={`flex flex-col items-start rounded-xl border p-3 text-left transition ${
                isSelected
                  ? "border-cyan-400 bg-cyan-400/10 text-cyan-100 shadow-sm"
                  : "border-white/10 bg-white/[0.03] text-slate-300 hover:bg-white/[0.06]"
              }`}
            >
              <div className="flex w-full items-center justify-between">
                <span className="font-semibold text-xs text-slate-100">{meta.shortName}</span>
                {isSelected && <Check size={14} className="text-cyan-400" />}
              </div>
              <p className="mt-1 line-clamp-2 text-[11px] leading-tight text-muted-foreground">
                {meta.description}
              </p>
            </button>
          );
        })}
      </div>

      <div className="rounded-xl border border-white/10 bg-white/[0.02] p-3 text-xs text-slate-300 space-y-2">
        <div className="flex items-center gap-1.5 font-medium text-cyan-200">
          <Shield size={14} />
          <span>Selected License: {selectedMeta.fullName}</span>
        </div>
        <p className="text-[11px] leading-relaxed text-muted-foreground">{selectedMeta.description}</p>
        
        <div className="grid grid-cols-2 gap-2 pt-1 border-t border-white/10 text-[11px]">
          <div>
            <span className="text-slate-400">Derivatives: </span>
            <span className={selectedMeta.allowsDerivatives ? "text-emerald-400 font-medium" : "text-rose-400 font-medium"}>
              {selectedMeta.allowsDerivatives ? "Permitted" : "Blocked (No derivatives)"}
            </span>
          </div>
          <div>
            <span className="text-slate-400">Commercial: </span>
            <span className={selectedMeta.allowsCommercial ? "text-emerald-400 font-medium" : "text-rose-400 font-medium"}>
              {selectedMeta.allowsCommercial ? "Allowed" : "Non-commercial only"}
            </span>
          </div>
        </div>
      </div>

      {onCustomAttributionChange && (
        <div className="pt-1">
          <label className="block text-xs font-medium text-slate-400 mb-1">
            Custom Attribution Note (Optional)
          </label>
          <input
            type="text"
            value={customAttribution}
            onChange={(e) => onCustomAttributionChange(e.target.value)}
            placeholder="e.g. Originally created for KnowledgeNetwork Foundation Research"
            className="w-full rounded-xl border border-white/10 bg-white/[0.04] p-2.5 text-xs text-slate-100 placeholder:text-muted-foreground focus:border-cyan-400 focus:outline-none"
            maxLength={500}
          />
        </div>
      )}
    </div>
  );
}
