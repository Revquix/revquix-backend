ALTER TABLE auth.user_auth
    ADD COLUMN mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE;

-- Ensure any existing NULLs (if any) are set to false
UPDATE auth.user_auth
SET mfa_enabled = FALSE
WHERE mfa_enabled IS NULL;