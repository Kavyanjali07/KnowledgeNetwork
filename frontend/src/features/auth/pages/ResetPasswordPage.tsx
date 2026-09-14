import React, { useState, useEffect } from "react";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import { useAuth } from "../../../lib/auth-context";
import { useToast } from "../../../components/ui/toast";
import { OtpInput } from "../components/OtpInput";
import { PasswordStrengthMeter } from "../components/PasswordStrengthMeter";
import { Mail, ArrowLeft, RefreshCw, KeyRound, CheckCircle, Eye, EyeOff } from "lucide-react";
import type { ApiError } from "../../../lib/api-client";

export const ResetPasswordPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { resetPassword, forgotPassword } = useAuth();
  const { addToast } = useToast();

  const [email, setEmail] = useState(searchParams.get("email") || "");
  const [code, setCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [cooldown, setCooldown] = useState(60);

  useEffect(() => {
    if (cooldown <= 0) return;
    const timer = setInterval(() => {
      setCooldown((prev) => prev - 1);
    }, 1000);
    return () => clearInterval(timer);
  }, [cooldown]);

  const handleReset = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !email.includes("@")) {
      setErrorMsg("Please enter a valid email address.");
      return;
    }
    if (code.length !== 6) {
      setErrorMsg("Please enter the complete 6-digit reset code.");
      return;
    }
    if (newPassword.length < 8) {
      setErrorMsg("New password must be at least 8 characters long.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setErrorMsg("Passwords do not match. Please verify your entries.");
      return;
    }

    setIsSubmitting(true);
    setErrorMsg(null);

    try {
      await resetPassword({
        email,
        code,
        newPassword
      });
      addToast({
        type: "success",
        title: "Password Reset Successful",
        description: "Your password has been reset successfully. You can now log in with your new password."
      });
      navigate("/login", { replace: true });
    } catch (err) {
      const apiErr = err as ApiError;
      let message = apiErr.message || "Failed to reset password. Please check your reset code.";
      if (message.toLowerCase().includes("expired")) {
        message = "This reset code has expired. Please request a new one.";
      } else if (message.toLowerCase().includes("invalid")) {
        message = "The reset code is invalid. Please check the code and try again.";
      }
      setErrorMsg(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResend = async () => {
    if (cooldown > 0 || isResending) return;
    if (!email || !email.includes("@")) {
      setErrorMsg("Please enter a valid email address to resend code.");
      return;
    }

    setIsResending(true);
    setErrorMsg(null);

    try {
      await forgotPassword(email);
      addToast({
        type: "info",
        title: "Code Sent",
        description: "A new 6-digit password reset code has been sent to your email."
      });
      setCooldown(60);
    } catch (err) {
      const apiErr = err as ApiError;
      setErrorMsg(apiErr.message || "Failed to resend reset code.");
    } finally {
      setIsResending(false);
    }
  };

  const isFormValid =
    email &&
    email.includes("@") &&
    code.length === 6 &&
    newPassword.length >= 8 &&
    newPassword === confirmPassword;

  return (
    <div className="min-h-screen bg-[#080c14] text-slate-100 flex flex-col justify-center items-center p-4 relative overflow-hidden">
      {/* Background glow effects */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-cyan-500/10 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-10 right-10 w-[300px] h-[300px] bg-purple-500/10 rounded-full blur-[100px] pointer-events-none" />

      <div className="w-full max-w-md bg-slate-900/80 backdrop-blur-xl border border-slate-800 rounded-2xl p-8 shadow-2xl z-10 relative">
        <div className="flex flex-col items-center text-center mb-6">
          <div className="w-14 h-14 rounded-2xl bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center mb-4 text-cyan-400">
            <KeyRound className="w-7 h-7" />
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-white mb-2">Reset your password</h1>
          <p className="text-sm text-slate-400 leading-relaxed max-w-xs">
            Enter the 6-digit code sent to your email along with your new password.
          </p>
        </div>

        <form onSubmit={handleReset} className="space-y-5">
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">Email Address</label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@company.com"
                className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-4 py-2.5 text-sm text-white focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-center text-xs font-medium text-slate-400 mb-3">
              Reset Code (6 digits)
            </label>
            <OtpInput
              value={code}
              onChange={(val) => {
                setCode(val);
                if (errorMsg) setErrorMsg(null);
              }}
              disabled={isSubmitting}
              hasError={Boolean(errorMsg && errorMsg.toLowerCase().includes("code"))}
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">New Password</label>
            <div className="relative">
              <input
                type={showNewPassword ? "text" : "password"}
                required
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="Minimum 8 characters"
                className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-3 pr-10 py-2.5 text-sm text-white focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500"
              />
              <button
                type="button"
                onClick={() => setShowNewPassword((prev) => !prev)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white"
                aria-label="Toggle password visibility"
              >
                {showNewPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            {newPassword && <PasswordStrengthMeter password={newPassword} />}
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">Confirm New Password</label>
            <div className="relative">
              <input
                type={showConfirmPassword ? "text" : "password"}
                required
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Re-enter your new password"
                className={`w-full bg-slate-950 border rounded-lg pl-3 pr-10 py-2.5 text-sm text-white focus:outline-none focus:ring-1 ${
                  confirmPassword && confirmPassword !== newPassword
                    ? "border-red-500/80 focus:border-red-500 focus:ring-red-500/30"
                    : "border-slate-800 focus:border-cyan-500 focus:ring-cyan-500"
                }`}
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword((prev) => !prev)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white"
                aria-label="Toggle password visibility"
              >
                {showConfirmPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            {confirmPassword && confirmPassword !== newPassword && (
              <p className="mt-1 text-xs text-red-400">Passwords do not match</p>
            )}
          </div>

          {errorMsg && (
            <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-lg text-xs text-red-400 text-center font-medium">
              {errorMsg}
            </div>
          )}

          <button
            type="submit"
            disabled={isSubmitting || !isFormValid}
            className="w-full py-3 px-4 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-medium rounded-lg shadow-lg shadow-cyan-500/20 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {isSubmitting ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                <span>Resetting password...</span>
              </>
            ) : (
              <>
                <CheckCircle className="w-4 h-4" />
                <span>Reset Password</span>
              </>
            )}
          </button>
        </form>

        <div className="mt-6 pt-6 border-t border-slate-800/80 flex flex-col items-center gap-4 text-xs">
          <div className="flex items-center gap-2 text-slate-400">
            <span>Didn't receive the code?</span>
            <button
              onClick={handleResend}
              disabled={cooldown > 0 || isResending}
              className="text-cyan-400 font-semibold hover:underline disabled:opacity-50 disabled:no-underline flex items-center gap-1"
            >
              {isResending ? (
                <span>Sending...</span>
              ) : cooldown > 0 ? (
                <span>Resend in {cooldown}s</span>
              ) : (
                <span>Resend code</span>
              )}
            </button>
          </div>

          <Link
            to="/login"
            className="text-slate-400 hover:text-slate-200 flex items-center gap-1 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to sign in</span>
          </Link>
        </div>
      </div>
    </div>
  );
};
