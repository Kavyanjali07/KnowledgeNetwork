-- Migration: V14__add_otp_type_column.sql
-- Description: Add otp_type column to verification_otps table to support password reset OTPs

ALTER TABLE verification_otps ADD COLUMN otp_type VARCHAR(30) NOT NULL DEFAULT 'EMAIL_VERIFICATION';

CREATE INDEX idx_verification_otps_user_type ON verification_otps(user_id, otp_type, used);
