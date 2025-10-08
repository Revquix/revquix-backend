package com.revquix.backend.auth.util;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: EntrypointTypeUtil
 */

import com.revquix.backend.auth.enums.EntrypointType;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@UtilityClass
@Slf4j
public class EntrypointTypeUtil {

    public EntrypointType parse(String entrypoint) {
        log.info("{}::parse -> Parsing Entry Point -> {}", entrypoint);
        if (entrypoint.contains("@") && entrypoint.contains("."))
            return EntrypointType.email;
        else
            return EntrypointType.username;
    }
}
