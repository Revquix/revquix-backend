-- Create user_role join table for many-to-many relationship
-- Developer: Rohit Parihar
-- Project: revquix-backend

CREATE TABLE auth.user_role (
    user_auth_id VARCHAR(255) NOT NULL,
    role_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_auth_id, role_id),
    CONSTRAINT fk_user_role_user_auth
        FOREIGN KEY (user_auth_id)
        REFERENCES auth.user_auth(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id)
        REFERENCES auth.role(role_id)
        ON DELETE CASCADE
);

-- Create indexes for better performance on foreign key lookups
CREATE INDEX idx_user_role_user_auth_id ON auth.user_role (user_auth_id);
CREATE INDEX idx_user_role_role_id ON auth.user_role (role_id);
