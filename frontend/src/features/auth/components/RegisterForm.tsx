import { zodResolver } from "@hookform/resolvers/zod";
import { motion } from "framer-motion";
import { ArrowRight, Eye, Sparkles } from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { useToast } from "../../../components/ui/toast";
import { useAuth } from "../../../lib/auth-context";
import type { ApiError } from "../../../lib/api-client";
import { Button } from "../../../components/ui/button";
import { Card } from "../../../components/ui/card";
import { AuthField } from "./AuthField";
import { PasswordStrengthMeter } from "./PasswordStrengthMeter";
import { registerSchema, type RegisterFormValues } from "../schemas/auth.schemas";

export function RegisterForm() {
  const [showPassword, setShowPassword] = useState(false);
  const { addToast } = useToast();
  const { register: registerAccount } = useAuth();
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isValid, isSubmitting }
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    mode: "onChange",
    defaultValues: { name: "", email: "", password: "", confirmPassword: "", terms: false }
  });

  const password = watch("password");

  const onSubmit = async (values: RegisterFormValues) => {
    const nameParts = values.name.trim().split(/\s+/);
    const firstName = nameParts[0] || "";
    const lastName = nameParts.length > 1 ? nameParts.slice(1).join(" ") : "";

    try {
      await registerAccount(values.email, values.password, firstName, lastName);
      addToast({
        type: "success",
        title: "Account Created",
        description: "Please check your email for your 6-digit verification code."
      });
      navigate(`/verify-email?email=${encodeURIComponent(values.email)}`, { replace: true });
    } catch (error) {
      const apiError = error as ApiError;
      addToast({ type: "error", title: "Registration failed", description: apiError.message });
    }
  };

  return (
    <motion.div
      className="w-full max-w-md"
      initial={{ opacity: 0, y: 18, scale: 0.98 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
    >
      <Card className="floating-auth-card p-6 sm:p-7">
        <div className="mb-7">
          <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl border border-white/10 bg-white/[0.07] text-cyan-200">
            <Sparkles size={20} />
          </div>
          <h1 className="text-2xl font-semibold tracking-[-0.015em]">Create your workspace</h1>
          <p className="mt-2 text-sm leading-6 text-muted-foreground">
            Start with a secure account and invite your team when your graph is ready.
          </p>
        </div>

        <form
          className="space-y-4"
          onSubmit={handleSubmit(onSubmit)}
        >
          <AuthField
            label="Name"
            autoComplete="name"
            placeholder="Kavya Rao"
            error={errors.name?.message}
            {...register("name")}
          />
          <AuthField
            label="Email"
            type="email"
            autoComplete="email"
            placeholder="you@company.com"
            error={errors.email?.message}
            {...register("email")}
          />

          <div className="relative">
            <AuthField
              label="Password"
              type={showPassword ? "text" : "password"}
              autoComplete="new-password"
              placeholder="Create a password"
              error={errors.password?.message}
              {...register("password")}
            />
            <button
              className="absolute right-3 top-[38px] text-muted-foreground transition hover:text-foreground"
              type="button"
              onClick={() => setShowPassword((value) => !value)}
              aria-label="Toggle password visibility"
            >
              <Eye size={16} />
            </button>
          </div>

          <PasswordStrengthMeter password={password} />

          <AuthField
            label="Confirm password"
            type={showPassword ? "text" : "password"}
            autoComplete="new-password"
            placeholder="Confirm your password"
            error={errors.confirmPassword?.message}
            {...register("confirmPassword")}
          />

          <label className="flex items-start gap-3 text-sm text-muted-foreground">
            <input
              type="checkbox"
              className="mt-0.5 h-4 w-4 rounded border-white/10 bg-white/10 accent-cyan-300"
              {...register("terms")}
            />
            <span>
              I agree to the workspace terms.
              <span className="block min-h-4 text-xs text-rose-200">{errors.terms?.message}</span>
            </span>
          </label>

          <Button type="submit" className="w-full" disabled={!isValid || isSubmitting}>
            {isSubmitting ? "Creating secure workspace..." : "Create account"}
            <ArrowRight size={16} />
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-muted-foreground">
          Already have an account?{" "}
          <Link className="font-medium text-cyan-200 hover:text-cyan-100" to="/login">
            Sign in
          </Link>
        </p>
      </Card>
    </motion.div>
  );
}
