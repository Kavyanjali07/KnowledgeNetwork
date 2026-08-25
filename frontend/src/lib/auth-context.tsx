import React, { createContext, useContext, useEffect, useState, useCallback } from "react";
import { useToast } from "../components/ui/toast";
import { setAccessTokenInMemory, clearStoredSession } from "./api-client";
import { authApi, RegisterResponse } from "../services/authApi";

export interface AuthUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  bio?: string;
  status?: string;
  role: string;
  emailVerified: boolean;
  createdAt: string;
}

interface AuthContextValue {
  currentUser: AuthUser | null;
  token: string | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, firstName: string, lastName: string) => Promise<RegisterResponse>;
  verifyEmail: (email: string, otp: string) => Promise<void>;
  resendOtp: (email: string) => Promise<void>;
  logout: () => Promise<void>;
  refresh: () => Promise<void>;
  updateCurrentUser: (user: AuthUser) => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function loadUserFromStorage(): AuthUser | null {
  try {
    const userJson = localStorage.getItem("auth_user");
    return userJson ? JSON.parse(userJson) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const { addToast } = useToast();
  const [currentUser, setCurrentUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      const { data } = await authApi.refresh();
      const newToken = data.accessToken;
      setAccessTokenInMemory(newToken);
      setToken(newToken);
      setCurrentUser(data.user);
      localStorage.setItem("auth_user", JSON.stringify(data.user));
    } catch {
      clearStoredSession();
      setAccessTokenInMemory(null);
      setToken(null);
      setCurrentUser(null);
    }
  }, []);

  useEffect(() => {
    // Attempt silent refresh via HttpOnly cookie on mount
    refresh().finally(() => {
      const cachedUser = loadUserFromStorage();
      if (cachedUser) {
        setCurrentUser(cachedUser);
      }
      setIsLoading(false);
    });
  }, [refresh]);

  const login = useCallback(
    async (email: string, password: string) => {
      const { data } = await authApi.login({
        email,
        password
      });
      setAccessTokenInMemory(data.accessToken);
      setToken(data.accessToken);
      setCurrentUser(data.user);
      localStorage.setItem("auth_user", JSON.stringify(data.user));
    },
    []
  );

  const register = useCallback(
    async (email: string, password: string, firstName: string, lastName: string) => {
      const { data } = await authApi.register({
        email,
        password,
        firstName,
        lastName
      });
      return data;
    },
    []
  );

  const verifyEmail = useCallback(
    async (email: string, otp: string) => {
      const { data } = await authApi.verifyEmail(email, otp);
      setAccessTokenInMemory(data.accessToken);
      setToken(data.accessToken);
      setCurrentUser(data.user);
      localStorage.setItem("auth_user", JSON.stringify(data.user));
    },
    []
  );

  const resendOtp = useCallback(
    async (email: string) => {
      await authApi.resendOtp(email);
    },
    []
  );

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      addToast({ type: "info", title: "Signed out locally", description: "The server session could not be reached." });
    }
    clearStoredSession();
    setAccessTokenInMemory(null);
    setToken(null);
    setCurrentUser(null);
    window.location.href = "/";
  }, [addToast]);

  const updateCurrentUser = useCallback((user: AuthUser) => {
    localStorage.setItem("auth_user", JSON.stringify(user));
    setCurrentUser(user);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        currentUser,
        token,
        isLoading,
        login,
        register,
        verifyEmail,
        resendOtp,
        logout,
        refresh,
        updateCurrentUser
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}

export { AuthContext };
