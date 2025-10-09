/**
 * Proprietary License Agreement
 * <p>
 * Copyright (c) 2025 Revquix
 * <p>
 * This software is the confidential and proprietary property of Revquix and is provided under a
 * license, not sold. The application owner is Rohit Parihar and Revquix. Only authorized
 * Revquix administrators are permitted to copy, modify, distribute, or sublicense this software
 * under the terms set forth in this agreement.
 * <p>
 * Restrictions
 *
 * You are expressly prohibited from:
 * 1. Copying, modifying, distributing, or sublicensing this software without the express
 *    written permission of Rohit Parihar or Revquix.
 * 2. Reverse engineering, decompiling, disassembling, or otherwise attempting to derive
 *    the source code of the software.
 * 3. Altering or modifying the terms of this license without prior written approval from
 *    Rohit Parihar and Revquix administrators.
 * <p>
 * Disclaimer of Warranties:
 * This software is provided "as is" without any warranties, express or implied. Revquix makes
 * no representations or warranties regarding the software, including but not limited to any
 * warranties of merchantability, fitness for a particular purpose, or non-infringement.
 * <p>
 * For inquiries regarding licensing, please contact: support@Revquix.com.
 */
package com.revquix.backend.application.config;

import com.revquix.backend.application.properties.SwaggerConfigProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final SwaggerConfigProperties swaggerConfigProperties;

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group(swaggerConfigProperties.getGroupName().getDefinition())
                .packagesToScan(swaggerConfigProperties.getGroupName().getScanPackages())
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(swaggerConfigProperties.getInfo().getTitle())
                        .version(swaggerConfigProperties.getInfo().getVersion())
                        .description(swaggerConfigProperties.getInfo().getDescription())
                        .license(new License().name(swaggerConfigProperties.getInfo().getLicense().getName()).url(swaggerConfigProperties.getInfo().getLicense().getUrl()))
                        .contact(new Contact().name(swaggerConfigProperties.getInfo().getContact().getName()).email(swaggerConfigProperties.getInfo().getContact().getEmail()).url(swaggerConfigProperties.getInfo().getContact().getUrl())))
                .servers(getServers())
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("JWT Authentication")));
    }

    public List<Server> getServers(){
        Map<String, SwaggerConfigProperties.Server> servers = swaggerConfigProperties.getServers();
        List<Server> serversList = new ArrayList<>();
        for (String server : servers.keySet()){
            SwaggerConfigProperties.Server getServer = servers.get(server);
            serversList.add(new Server().description(getServer.getName()).url(getServer.getUrl()));
        }
        return serversList;
    }
}
