package com.revquix.backend.revquix.service;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: DomainService
 */

import com.revquix.backend.auth.payload.response.AuthResponse;
import com.revquix.backend.revquix.payload.request.AddMailDomainRequest;
import org.springframework.http.ResponseEntity;

public interface DomainService {
    ResponseEntity<AuthResponse> addDomain(AddMailDomainRequest addMailDomainRequest);
}
