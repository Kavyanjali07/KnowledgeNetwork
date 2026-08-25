import React, { useRef } from "react";

interface OtpInputProps {
  value: string;
  onChange: (otp: string) => void;
  disabled?: boolean;
  hasError?: boolean;
}

export const OtpInput: React.FC<OtpInputProps> = ({
  value,
  onChange,
  disabled = false,
  hasError = false
}) => {
  const inputsRef = useRef<(HTMLInputElement | null)[]>([]);
  const length = 6;
  const digits = value.padEnd(length, "").slice(0, length).split("");

  const handleChange = (index: number, e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value.replace(/\D/g, "");
    if (!val) {
      // Cleared input
      const newDigits = [...digits];
      newDigits[index] = "";
      onChange(newDigits.join(""));
      return;
    }

    const lastDigit: string = (val && val.length > 0) ? (val[val.length - 1] ?? "") : "";
    const newDigits = [...digits];
    newDigits[index] = lastDigit;
    const newOtp = newDigits.join("");
    onChange(newOtp);

    // Auto-advance to next input
    if (index < length - 1 && lastDigit) {
      inputsRef.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Backspace") {
      if (!digits[index] && index > 0) {
        inputsRef.current[index - 1]?.focus();
      }
    } else if (e.key === "ArrowLeft" && index > 0) {
      inputsRef.current[index - 1]?.focus();
    } else if (e.key === "ArrowRight" && index < length - 1) {
      inputsRef.current[index + 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, length);
    if (!pastedData) return;

    onChange(pastedData);
    const focusIndex = Math.min(pastedData.length, length - 1);
    inputsRef.current[focusIndex]?.focus();
  };

  return (
    <div className="flex items-center justify-center gap-2 sm:gap-3" onPaste={handlePaste}>
      {Array.from({ length }).map((_, index) => {
        const digit = digits[index] && digits[index] !== " " ? digits[index] : "";
        return (
          <input
            key={index}
            ref={(el) => {
              inputsRef.current[index] = el;
            }}
            type="text"
            inputMode="numeric"
            pattern="\d*"
            maxLength={1}
            value={digit}
            disabled={disabled}
            onChange={(e) => handleChange(index, e)}
            onKeyDown={(e) => handleKeyDown(index, e)}
            aria-label={`Digit ${index + 1} of verification code`}
            className={`
              w-10 h-12 sm:w-12 sm:h-14 text-center text-xl sm:text-2xl font-mono font-bold rounded-lg
              bg-slate-900 border transition-all duration-200 outline-none
              ${
                hasError
                  ? "border-red-500/80 text-red-400 focus:ring-2 focus:ring-red-500/30"
                  : "border-slate-800 text-cyan-400 focus:border-cyan-500 focus:ring-2 focus:ring-cyan-500/20"
              }
              ${disabled ? "opacity-50 cursor-not-allowed" : "hover:border-slate-700"}
            `}
          />
        );
      })}
    </div>
  );
};
