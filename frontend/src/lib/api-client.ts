import axios from "axios";

export interface ApiError {
  title: string;
  message: string;
  status: number;
  code: "BAD_REQUEST" | "UNAUTHORIZED" | "FORBIDDEN" | "NOT_FOUND" | "CONFLICT" | "UNPROCESSABLE_ENTITY" | "INTERNAL_SERVER_ERROR" | "NETWORK_ERROR" | "UNKNOWN_ERROR";
  validationErrors?: Record<string, string>;
  originalError?: unknown;
}

type RetriableRequestConfig = Parameters<typeof apiClient.request>[0] & {
  _authRetry?: boolean;
};

function normalizeApiBaseUrl(value?: string): string {
  const baseUrl = value?.trim() || "/api/v1";
  if (baseUrl === "/api/v1" || baseUrl.endsWith("/api/v1")) {
    return baseUrl;
  }
  return `${baseUrl.replace(/\/$/, "")}/api/v1`;
}

const apiClient = axios.create({
  baseURL: normalizeApiBaseUrl(import.meta.env.VITE_API_URL),
  withCredentials: true,
  headers: {
    "Content-Type": "application/json"
  },
  timeout: 30_000
});

let inMemoryAccessToken: string | null = null;
let refreshPromise: Promise<string> | null = null;

export function setAccessTokenInMemory(token: string | null) {
  inMemoryAccessToken = token;
}

export function getAccessTokenInMemory(): string | null {
  return inMemoryAccessToken;
}

export function clearStoredSession() {
  inMemoryAccessToken = null;
  localStorage.removeItem("auth_user");
  localStorage.removeItem("auth_token");
}

async function refreshAccessToken(): Promise<string> {
  const response = await apiClient.post<ApiResponse<{
    accessToken: string;
    user: unknown;
  }>>("/auth/refresh");

  const authResponse = response.data.data;
  setAccessTokenInMemory(authResponse.accessToken);
  if (authResponse.user) {
    localStorage.setItem("auth_user", JSON.stringify(authResponse.user));
  }
  return authResponse.accessToken;
}

function sanitizeErrorMessage(msg?: string): string {
  if (!msg) return "";
  if (msg.includes("Exception") || msg.includes("\tat ") || msg.includes("Trace:")) {
    return "An internal system error occurred. Details have been logged.";
  }
  return msg;
}

function parseApiError(error: any): ApiError {
  if (!error.response) {
    return {
      title: "Network Error",
      message: "Unable to connect to the server. Please check your network connection.",
      status: 0,
      code: "NETWORK_ERROR",
      originalError: error
    };
  }

  const status = error.response.status;
  const serverData = error.response.data || {};
  const rawDetail = serverData.detail || serverData.message;
  const detailMessage = sanitizeErrorMessage(rawDetail);

  switch (status) {
    case 400:
      return {
        title: serverData.title || "Bad Request (400)",
        message: detailMessage || "The request could not be understood or contained invalid parameters.",
        status,
        code: "BAD_REQUEST",
        validationErrors: serverData.validationErrors,
        originalError: error
      };
    case 401:
      return {
        title: serverData.title || "Unauthorized (401)",
        message: detailMessage || "Your session has expired or authentication credentials are missing.",
        status,
        code: "UNAUTHORIZED",
        originalError: error
      };
    case 403:
      return {
        title: serverData.title || "Access Denied (403)",
        message: detailMessage || "You do not have permission to access this resource.",
        status,
        code: "FORBIDDEN",
        originalError: error
      };
    case 404:
      return {
        title: serverData.title || "Resource Not Found (404)",
        message: detailMessage || "The requested resource could not be found.",
        status,
        code: "NOT_FOUND",
        originalError: error
      };
    case 409:
      return {
        title: serverData.title || "Conflict Detected (409)",
        message: detailMessage || "The request could not be completed due to a conflict with current resource state.",
        status,
        code: "CONFLICT",
        originalError: error
      };
    case 422:
      return {
        title: serverData.title || "Unprocessable Entity (422)",
        message: detailMessage || "The submitted data is invalid or failed business validation rules.",
        status,
        code: "UNPROCESSABLE_ENTITY",
        validationErrors: serverData.validationErrors,
        originalError: error
      };
    case 500:
      return {
        title: serverData.title || "Internal Server Error (500)",
        message: "An unexpected error occurred on the server. Please try again later.",
        status,
        code: "INTERNAL_SERVER_ERROR",
        originalError: error
      };
    default:
      return {
        title: serverData.title || `Request Error (${status})`,
        message: detailMessage || "An unexpected error occurred while processing your request.",
        status,
        code: "UNKNOWN_ERROR",
        originalError: error
      };
  }
}

apiClient.interceptors.request.use((config) => {
  const token = getAccessTokenInMemory();
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response && error.response.status === 401) {
      const requestConfig = error.config as RetriableRequestConfig | undefined;
      const isRefreshRequest = requestConfig?.url?.includes("/auth/refresh");

      if (requestConfig && !requestConfig._authRetry && !isRefreshRequest) {
        requestConfig._authRetry = true;
        refreshPromise ??= refreshAccessToken().finally(() => {
          refreshPromise = null;
        });

        try {
          const accessToken = await refreshPromise;
          requestConfig.headers ??= {};
          requestConfig.headers.Authorization = `Bearer ${accessToken}`;
          return apiClient.request(requestConfig);
        } catch {
          clearStoredSession();
        }
      } else {
        clearStoredSession();
      }
    }

    const apiError = parseApiError(error);

    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("api-error", { detail: apiError }));
    }

    return Promise.reject(apiError);
  }
);

export interface ApiResponse<T = unknown> {
  data: T;
  message?: string;
  status: number;
}

export async function get<T>(url: string, config?: Record<string, unknown>): Promise<{ data: T }> {
  const response = await apiClient.get<ApiResponse<T>>(url, config as any);
  const apiResponse = response.data as ApiResponse<T>;
  return { data: apiResponse.data };
}

export async function post<T>(url: string, data?: unknown, config?: Record<string, unknown>): Promise<{ data: T }> {
  const response = await apiClient.post<ApiResponse<T>>(url, data, config as any);
  const apiResponse = response.data as ApiResponse<T>;
  return { data: apiResponse.data };
}

export async function put<T>(url: string, data?: unknown, config?: Record<string, unknown>): Promise<{ data: T }> {
  const response = await apiClient.put<ApiResponse<T>>(url, data, config as any);
  const apiResponse = response.data as ApiResponse<T>;
  return { data: apiResponse.data };
}

export async function patch<T>(url: string, data?: unknown, config?: Record<string, unknown>): Promise<{ data: T }> {
  const response = await apiClient.patch<ApiResponse<T>>(url, data, config as any);
  const apiResponse = response.data as ApiResponse<T>;
  return { data: apiResponse.data };
}

export async function del<T>(url: string, config?: Record<string, unknown>): Promise<{ data: T }> {
  const response = await apiClient.delete<ApiResponse<T>>(url, config as any);
  const apiResponse = response.data as ApiResponse<T>;
  return { data: apiResponse.data };
}

export default apiClient;
