import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, fireEvent } from "@testing-library/react";
import { render } from "../../../test/test-utils";
import { GraphManagementPage } from "./GraphManagementPage";
import * as useQueries from "../../../hooks/use-queries";

vi.mock("../../../hooks/use-queries", () => ({
  useGraphs: vi.fn()
}));

vi.mock("../../../hooks/use-mutations", () => ({
  useCreateGraph: () => ({ mutateAsync: vi.fn(), isPending: false }),
  useUpdateGraph: () => ({ mutateAsync: vi.fn(), isPending: false }),
  useDeleteGraph: () => ({ mutateAsync: vi.fn(), isPending: false })
}));

describe("GraphManagementPage Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders loading state", () => {
    (useQueries.useGraphs as any).mockReturnValue({
      data: null,
      isLoading: true,
      isError: false,
      refetch: vi.fn()
    });

    const { container } = render(<GraphManagementPage />);
    expect(container.querySelector("section")).toBeInTheDocument();
  });

  it("renders graph library when loaded", () => {
    (useQueries.useGraphs as any).mockReturnValue({
      data: {
        content: [
          {
            id: "1",
            title: "AI Knowledge Architecture",
            name: "AI Knowledge Architecture",
            description: "Graph of core neural components",
            visibility: "PUBLIC",
            version: 1,
            createdAt: "2026-09-01T00:00:00Z",
            updatedAt: "2026-09-01T00:00:00Z",
            createdBy: "1",
            updatedBy: "1",
            deleted: false
          }
        ]
      },
      isLoading: false,
      isError: false,
      refetch: vi.fn()
    });

    render(<GraphManagementPage />);

    expect(screen.getByText(/Knowledge graph library/i)).toBeInTheDocument();
    expect(screen.getByText(/AI Knowledge Architecture/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Create Network/i })).toBeInTheDocument();
  });

  it("opens create modal on button click", () => {
    (useQueries.useGraphs as any).mockReturnValue({
      data: { content: [] },
      isLoading: false,
      isError: false,
      refetch: vi.fn()
    });

    render(<GraphManagementPage />);

    const newBtn = screen.getByRole("button", { name: /Create Network/i });
    fireEvent.click(newBtn);

    expect(screen.getByText(/Create Knowledge Network/i)).toBeInTheDocument();
  });
});
