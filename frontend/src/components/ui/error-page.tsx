import { AlertTriangle, RefreshCcw } from "lucide-react";
import type { ComponentProps } from "react";
import React from "react";
import { cn } from "../../lib/cn";

type ErrorPageProps = ComponentProps<"div"> & {
  title?: string;
  description?: string;
  onRetry?: () => void;
};

export function ErrorPage({
  title = "Something went wrong",
  description = "We couldn&apos;t load this page. Please try again.",
  onRetry,
  className,
  ...props
}: ErrorPageProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center gap-3 rounded-2xl border border-white/10 bg-white/[0.03] p-10 text-center",
        className
      )}
      {...props}
    >
      <div className="flex h-12 w-12 items-center justify-center rounded-2xl border border-rose-400/30 bg-rose-400/10 text-rose-200">
        <AlertTriangle size={22} />
      </div>
      <h3 className="text-sm font-semibold text-slate-200">{title}</h3>
      <p className="max-w-sm text-xs text-muted-foreground">{description}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="inline-flex items-center gap-2 rounded-lg border border-white/10 bg-white/[0.055] px-3 py-2 text-xs text-slate-300 transition hover:bg-white/[0.085]"
        >
          <RefreshCcw size={13} />
          Retry
        </button>
      )}
    </div>
  );
}

export class ErrorBoundary extends React.Component<{ children: React.ReactNode }, { hasError: boolean }> {
  state = { hasError: false };

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error: unknown) {
    console.error("UI error boundary caught:", error);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex min-h-screen items-center justify-center bg-background p-6">
          <div className="text-center">
            <h1 className="text-2xl font-semibold text-slate-100">Something went wrong</h1>
            <p className="mt-2 text-sm text-muted-foreground">
              We hit an unexpected error. Please refresh the page.
            </p>
            <button
              onClick={() => this.setState({ hasError: false })}
              className="mt-4 rounded-lg border border-white/10 bg-white/[0.055] px-4 py-2 text-sm text-slate-300 transition hover:bg-white/[0.085]"
            >
              Try again
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
