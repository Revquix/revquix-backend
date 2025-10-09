CREATE TABLE auth.otp_entity (
    otp_id VARCHAR(255) PRIMARY KEY,
    otp VARCHAR(10) NOT NULL,
    otp_for VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    times_sent INT NOT NULL DEFAULT 0,
    otp_status VARCHAR(20),
    user_id VARCHAR(255) NOT NULL
);

ALTER TABLE auth.otp_entity
    ADD CONSTRAINT uk_otpentity_otp UNIQUE (otp);