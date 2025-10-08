package com.revquix.backend.auth.dao.repository;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: UserAuthRepository
 */

import com.revquix.backend.auth.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, String> {

    @Query("SELECT u FROM UserAuth u WHERE LOWER(u.email) = LOWER(:entrypoint) OR LOWER(u.username) = LOWER(:entrypoint)")
    Optional<UserAuth> findByEntrypoint(String entrypoint);
}
