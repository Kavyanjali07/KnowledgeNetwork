import { lazy, Suspense } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { ErrorBoundary } from "./components/ui/error-page";
import { SaasLayout } from "./components/layout/SaasLayout";
import { LoginPage } from "./features/auth/pages/LoginPage";
import { RegisterPage } from "./features/auth/pages/RegisterPage";
import { VerifyEmailPage } from "./features/auth/components/VerifyEmailPage";
import { AuthProvider, useAuth } from "./lib/auth-context";

function PageSkeleton() {
  return (
    <div className="flex min-h-[60vh] items-center justify-center">
      <div className="h-8 w-8 animate-spin rounded-full border-2 border-cyan-300 border-t-transparent" />
    </div>
  );
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { token, isLoading } = useAuth();
  if (isLoading) {
    return <PageSkeleton />;
  }
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

function PublicRoute({ children }: { children: React.ReactNode }) {
  const { token, isLoading } = useAuth();
  if (isLoading) {
    return <PageSkeleton />;
  }
  if (token) {
    return <Navigate to="/dashboard" replace />;
  }
  return <>{children}</>;
}

function renderLazy(element: React.ReactNode) {
  return (
    <ErrorBoundary>
      <Suspense fallback={<PageSkeleton />}>
        <SaasLayout mainContent={element} />
      </Suspense>
    </ErrorBoundary>
  );
}

const GraphEditorPage = lazy(() => import("./features/graph/pages/GraphEditorPage").then(m => ({ default: m.GraphEditorPage })));
const GraphManagementPage = lazy(() => import("./features/graphs/pages/GraphManagementPage").then(m => ({ default: m.GraphManagementPage })));
const ActivityPage = lazy(() => import("./features/social/pages/ActivityPage").then(m => ({ default: m.ActivityPage })));
const SearchPage = lazy(() => import("./features/search/pages/SearchPage").then(m => ({ default: m.SearchPage })));
const VersionHistoryPage = lazy(() => import("./features/versions/pages/VersionHistoryPage").then(m => ({ default: m.VersionHistoryPage })));
const ProfilePage = lazy(() => import("./features/profile/pages/ProfilePage").then(m => ({ default: m.ProfilePage })));
const SettingsPage = lazy(() => import("./features/settings/pages/SettingsPage").then(m => ({ default: m.SettingsPage })));
const NotificationHistoryPage = lazy(() => import("./features/notifications/pages/NotificationHistoryPage").then(m => ({ default: m.NotificationHistoryPage })));

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
      <Route path="/register" element={<PublicRoute><RegisterPage /></PublicRoute>} />
      <Route path="/verify-email" element={<PublicRoute><VerifyEmailPage /></PublicRoute>} />

      {/* Account protected routes */}
      <Route path="/dashboard" element={<ProtectedRoute>{renderLazy(<GraphManagementPage />)}</ProtectedRoute>} />
      <Route path="/graphs" element={<ProtectedRoute>{renderLazy(<GraphManagementPage />)}</ProtectedRoute>} />
      <Route path="/graphs/:id" element={<ProtectedRoute>{renderLazy(<GraphEditorPage />)}</ProtectedRoute>} />
      <Route path="/graphs/:id/edit" element={<ProtectedRoute>{renderLazy(<GraphEditorPage />)}</ProtectedRoute>} />
      <Route path="/graph" element={<ProtectedRoute>{renderLazy(<GraphEditorPage />)}</ProtectedRoute>} />
      <Route path="/graph/:id" element={<ProtectedRoute>{renderLazy(<GraphEditorPage />)}</ProtectedRoute>} />
      <Route path="/graph/:id/edit" element={<ProtectedRoute>{renderLazy(<GraphEditorPage />)}</ProtectedRoute>} />
      <Route path="/activity" element={<ProtectedRoute>{renderLazy(<ActivityPage />)}</ProtectedRoute>} />
      <Route path="/search" element={<ProtectedRoute>{renderLazy(<SearchPage />)}</ProtectedRoute>} />
      <Route path="/history" element={<ProtectedRoute>{renderLazy(<VersionHistoryPage />)}</ProtectedRoute>} />
      <Route path="/profile" element={<ProtectedRoute>{renderLazy(<ProfilePage />)}</ProtectedRoute>} />
      <Route path="/settings" element={<ProtectedRoute>{renderLazy(<SettingsPage />)}</ProtectedRoute>} />
      <Route path="/notifications" element={<ProtectedRoute>{renderLazy(<NotificationHistoryPage />)}</ProtectedRoute>} />

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}

