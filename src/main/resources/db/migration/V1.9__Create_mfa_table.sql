CREATE TABLE auth.mfa (
    mfa_id VARCHAR(36) PRIMARY KEY,
    token VARCHAR(700) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    expires_in TIMESTAMP NOT NULL,
    date_created TIMESTAMP NOT NULL,
    remote_address VARCHAR(255) NOT NULL,
    os VARCHAR(255) NOT NULL,
    browser VARCHAR(255) NOT NULL
);
