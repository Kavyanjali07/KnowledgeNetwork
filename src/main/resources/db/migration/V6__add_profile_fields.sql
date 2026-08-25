-- Migration: V6__add_profile_fields.sql
-- Description: Persist editable user profile fields

ALTER TABLE users ADD COLUMN bio VARCHAR(500);
ALTER TABLE users ADD COLUMN status VARCHAR(40) NOT NULL DEFAULT 'online';
