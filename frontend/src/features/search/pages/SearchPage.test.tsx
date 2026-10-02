import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen } from "@testing-library/react";
import { render } from "../../../test/test-utils";
import { SearchPage } from "./SearchPage";
import * as advancedSearch from "../hooks/useAdvancedSearch";
import * as globalSearch from "../hooks/useGlobalSearch";

vi.mock("../hooks/useAdvancedSearch", () => ({
  useAdvancedSearch: vi.fn()
}));

vi.mock("../hooks/useGlobalSearch", () => ({
  useGlobalSearch: vi.fn()
}));

describe("SearchPage Component", () => {
  const mockOpenSearch = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    (globalSearch.useGlobalSearch as any).mockReturnValue({ openSearch: mockOpenSearch });
    (advancedSearch.useAdvancedSearch as any).mockReturnValue({ isSearchLoading: false });
  });

  it("renders heading and instructions", () => {
    render(<SearchPage />);

    expect(screen.getByText(/Knowledge search/i)).toBeInTheDocument();
    expect(screen.getByText(/⌘K/i)).toBeInTheDocument();
    expect(mockOpenSearch).toHaveBeenCalled();
  });
});
