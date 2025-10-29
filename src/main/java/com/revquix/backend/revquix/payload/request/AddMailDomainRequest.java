package com.revquix.backend.revquix.payload.request;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: AddMailDomainRequest
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.revquix.backend.application.utils.ModelPayload;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddMailDomainRequest extends ModelPayload<AddMailDomainRequest> {

    private String domain;
    private String description;
}
