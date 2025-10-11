CREATE TABLE auth.refresh_token (
    jti VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    date_created TIMESTAMP NOT NULL,
    expires_in BIGINT NOT NULL,
    remote_address VARCHAR(255) NOT NULL
);