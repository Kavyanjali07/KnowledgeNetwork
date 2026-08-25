import { useMemo } from "react";
import { contributionDays } from "../data/profile";
import type { ContributionDay } from "../types";

const CELL_SIZE = 10;
const CELL_GAP = 3;
const WEEKS_TO_SHOW = 20;

function levelColor(value: number): string {
  if (value === 0) return "bg-white/5";
  if (value === 1) return "bg-cyan-400/35";
  if (value === 2) return "bg-cyan-400/55";
  if (value === 3) return "bg-cyan-400/75";
  return "bg-cyan-400";
}

function monthLabel(day: { date: string }) {
  const date = new Date(day.date);
  return new Intl.DateTimeFormat("en", { month: "short" }).format(date);
}

export function ContributionGraph({ days: providedDays }: { days?: ContributionDay[] }) {
  const sourceDays = providedDays ?? contributionDays;
  const days = useMemo(() => sourceDays.slice(-WEEKS_TO_SHOW * 7), [sourceDays]);
  const weeks = useMemo(() => {
    const result: typeof days[] = [];
    for (let i = 0; i < days.length; i += 7) {
      result.push(days.slice(i, i + 7));
    }
    return result;
  }, [days]);

  const monthMarkers = useMemo(() => {
    const markers: Array<{ weekIndex: number; label: string }> = [];
    let lastMonth = "";
    weeks.forEach((week, weekIndex) => {
      const firstDay = week[0];
      if (firstDay) {
        const currentMonth = monthLabel(firstDay);
        if (currentMonth !== lastMonth) {
          markers.push({ weekIndex, label: currentMonth });
          lastMonth = currentMonth;
        }
      }
    });
    return markers;
  }, [weeks]);

  return (
    <div className="rounded-2xl border border-white/10 bg-white/[0.03] p-5">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-sm font-semibold text-slate-200">Contribution graph</h3>
          <p className="text-xs text-muted-foreground">
            {days.reduce((sum, day) => sum + day.count, 0)} contributions in the last {WEEKS_TO_SHOW} weeks
          </p>
        </div>
        <div className="flex items-center gap-2 text-[10px] text-muted-foreground">
          <span>Less</span>
          {[0, 1, 2, 3, 4].map((level) => (
            <span key={level} className={`h-3 w-3 rounded-sm ${levelColor(level)}`} />
          ))}
          <span>More</span>
        </div>
      </div>

      <div className="mt-4 overflow-x-auto">
        <div className="relative inline-flex min-w-full flex-col gap-[3px]">
          <div className="flex gap-[3px]">
            {monthMarkers.map((marker) => (
              <span
                key={marker.label}
                className="text-[10px] text-muted-foreground"
                style={{ marginLeft: marker.weekIndex * (CELL_SIZE + CELL_GAP) }}
              >
                {marker.label}
              </span>
            ))}
          </div>
          <div className="flex gap-[3px]">
            {weeks.map((week, weekIndex) => (
              <div key={weekIndex} className="flex flex-col gap-[3px]">
                {week.map((day, dayIndex) => {
                  const key = `${day.date}-${dayIndex}`;
                  return (
                    <div
                      key={key}
                      className={`h-[10px] w-[10px] rounded-sm ${levelColor(day.count)}`}
                      title={`${day.date}: ${day.count} contributions`}
                    />
                  );
                })}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
