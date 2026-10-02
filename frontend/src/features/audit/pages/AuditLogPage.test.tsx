import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import { render } from "../../../test/test-utils";
import { AuditLogPage } from "./AuditLogPage";
import { auditLogApi } from "../../../services/auditLogApi";
import { workspaceApi } from "../../../services/graphApi";

vi.mock("../../../services/auditLogApi", () => ({
  auditLogApi: {
    getLogs: vi.fn()
  }
}));

vi.mock("../../../services/graphApi", () => ({
  workspaceApi: {
    list: vi.fn()
  }
}));

describe("AuditLogPage Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (workspaceApi.list as any).mockResolvedValue({ data: [] });
  });

  it("renders header and audit table", async () => {
    (auditLogApi.getLogs as any).mockResolvedValueOnce({
      content: [
        {
          id: "log-1",
          workspaceId: "ws-1",
          actorId: "user-1",
          actorName: "Test Actor",
          actorEmail: "actor@example.com",
          action: "WORKSPACE_CREATED",
          entityType: "WORKSPACE",
          entityId: "ws-1",
          timestamp: "2026-09-01T12:00:00Z"
        }
      ],
      totalPages: 1,
      totalElements: 1,
      number: 0,
      size: 20
    });

    render(<AuditLogPage />);

    expect(screen.getByText(/Production Audit Log/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/WORKSPACE_CREATED/i)).toBeInTheDocument();
      expect(screen.getByText(/Test Actor/i)).toBeInTheDocument();
    });
  });
});
