import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, act, waitFor } from "@testing-library/react";
import { AuthProvider, useAuth } from "./auth-context";
import { authApi } from "../services/authApi";
import React from "react";
import { ToastProvider } from "../components/ui/toast";

vi.mock("../services/authApi", () => ({
  authApi: {
    refresh: vi.fn(),
    login: vi.fn(),
    logout: vi.fn(),
    register: vi.fn(),
    verifyEmail: vi.fn(),
    resendOtp: vi.fn(),
    forgotPassword: vi.fn(),
    resetPassword: vi.fn()
  }
}));

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <ToastProvider>
    <AuthProvider>{children}</AuthProvider>
  </ToastProvider>
);

describe("AuthContext", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it("throws error when useAuth is used outside AuthProvider", () => {
    expect(() => renderHook(() => useAuth())).toThrow("useAuth must be used inside AuthProvider");
  });

  it("initializes with unauthenticated user when silent refresh fails", async () => {
    (authApi.refresh as any).mockRejectedValueOnce(new Error("No session"));

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.currentUser).toBeNull();
    expect(result.current.token).toBeNull();
  });

  it("sets user and token on successful login", async () => {
    (authApi.refresh as any).mockRejectedValueOnce(new Error("No session"));
    const mockUser = { id: "1", email: "test@example.com", firstName: "Test", lastName: "User", role: "USER", emailVerified: true, createdAt: "" };
    (authApi.login as any).mockResolvedValueOnce({
      data: { accessToken: "jwt-token-123", user: mockUser }
    });

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    await act(async () => {
      await result.current.login("test@example.com", "Password123!");
    });

    expect(result.current.token).toBe("jwt-token-123");
    expect(result.current.currentUser).toEqual(mockUser);
  });
});
