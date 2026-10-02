import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import { render } from "../../../test/test-utils";
import { NotificationHistoryPage } from "./NotificationHistoryPage";
import { notificationApi } from "../../../services";

vi.mock("../../../services", () => ({
  notificationApi: {
    list: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn()
  }
}));

describe("NotificationHistoryPage Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders notification inbox header and items when loaded", async () => {
    (notificationApi.list as any).mockResolvedValueOnce({
      data: {
        content: [
          {
            id: "notif-1",
            type: "LIKE",
            message: "Alice liked your graph",
            isRead: false,
            createdAt: new Date().toISOString(),
            workspace: { id: "ws-1", name: "AI Graph" }
          }
        ],
        pageNumber: 0,
        totalPages: 1
      }
    });

    render(<NotificationHistoryPage />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: /Notifications/i })).toBeInTheDocument();
      expect(screen.getByRole("button", { name: /Mark all read/i })).toBeInTheDocument();
    });
  });
});
