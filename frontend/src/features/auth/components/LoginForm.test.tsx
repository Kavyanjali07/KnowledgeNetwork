import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, fireEvent, waitFor } from "@testing-library/react";
import { render } from "../../../test/test-utils";
import { LoginForm } from "./LoginForm";
import * as authContext from "../../../lib/auth-context";

vi.mock("../../../lib/auth-context", async () => {
  const actual = await vi.importActual("../../../lib/auth-context");
  return {
    ...actual,
    useAuth: vi.fn()
  };
});

describe("LoginForm Component", () => {
  const mockLogin = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    (authContext.useAuth as any).mockReturnValue({
      login: mockLogin,
      currentUser: null,
      isLoading: false
    });
  });

  it("renders email and password inputs", () => {
    render(<LoginForm />);

    expect(screen.getByLabelText(/^Email$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Password$/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Sign in/i })).toBeInTheDocument();
  });

  it("renders link to registration page", () => {
    render(<LoginForm />);

    const registerLink = screen.getByRole("link", { name: /Create account/i });
    expect(registerLink).toBeInTheDocument();
    expect(registerLink).toHaveAttribute("href", "/register");
  });

  it("renders link to forgot password page", () => {
    render(<LoginForm />);

    const forgotLink = screen.getByRole("link", { name: /Forgot password\?/i });
    expect(forgotLink).toBeInTheDocument();
    expect(forgotLink).toHaveAttribute("href", "/forgot-password");
  });

  it("calls login function on valid form submission", async () => {
    mockLogin.mockResolvedValueOnce(undefined);

    render(<LoginForm />);

    const emailInput = screen.getByLabelText(/^Email$/i);
    const passwordInput = screen.getByLabelText(/^Password$/i);
    const submitBtn = screen.getByRole("button", { name: /Sign in/i });

    fireEvent.change(emailInput, { target: { value: "user@example.com" } });
    fireEvent.change(passwordInput, { target: { value: "Password123!" } });

    await waitFor(() => {
      expect(submitBtn).not.toBeDisabled();
    });

    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith("user@example.com", "Password123!");
    });
  });
});
