import { post } from "../lib/api-client";

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface RegisterResponse {
  email: string;
  verificationRequired: boolean;
  message: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken?: string;
  tokenType: string;
  expiresIn: number;
}

export interface AuthUserResponse {
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

export interface LoginResponse extends AuthTokens {
  user: AuthUserResponse;
}

export type RefreshResponse = LoginResponse;

export const authApi = {
  login: (credentials: LoginCredentials) =>
    post<LoginResponse>("/auth/login", credentials),
  register: (data: RegisterData) =>
    post<RegisterResponse>("/auth/register", data),
  verifyEmail: (email: string, otp: string) =>
    post<LoginResponse>("/auth/verify-email", { email, otp }),
  resendOtp: (email: string) =>
    post<string>("/auth/resend-verification-otp", { email }),
  refresh: () =>
    post<RefreshResponse>("/auth/refresh", {}),
  logout: () =>
    post<void>("/auth/logout")
};
