-- Create role table
-- Developer: Rohit Parihar
-- Project: revquix-backend

CREATE TABLE auth.role (
    role_id VARCHAR(255) PRIMARY KEY,
    role VARCHAR(255),
    internal_roles TEXT[],
    description TEXT
);
