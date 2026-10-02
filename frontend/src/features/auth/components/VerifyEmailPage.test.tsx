import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, fireEvent, waitFor } from "@testing-library/react";
import { render } from "../../../test/test-utils";
import { VerifyEmailPage } from "./VerifyEmailPage";
import * as authContext from "../../../lib/auth-context";

vi.mock("../../../lib/auth-context", async () => {
  const actual = await vi.importActual("../../../lib/auth-context");
  return {
    ...actual,
    useAuth: vi.fn()
  };
});

describe("VerifyEmailPage Component", () => {
  const mockVerifyEmail = vi.fn();
  const mockResendOtp = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    (authContext.useAuth as any).mockReturnValue({
      verifyEmail: mockVerifyEmail,
      resendOtp: mockResendOtp,
      currentUser: null,
      isLoading: false
    });
  });

  it("renders heading and input elements", () => {
    render(<VerifyEmailPage />, { initialEntries: ["/verify-email?email=user%40example.com"] });

    expect(screen.getByText(/Check your email/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Verify Email/i })).toBeInTheDocument();
  });

  it("calls verifyEmail when OTP inputs are filled and submit clicked", async () => {
    mockVerifyEmail.mockResolvedValueOnce(undefined);

    render(<VerifyEmailPage />, { initialEntries: ["/verify-email?email=user%40example.com"] });

    const inputs = screen.getAllByRole("textbox");
    // Enter 6 digits
    ["1", "2", "3", "4", "5", "6"].forEach((digit, i) => {
      fireEvent.change(inputs[i]!, { target: { value: digit } });
    });

    const verifyBtn = screen.getByRole("button", { name: /Verify Email/i });
    expect(verifyBtn).not.toBeDisabled();

    fireEvent.click(verifyBtn);

    await waitFor(() => {
      expect(mockVerifyEmail).toHaveBeenCalledWith("user@example.com", "123456");
    });
  });
});
