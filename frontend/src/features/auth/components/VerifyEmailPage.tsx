import React, { useState, useEffect } from "react";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import { useAuth } from "../../../lib/auth-context";
import { useToast } from "../../../components/ui/toast";
import { OtpInput } from "./OtpInput";
import { Mail, ArrowLeft, RefreshCw, CheckCircle, ShieldCheck } from "lucide-react";
import type { ApiError } from "../../../lib/api-client";

export const VerifyEmailPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { verifyEmail, resendOtp } = useAuth();
  const { addToast } = useToast();

  const [email, setEmail] = useState(searchParams.get("email") || "");
  const [otp, setOtp] = useState("");
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

  const handleVerify = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!email || !email.includes("@")) {
      setErrorMsg("Please enter a valid email address.");
      return;
    }
    if (otp.length !== 6) {
      setErrorMsg("Please enter the complete 6-digit verification code.");
      return;
    }

    setIsSubmitting(true);
    setErrorMsg(null);

    try {
      await verifyEmail(email, otp);
      addToast({
        type: "success",
        title: "Email verified!",
        description: "Welcome to KnowledgeNetwork. Redirecting to workspace..."
      });
      navigate("/dashboard", { replace: true });
    } catch (err) {
      const apiErr = err as ApiError;
      setErrorMsg(apiErr.message || "Invalid or expired verification code.");
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
      await resendOtp(email);
      addToast({
        type: "info",
        title: "Code Sent",
        description: "A new 6-digit verification code has been sent to your email."
      });
      setCooldown(60);
    } catch (err) {
      const apiErr = err as ApiError;
      setErrorMsg(apiErr.message || "Failed to resend verification code.");
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#080c14] text-slate-100 flex flex-col justify-center items-center p-4 relative overflow-hidden">
      {/* Dynamic Background Effects */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-cyan-500/10 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-10 right-10 w-[300px] h-[300px] bg-purple-500/10 rounded-full blur-[100px] pointer-events-none" />

      <div className="w-full max-w-md bg-slate-900/80 backdrop-blur-xl border border-slate-800 rounded-2xl p-8 shadow-2xl z-10 relative">
        <div className="flex flex-col items-center text-center mb-8">
          <div className="w-14 h-14 rounded-2xl bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center mb-4 text-cyan-400">
            <ShieldCheck className="w-7 h-7" />
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-white mb-2">Check your email</h1>
          <p className="text-sm text-slate-400 leading-relaxed max-w-xs">
            We sent a 6-digit verification code to{" "}
            <span className="font-semibold text-cyan-300">{email || "your email address"}</span>
          </p>
        </div>

        <form onSubmit={handleVerify} className="space-y-6">
          {!searchParams.get("email") && (
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@company.com"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-4 py-2 text-sm text-white focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500"
                />
              </div>
            </div>
          )}

          <div>
            <label className="block text-center text-xs font-medium text-slate-400 mb-4">
              Enter 6-digit verification code
            </label>
            <OtpInput
              value={otp}
              onChange={(val) => {
                setOtp(val);
                if (errorMsg) setErrorMsg(null);
              }}
              disabled={isSubmitting}
              hasError={Boolean(errorMsg)}
            />
          </div>

          {errorMsg && (
            <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-lg text-xs text-red-400 text-center font-medium">
              {errorMsg}
            </div>
          )}

          <button
            type="submit"
            disabled={isSubmitting || otp.length !== 6}
            className="w-full py-3 px-4 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-medium rounded-lg shadow-lg shadow-cyan-500/20 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {isSubmitting ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                <span>Verifying code...</span>
              </>
            ) : (
              <>
                <CheckCircle className="w-4 h-4" />
                <span>Verify Email</span>
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
            className="text-slate-500 hover:text-slate-300 flex items-center gap-1 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to sign in</span>
          </Link>
        </div>
      </div>
    </div>
  );
};
