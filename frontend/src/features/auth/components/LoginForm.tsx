import { zodResolver } from "@hookform/resolvers/zod";
import { motion } from "framer-motion";
import { ArrowRight, Eye, LockKeyhole } from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { useToast } from "../../../components/ui/toast";
import { useAuth } from "../../../lib/auth-context";
import type { ApiError } from "../../../lib/api-client";
import { Button } from "../../../components/ui/button";
import { Card } from "../../../components/ui/card";
import { AuthField } from "./AuthField";
import { loginSchema, type LoginFormValues } from "../schemas/auth.schemas";

export function LoginForm() {
  const [showPassword, setShowPassword] = useState(false);
  const { addToast } = useToast();
  const { login } = useAuth();
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors, isValid, isSubmitting }
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    mode: "onChange",
    defaultValues: { email: "", password: "" }
  });

  const [unverifiedEmail, setUnverifiedEmail] = useState<string | null>(null);

  const onSubmit = async (values: LoginFormValues) => {
    setUnverifiedEmail(null);
    try {
      await login(values.email, values.password);
      addToast({ type: "success", title: "Signed in", description: "Welcome back to KnowledgeNetwork." });
      navigate("/dashboard", { replace: true });
    } catch (error) {
      const apiError = error as ApiError;
      if (apiError.message?.includes("EMAIL_NOT_VERIFIED") || apiError.code === "FORBIDDEN") {
        setUnverifiedEmail(values.email);
        addToast({
          type: "info",
          title: "Email Verification Required",
          description: "Please verify your email before signing in."
        });
        navigate(`/verify-email?email=${encodeURIComponent(values.email)}`);
      } else {
        addToast({ type: "error", title: "Login failed", description: apiError.message });
      }
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
            <LockKeyhole size={20} />
          </div>
          <h1 className="text-2xl font-semibold tracking-[-0.015em]">Welcome back</h1>
          <p className="mt-2 text-sm leading-6 text-muted-foreground">
            Sign in to continue mapping your team's knowledge graph.
          </p>
        </div>

        {unverifiedEmail && (
          <div className="mb-6 rounded-xl border border-amber-500/30 bg-amber-500/10 p-4 text-sm text-amber-200">
            <p className="font-medium">Please verify your email before signing in.</p>
            <Link
              to={`/verify-email?email=${encodeURIComponent(unverifiedEmail)}`}
              className="mt-2 inline-flex items-center gap-1 font-semibold text-cyan-300 hover:underline"
            >
              Verify Email <ArrowRight size={14} />
            </Link>
          </div>
        )}

        <form
          className="space-y-4"
          onSubmit={handleSubmit(onSubmit)}
        >
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
              autoComplete="current-password"
              placeholder="Enter your password"
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

          <div className="flex items-center justify-between text-sm">
            <label className="flex items-center gap-2 text-muted-foreground">
              <input type="checkbox" className="h-4 w-4 rounded border-white/10 bg-white/10 accent-cyan-300" />
              Remember me
            </label>
          </div>

          <Button type="submit" className="w-full" disabled={!isValid || isSubmitting}>
            {isSubmitting ? "Checking workspace..." : "Sign in"}
            <ArrowRight size={16} />
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-muted-foreground">
          New to KnowledgeNetwork?{" "}
          <Link className="font-medium text-cyan-200 hover:text-cyan-100" to="/register">
            Create account
          </Link>
        </p>
      </Card>
    </motion.div>
  );
}
