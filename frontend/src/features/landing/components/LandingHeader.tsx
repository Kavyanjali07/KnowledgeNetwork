import { useState } from "react";
import { Link } from "react-router-dom";
import { Network, Menu, X } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";

const navLinks = [
  { label: "How it works", href: "#how-it-works" },
  { label: "Explore", href: "#explore" },
];

export function LandingHeader() {
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleAnchorClick = (e: React.MouseEvent<HTMLAnchorElement>, href: string) => {
    if (href.startsWith("#")) {
      e.preventDefault();
      const el = document.querySelector(href);
      if (el) el.scrollIntoView({ behavior: "smooth" });
      setMobileOpen(false);
    }
  };

  return (
    <header className="fixed inset-x-0 top-0 z-50">
      <div className="mx-auto flex max-w-[1200px] items-center justify-between px-6 py-5 md:px-10">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-3 group" aria-label="KnowledgeNetwork home">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-violet-500 to-cyan-400 shadow-glow transition-transform group-hover:scale-105">
            <Network size={20} className="text-white" />
          </span>
          <span className="text-lg font-semibold tracking-tight text-white">
            Knowledge<span className="text-cyan-400">Network</span>
          </span>
        </Link>

        {/* Desktop nav */}
        <nav className="hidden items-center gap-8 md:flex" aria-label="Landing navigation">
          {navLinks.map((link) => (
            <a
              key={link.label}
              href={link.href}
              onClick={(e) => handleAnchorClick(e, link.href)}
              className="text-sm font-medium text-slate-400 transition hover:text-white"
            >
              {link.label}
            </a>
          ))}
        </nav>

        {/* Desktop auth */}
        <div className="hidden items-center gap-3 md:flex">
          <Link
            to="/login"
            className="rounded-lg border border-white/12 px-5 py-2.5 text-sm font-medium text-slate-200 transition hover:border-white/25 hover:text-white"
          >
            Log in
          </Link>
          <Link
            to="/register"
            className="rounded-lg bg-gradient-to-r from-cyan-500 to-cyan-400 px-5 py-2.5 text-sm font-semibold text-slate-950 shadow-glow transition hover:shadow-lg hover:brightness-110"
          >
            Sign up
          </Link>
        </div>

        {/* Mobile hamburger */}
        <button
          className="inline-flex h-10 w-10 items-center justify-center rounded-lg border border-white/10 text-slate-300 md:hidden"
          onClick={() => setMobileOpen(!mobileOpen)}
          aria-label={mobileOpen ? "Close menu" : "Open menu"}
        >
          {mobileOpen ? <X size={20} /> : <Menu size={20} />}
        </button>
      </div>

      {/* Mobile menu */}
      <AnimatePresence>
        {mobileOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            className="overflow-hidden border-b border-white/10 bg-[rgba(5,7,11,0.96)] backdrop-blur-2xl md:hidden"
          >
            <nav className="flex flex-col gap-1 px-6 py-4" aria-label="Mobile navigation">
              {navLinks.map((link) => (
                <a
                  key={link.label}
                  href={link.href}
                  onClick={(e) => handleAnchorClick(e, link.href)}
                  className="rounded-lg px-4 py-3 text-sm font-medium text-slate-300 transition hover:bg-white/5 hover:text-white"
                >
                  {link.label}
                </a>
              ))}
              <div className="mt-3 flex flex-col gap-2 border-t border-white/10 pt-4">
                <Link
                  to="/login"
                  className="rounded-lg border border-white/12 px-4 py-3 text-center text-sm font-medium text-slate-200 transition hover:bg-white/5"
                  onClick={() => setMobileOpen(false)}
                >
                  Log in
                </Link>
                <Link
                  to="/register"
                  className="rounded-lg bg-gradient-to-r from-cyan-500 to-cyan-400 px-4 py-3 text-center text-sm font-semibold text-slate-950 transition hover:brightness-110"
                  onClick={() => setMobileOpen(false)}
                >
                  Sign up
                </Link>
              </div>
            </nav>
          </motion.div>
        )}
      </AnimatePresence>
    </header>
  );
}
