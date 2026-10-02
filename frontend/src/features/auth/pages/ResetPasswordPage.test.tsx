import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, fireEvent, waitFor } from "@testing-library/react";
import { render } from "../../../test/test-utils";
import { ResetPasswordPage } from "./ResetPasswordPage";
import * as authContext from "../../../lib/auth-context";

vi.mock("../../../lib/auth-context", async () => {
  const actual = await vi.importActual("../../../lib/auth-context");
  return {
    ...actual,
    useAuth: vi.fn()
  };
});

describe("ResetPasswordPage Component", () => {
  const mockResetPassword = vi.fn();
  const mockForgotPassword = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    (authContext.useAuth as any).mockReturnValue({
      resetPassword: mockResetPassword,
      forgotPassword: mockForgotPassword,
      currentUser: null,
      isLoading: false
    });
  });

  it("renders reset password form fields", () => {
    render(<ResetPasswordPage />);

    expect(screen.getByText(/Reset your password/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Minimum 8 characters/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Re-enter your new password/i)).toBeInTheDocument();
  });

  it("calls resetPassword with data when form is properly completed", async () => {
    mockResetPassword.mockResolvedValueOnce(undefined);

    render(<ResetPasswordPage />, { initialEntries: ["/reset-password?email=user%40example.com"] });

    const textboxes = screen.getAllByRole("textbox"); 
    const otpInputs = textboxes.slice(1); // skip email input field
    ["6", "5", "4", "3", "2", "1"].forEach((digit, i) => {
      fireEvent.change(otpInputs[i]!, { target: { value: digit } });
    });

    const newPassInput = screen.getByPlaceholderText(/Minimum 8 characters/i);
    const confirmPassInput = screen.getByPlaceholderText(/Re-enter your new password/i);

    fireEvent.change(newPassInput, { target: { value: "NewSecurePass123!" } });
    fireEvent.change(confirmPassInput, { target: { value: "NewSecurePass123!" } });

    const resetBtn = screen.getByRole("button", { name: /Reset Password/i });

    await waitFor(() => {
      expect(resetBtn).not.toBeDisabled();
    });

    fireEvent.click(resetBtn);

    await waitFor(() => {
      expect(mockResetPassword).toHaveBeenCalledWith({
        email: "user@example.com",
        code: "654321",
        newPassword: "NewSecurePass123!"
      });
    });
  });
});
