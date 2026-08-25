const shortcuts = [
  ["N", "New node"],
  ["Del", "Delete selection"],
  ["F", "Fit view"],
  ["⌘S", "Save"]
];

export function KeyboardShortcuts() {
  return (
    <div className="pointer-events-none absolute bottom-4 left-4 z-10 hidden rounded-xl border border-white/10 bg-[rgba(8,12,20,0.7)] p-3 text-xs text-muted-foreground shadow-glass backdrop-blur-2xl lg:block">
      <div className="grid grid-cols-2 gap-x-4 gap-y-2">
        {shortcuts.map(([key, label]) => (
          <div key={key} className="flex items-center gap-2">
            <span className="rounded-md border border-white/10 bg-white/[0.06] px-1.5 py-0.5 font-mono text-[11px] text-slate-200">
              {key}
            </span>
            {label}
          </div>
        ))}
      </div>
    </div>
  );
}
