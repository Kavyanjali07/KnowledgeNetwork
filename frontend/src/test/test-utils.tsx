import React, { ReactElement } from "react";
import { render, RenderOptions } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { ToastProvider } from "../components/ui/toast";

export function createTestQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0
      },
      mutations: {
        retry: false
      }
    }
  });
}

interface AllTheProvidersProps {
  children: React.ReactNode;
  initialEntries?: string[];
}

export function AllTheProviders({ children, initialEntries = ["/"] }: AllTheProvidersProps) {
  const queryClient = createTestQueryClient();

  return (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={initialEntries}>
        <ToastProvider>{children}</ToastProvider>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

function customRender(
  ui: ReactElement,
  options?: Omit<RenderOptions, "wrapper"> & { initialEntries?: string[] }
) {
  const { initialEntries, ...renderOptions } = options || {};
  return render(ui, {
    wrapper: (props) => <AllTheProviders {...props} initialEntries={initialEntries} />,
    ...renderOptions
  });
}

export * from "@testing-library/react";
export { customRender as render };
