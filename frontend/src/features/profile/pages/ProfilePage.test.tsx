import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import { render } from "../../../test/test-utils";
import { ProfilePage } from "./ProfilePage";
import { profileApi } from "../../../services";
import * as authContext from "../../../lib/auth-context";

vi.mock("../../../services", () => ({
  profileApi: {
    me: vi.fn(),
    getUser: vi.fn(),
    updateMe: vi.fn()
  },
  userApi: {
    isFollowing: vi.fn(),
    followUser: vi.fn(),
    unfollowUser: vi.fn()
  }
}));

vi.mock("../../../lib/auth-context", async () => {
  const actual = await vi.importActual("../../../lib/auth-context");
  return {
    ...actual,
    useAuth: vi.fn()
  };
});

describe("ProfilePage Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (authContext.useAuth as any).mockReturnValue({
      currentUser: { id: "user-123", email: "profile@example.com", firstName: "Profile", lastName: "Owner" },
      updateCurrentUser: vi.fn(),
      logout: vi.fn()
    });
  });

  it("renders profile details when loaded", async () => {
    (profileApi.me as any).mockResolvedValueOnce({
      data: {
        id: "user-123",
        email: "profile@example.com",
        firstName: "Profile",
        lastName: "Owner",
        bio: "Test bio",
        status: "online",
        emailVerified: true,
        graphs: [],
        statistics: {
          graphsOwned: 2,
          nodesCreated: 15,
          edgesAuthored: 8,
          posts: 4,
          followers: 10,
          following: 5
        }
      }
    });

    render(<ProfilePage />);

    await waitFor(() => {
      expect(screen.getByText(/Profile Owner/i)).toBeInTheDocument();
      expect(screen.getByText(/profile@example.com/i)).toBeInTheDocument();
      expect(screen.getByText(/Test bio/i)).toBeInTheDocument();
    });
  });
});
