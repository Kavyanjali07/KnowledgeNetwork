import { describe, it, expect, vi } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import { render } from "../../../test/test-utils";
import { ActivityPage } from "./ActivityPage";

vi.mock("../components/SocialFeed", () => ({
  SocialFeed: () => <div data-testid="social-feed">Social Feed Component</div>
}));

describe("ActivityPage Component", () => {
  it("renders activity feed after loading timer", async () => {
    render(<ActivityPage />);

    expect(screen.getByText(/Workspace feed/i)).toBeInTheDocument();

    await waitFor(
      () => {
        expect(screen.getByTestId("social-feed")).toBeInTheDocument();
      },
      { timeout: 1000 }
    );
  });
});
