import { z } from "zod";

export const loginSchema = z.object({
  email: z.string().trim().email("Enter a valid email address."),
  password: z.string().min(1, "Password is required.")
});

export const registerSchema = z
  .object({
    name: z
      .string()
      .trim()
      .min(2, "Name must be at least 2 characters."),
    email: z.string().trim().email("Enter a valid email address."),
    password: z
      .string()
      .min(8, "Use at least 8 characters.")
      .regex(/[A-Z]/, "Add one uppercase letter.")
      .regex(/[a-z]/, "Add one lowercase letter.")
      .regex(/[0-9]/, "Add one number."),
    confirmPassword: z.string(),
    terms: z.boolean().refine((value) => value, "Accept the workspace terms to continue.")
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords do not match.",
    path: ["confirmPassword"]
  });

export type LoginFormValues = z.infer<typeof loginSchema>;
export type RegisterFormValues = z.input<typeof registerSchema>;

export const verifyOtpSchema = z.object({
  email: z.string().trim().email("Enter a valid email address."),
  otp: z
    .string()
    .length(6, "Verification code must be 6 digits.")
    .regex(/^\d{6}$/, "Code must contain numbers only.")
});

export type VerifyOtpFormValues = z.infer<typeof verifyOtpSchema>;
