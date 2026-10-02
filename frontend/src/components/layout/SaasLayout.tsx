import { SearchProvider } from "../../features/search/hooks/useGlobalSearch";
import { SearchModal } from "../../features/search/components/SearchModal";
import { CommandPalette } from "../navigation/CommandPalette";
import { FloatingNavbar } from "../navigation/FloatingNavbar";
import { Sidebar } from "../navigation/Sidebar";
import { BottomNav } from "../navigation/BottomNav";
import { DashboardPreview } from "./DashboardPreview";
import { OfflineIndicator } from "../ui/offline-indicator";
import { FloatingActionButton } from "../ui/floating-action-button";
import { motion } from "framer-motion";

import { KnowledgeTrailBar } from "../navigation/KnowledgeTrailBar";

type SaasLayoutProps = {
  mainContent?: React.ReactNode;
};

export function SaasLayout({ mainContent }: SaasLayoutProps) {
  return (
    <SearchProvider>
      <div className="min-h-screen overflow-hidden bg-background text-foreground">
        <OfflineIndicator />
        <div className="pointer-events-none fixed inset-0 bg-[radial-gradient(circle_at_22%_12%,rgba(124,58,237,0.16),transparent_32%),radial-gradient(circle_at_80%_4%,rgba(34,211,238,0.12),transparent_28%)]" />
        <div className="relative flex min-h-screen">
          <Sidebar />
          <motion.main
            className="min-w-0 flex-1 px-4 pb-24 pt-4 md:px-6 lg:pl-4 md:pb-8"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
          >
            <KnowledgeTrailBar />
            <FloatingNavbar />
            {mainContent ?? <DashboardPreview />}
          </motion.main>
        </div>

        <BottomNav />
        <FloatingActionButton className="fixed bottom-20 right-4 z-30 md:bottom-6 md:right-6" aria-label="Create node" />
        <CommandPalette />
      </div>
      <SearchModal />
    </SearchProvider>
  );
}
