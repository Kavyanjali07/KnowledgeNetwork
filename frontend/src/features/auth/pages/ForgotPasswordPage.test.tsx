import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, fireEvent, waitFor } from "@testing-library/react";
import { render } from "../../../test/test-utils";
import { ForgotPasswordPage } from "./ForgotPasswordPage";
import * as authContext from "../../../lib/auth-context";

vi.mock("../../../lib/auth-context", async () => {
  const actual = await vi.importActual("../../../lib/auth-context");
  return {
    ...actual,
    useAuth: vi.fn()
  };
});

describe("ForgotPasswordPage Component", () => {
  const mockForgotPassword = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    (authContext.useAuth as any).mockReturnValue({
      forgotPassword: mockForgotPassword,
      currentUser: null,
      isLoading: false
    });
  });

  it("renders email input and submit button", () => {
    render(<ForgotPasswordPage />);

    expect(screen.getByLabelText(/Email Address/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Send Reset Code/i })).toBeInTheDocument();
  });

  it("calls forgotPassword on form submit", async () => {
    mockForgotPassword.mockResolvedValueOnce(undefined);

    render(<ForgotPasswordPage />);

    const emailInput = screen.getByLabelText(/Email Address/i);
    fireEvent.change(emailInput, { target: { value: "user@example.com" } });

    const submitBtn = screen.getByRole("button", { name: /Send Reset Code/i });
    expect(submitBtn).not.toBeDisabled();

    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(mockForgotPassword).toHaveBeenCalledWith("user@example.com");
    });

    expect(screen.getByText(/Enter Reset Code/i)).toBeInTheDocument();
  });
});
