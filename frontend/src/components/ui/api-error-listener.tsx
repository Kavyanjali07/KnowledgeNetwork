import { useEffect } from "react";
import { useToast } from "./toast";
import type { ApiError } from "../../lib/api-client";

export function ApiErrorListener() {
  const { addToast } = useToast();

  useEffect(() => {
    function handleApiError(event: Event) {
      const customEvent = event as CustomEvent<ApiError>;
      const error = customEvent.detail;

      if (!error) return;

      let description = error.message;

      // If validation errors exist (e.g., status 422 or 400), format them concisely
      if (error.validationErrors && Object.keys(error.validationErrors).length > 0) {
        const fieldErrors = Object.entries(error.validationErrors)
          .map(([field, msg]) => `${field}: ${msg}`)
          .join(" | ");
        description = `${error.message} (${fieldErrors})`;
      }

      addToast({
        type: "error",
        title: error.title || "API Error",
        description,
      });
    }

    window.addEventListener("api-error", handleApiError);
    return () => {
      window.removeEventListener("api-error", handleApiError);
    };
  }, [addToast]);

  return null;
}
