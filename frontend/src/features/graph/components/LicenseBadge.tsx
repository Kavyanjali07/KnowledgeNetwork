import { useState } from "react";
import { Shield, ExternalLink, Check, X, Scale } from "lucide-react";
import { LICENSE_METADATA, type LicenseType } from "../../../services/graphApi";

interface LicenseBadgeProps {
  licenseType?: LicenseType;
  showDetails?: boolean;
  className?: string;
}

export function LicenseBadge({ licenseType = "CC_BY_4_0", showDetails = false, className = "" }: LicenseBadgeProps) {
  const [isOpen, setIsOpen] = useState(false);
  const metadata = LICENSE_METADATA[licenseType] || LICENSE_METADATA.CC_BY_4_0;

  return (
    <div className={`relative inline-block ${className}`}>
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        onMouseEnter={() => setIsOpen(true)}
        onMouseLeave={() => setIsOpen(false)}
        className="group inline-flex items-center gap-1.5 rounded-full border border-cyan-400/30 bg-cyan-400/10 px-2.5 py-1 text-xs font-semibold text-cyan-200 transition hover:border-cyan-400/60 hover:bg-cyan-400/20"
      >
        <Scale size={13} className="text-cyan-300" />
        <span>{metadata.shortName}</span>
      </button>

      {(isOpen || showDetails) && (
        <div className="absolute left-0 top-full z-40 mt-2 w-72 rounded-xl border border-white/10 bg-[rgba(10,15,28,0.97)] p-4 shadow-2xl backdrop-blur-2xl text-left text-xs text-slate-200">
          <div className="flex items-center justify-between font-semibold text-slate-100 pb-2 border-b border-white/10">
            <div className="flex items-center gap-1.5">
              <Shield size={14} className="text-cyan-400" />
              <span>{metadata.fullName}</span>
            </div>
          </div>

          <p className="mt-2 text-[11px] leading-relaxed text-slate-300">
            {metadata.description}
          </p>

          <div className="mt-3 space-y-1.5 pt-2 border-t border-white/10">
            <div className="flex items-center justify-between text-[11px]">
              <span className="text-slate-400">Attribution (BY)</span>
              <span className="flex items-center gap-1 font-medium">
                {metadata.requiresAttribution ? (
                  <>
                    <Check size={12} className="text-emerald-400" /> Required
                  </>
                ) : (
                  <>
                    <X size={12} className="text-slate-400" /> Not Required
                  </>
                )}
              </span>
            </div>

            <div className="flex items-center justify-between text-[11px]">
              <span className="text-slate-400">Commercial Use</span>
              <span className="flex items-center gap-1 font-medium">
                {metadata.allowsCommercial ? (
                  <>
                    <Check size={12} className="text-emerald-400" /> Allowed
                  </>
                ) : (
                  <>
                    <X size={12} className="text-rose-400" /> Restricted
                  </>
                )}
              </span>
            </div>

            <div className="flex items-center justify-between text-[11px]">
              <span className="text-slate-400">Derivative Works</span>
              <span className="flex items-center gap-1 font-medium">
                {metadata.allowsDerivatives ? (
                  <>
                    <Check size={12} className="text-emerald-400" /> Permitted
                  </>
                ) : (
                  <>
                    <X size={12} className="text-rose-400" /> Blocked (ND)
                  </>
                )}
              </span>
            </div>

            <div className="flex items-center justify-between text-[11px]">
              <span className="text-slate-400">ShareAlike (SA)</span>
              <span className="flex items-center gap-1 font-medium">
                {metadata.requiresShareAlike ? (
                  <>
                    <Check size={12} className="text-amber-400" /> Required
                  </>
                ) : (
                  <>
                    <X size={12} className="text-slate-400" /> Not Required
                  </>
                )}
              </span>
            </div>
          </div>

          <a
            href="https://creativecommons.org/licenses/"
            target="_blank"
            rel="noopener noreferrer"
            className="mt-3 flex items-center justify-end gap-1 text-[10px] text-cyan-400 hover:underline"
          >
            <span>Learn more about CC Licenses</span>
            <ExternalLink size={10} />
          </a>
        </div>
      )}
    </div>
  );
}
