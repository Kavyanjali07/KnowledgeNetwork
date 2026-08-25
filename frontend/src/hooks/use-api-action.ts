import { useState, useCallback } from "react";
import { useToast } from "../components/ui/toast";
import type { ApiError } from "../lib/api-client";

export interface UseApiActionOptions<T, V = void> {
  action: (variables: V) => Promise<T>;
  onSuccess?: (data: T, variables: V) => void;
  onError?: (error: ApiError, variables: V) => void;
  successTitle?: string;
  successMessage?: string;
  errorTitle?: string;
  showToastOnError?: boolean;
}

export interface UseApiActionResult<T, V = void> {
  execute: (variables: V) => Promise<T | null>;
  isLoading: boolean;
  isSuccess: boolean;
  isError: boolean;
  data: T | null;
  error: ApiError | null;
  reset: () => void;
}

/**
 * Standardized hook for managing API request lifecycle:
 * Loading -> Request -> Success (Update UI / optional toast) OR Failure (Show useful error)
 */
export function useApiAction<T, V = void>(
  options: UseApiActionOptions<T, V>
): UseApiActionResult<T, V> {
  const {
    action,
    onSuccess,
    onError,
    successTitle,
    successMessage,
    errorTitle,
    showToastOnError = true,
  } = options;

  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isSuccess, setIsSuccess] = useState<boolean>(false);
  const [isError, setIsError] = useState<boolean>(false);
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | null>(null);

  const { addToast } = useToast();

  const reset = useCallback(() => {
    setIsLoading(false);
    setIsSuccess(false);
    setIsError(false);
    setData(null);
    setError(null);
  }, []);

  const execute = useCallback(
    async (variables: V): Promise<T | null> => {
      setIsLoading(true);
      setIsSuccess(false);
      setIsError(false);
      setError(null);

      try {
        const result = await action(variables);
        setData(result);
        setIsSuccess(true);
        setIsLoading(false);

        if (successTitle || successMessage) {
          addToast({
            type: "success",
            title: successTitle || "Operation successful",
            description: successMessage,
          });
        }

        if (onSuccess) {
          onSuccess(result, variables);
        }

        return result;
      } catch (err: any) {
        const apiError: ApiError = err.title && err.code ? err : {
          title: errorTitle || "Request Failed",
          message: err.message || "An unexpected error occurred",
          status: err.status || 0,
          code: "UNKNOWN_ERROR"
        };

        setError(apiError);
        setIsError(true);
        setIsLoading(false);

        if (showToastOnError) {
          addToast({
            type: "error",
            title: apiError.title || errorTitle || "Operation Failed",
            description: apiError.message,
          });
        }

        if (onError) {
          onError(apiError, variables);
        }

        return null;
      }
    },
    [action, onSuccess, onError, successTitle, successMessage, errorTitle, showToastOnError, addToast]
  );

  return {
    execute,
    isLoading,
    isSuccess,
    isError,
    data,
    error,
    reset,
  };
}
