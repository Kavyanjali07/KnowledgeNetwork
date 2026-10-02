import { Network } from "lucide-react";
import { Link } from "react-router-dom";

export function LandingFooter() {
  return (
    <footer className="border-t border-white/8 py-10">
      <div className="mx-auto flex max-w-[1200px] flex-col items-center justify-between gap-4 px-6 sm:flex-row md:px-10">
        <div className="flex items-center gap-2">
          <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-violet-500 to-cyan-400">
            <Network size={14} className="text-white" />
          </span>
          <span className="text-sm font-medium text-slate-400">
            Knowledge<span className="text-slate-300">Network</span>
          </span>
        </div>
        <div className="flex items-center gap-6 text-xs text-slate-500">
          <Link to="/login" className="transition hover:text-slate-300">Log in</Link>
          <Link to="/register" className="transition hover:text-slate-300">Sign up</Link>
        </div>
        <p className="text-xs text-slate-600">
          &copy; {new Date().getFullYear()} KnowledgeNetwork
        </p>
      </div>
    </footer>
  );
}
