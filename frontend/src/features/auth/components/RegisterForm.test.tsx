import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, fireEvent, waitFor } from "@testing-library/react";
import { render } from "../../../test/test-utils";
import { RegisterForm } from "./RegisterForm";
import * as authContext from "../../../lib/auth-context";

vi.mock("../../../lib/auth-context", async () => {
  const actual = await vi.importActual("../../../lib/auth-context");
  return {
    ...actual,
    useAuth: vi.fn()
  };
});

describe("RegisterForm Component", () => {
  const mockRegister = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    (authContext.useAuth as any).mockReturnValue({
      register: mockRegister,
      currentUser: null,
      isLoading: false
    });
  });

  it("renders form fields and submit button", () => {
    render(<RegisterForm />);

    expect(screen.getByLabelText(/Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Password/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Confirm password/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Create account/i })).toBeInTheDocument();
  });

  it("renders link to login page", () => {
    render(<RegisterForm />);

    const loginLink = screen.getByRole("link", { name: /Sign in/i });
    expect(loginLink).toBeInTheDocument();
    expect(loginLink).toHaveAttribute("href", "/login");
  });

  it("submits registration with parsed first and last names", async () => {
    mockRegister.mockResolvedValueOnce({ message: "Success" });

    render(<RegisterForm />);

    fireEvent.change(screen.getByLabelText(/Name/i), { target: { value: "Kavya Rao" } });
    fireEvent.change(screen.getByLabelText(/^Email/i), { target: { value: "kavya@example.com" } });
    fireEvent.change(screen.getByLabelText(/^Password/i), { target: { value: "Password123!" } });
    fireEvent.change(screen.getByLabelText(/Confirm password/i), { target: { value: "Password123!" } });
    fireEvent.click(screen.getByRole("checkbox"));

    const submitBtn = screen.getByRole("button", { name: /Create account/i });

    await waitFor(() => {
      expect(submitBtn).not.toBeDisabled();
    });

    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith(
        "kavya@example.com",
        "Password123!",
        "Kavya",
        "Rao"
      );
    });
  });
});
