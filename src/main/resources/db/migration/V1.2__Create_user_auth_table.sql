-- Create user_auth table with sequence
-- Developer: Rohit Parihar
-- Project: revquix-backend

-- Create sequence for user_auth_id
CREATE SEQUENCE auth.user_auth_id_seq START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Create user_auth table
CREATE TABLE auth.user_auth (
    user_id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    username VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_enabled BOOLEAN DEFAULT TRUE,
    is_account_non_locked BOOLEAN DEFAULT TRUE,
    auth_provider TEXT[],
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP NOT NULL,
    last_password_change TIMESTAMP,
    register_ip VARCHAR(255),
    last_login_ip VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    user_badge VARCHAR(20) DEFAULT 'STANDARD',
    last_username_change TIMESTAMP
);

-- Add unique constraints
ALTER TABLE auth.user_auth
ADD CONSTRAINT uk_userauth_email UNIQUE (email);

ALTER TABLE auth.user_auth
ADD CONSTRAINT uk_userauth_username UNIQUE (username);

-- Create indexes
CREATE INDEX idx_userauth_userId ON auth.user_auth (user_id);
CREATE INDEX idx_userauth_email ON auth.user_auth (email);
CREATE INDEX idx_userauth_username ON auth.user_auth (username);
