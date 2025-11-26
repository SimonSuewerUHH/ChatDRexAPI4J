package de.hamburg.university.api;


import de.hamburg.university.agent.provider.setting.UserLLMModelSetting;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.util.Optional;

@Provider
public class RequestDataFilter implements ContainerRequestFilter {

    @Inject
    UserLLMModelSetting llmSettings;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Optional.ofNullable(requestContext.getHeaderString("X-User-LLM-Model"))
                .ifPresent(llmSettings::setUserSetting);

    }
}