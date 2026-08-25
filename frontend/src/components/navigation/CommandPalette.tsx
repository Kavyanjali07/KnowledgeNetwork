import { Command, FilePlus2, GitBranch, Network, Search, Settings, Users } from "lucide-react";
import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useGlobalSearch } from "../../features/search/hooks/useGlobalSearch";

const commands = [
  { label: "Create node", hint: "Graph", icon: FilePlus2, action: "create-node" },
  { label: "Search knowledge graph", hint: "Search", icon: Search, action: "open-search" },
  { label: "Open relationship map", hint: "Graph", icon: GitBranch, action: "open-relationships" },
  { label: "Invite member", hint: "Workspace", icon: Users, action: "invite-member" },
  { label: "Open graph canvas", hint: "Navigation", icon: Network, action: "open-canvas" },
  { label: "Workspace settings", hint: "Settings", icon: Settings, action: "workspace-settings" }
];

export function CommandPalette() {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const { openSearch } = useGlobalSearch();

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        setOpen((value) => !value);
      }

      if (event.key === "Escape") {
        setOpen(false);
      }
    }

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, []);

  const handleCommand = (action: string) => {
    setOpen(false);

    if (action === "open-search") {
      openSearch();
    } else if (action === "create-node") {
      openSearch();
    } else if (action === "open-relationships") {
      navigate("/graphs");
    } else if (action === "open-canvas") {
      navigate("/graph");
    } else if (action === "workspace-settings") {
      navigate("/settings");
    } else if (action === "invite-member") {
      navigate("/settings");
    }
  };

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          className="fixed inset-0 z-[70] flex items-start justify-center bg-black/58 px-4 pt-[14vh] backdrop-blur-md"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onMouseDown={() => setOpen(false)}
        >
          <motion.div
            className="w-full max-w-2xl overflow-hidden rounded-2xl border border-white/12 bg-[rgba(8,12,20,0.9)] shadow-glass backdrop-blur-2xl"
            initial={{ opacity: 0, scale: 0.98, y: -8 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.98, y: -8 }}
            transition={{ duration: 0.16, ease: [0.16, 1, 0.3, 1] }}
            onMouseDown={(event) => event.stopPropagation()}
          >
            <div className="flex items-center gap-3 border-b border-white/10 px-4 py-3">
              <Command size={18} className="text-cyan-300" />
              <input
                autoFocus
                className="h-10 flex-1 bg-transparent text-sm outline-none placeholder:text-muted-foreground"
                placeholder="Type a command or search the graph..."
              />
              <span className="rounded-md border border-white/10 px-1.5 py-0.5 text-xs text-muted-foreground">Esc</span>
            </div>
            <div className="p-2">
              {commands.map((command) => (
                <button
                  key={command.label}
                  onClick={() => handleCommand(command.action)}
                  className="flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left text-sm hover:bg-white/8"
                >
                  <span className="flex h-8 w-8 items-center justify-center rounded-lg border border-white/10 bg-white/[0.045] text-slate-300">
                    <command.icon size={16} />
                  </span>
                  <span>{command.label}</span>
                  <span className="ml-auto text-xs text-muted-foreground">{command.hint}</span>
                </button>
              ))}
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

