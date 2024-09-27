package org.acme.attempt;

import io.quarkus.logging.Log;
import io.quarkus.security.spi.runtime.AuthenticationFailureEvent;
import io.quarkus.security.spi.runtime.AuthenticationSuccessEvent;
import io.quarkus.security.spi.runtime.SecurityEvent;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;

import java.util.Base64;

@ApplicationScoped
public class LoginAttemptObserver {

    public static final int BEGIN_INDEX = "Basic ".length();
    private final LoginAttemptService loginAttemptService;

    public LoginAttemptObserver(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    private static String getUsername(SecurityEvent event) {
        if (event.getSecurityIdentity() != null) {
            return event.getSecurityIdentity().getPrincipal().getName();
        }
        return null;
    }

    private static String getUsername(RoutingContext routingContext) {
        String authorization = routingContext.request().headers().get(HttpHeaders.AUTHORIZATION);
        try {
            String encoded = authorization.substring(BEGIN_INDEX);
            String decoded = new String(Base64.getDecoder().decode(encoded));
            int i = decoded.indexOf(':');
            if (i > -1) {
                return decoded.substring(0, i);
            }
        } catch (Exception e) {
            Log.debug("Could not get username from header", e);
        }
        return null;
    }

    public void observeAuthenticationSuccess(@ObservesAsync AuthenticationSuccessEvent event) {
        String principalName = getUsername(event);
        if (principalName == null) {
            return;
        }
        RoutingContext routingContext = (RoutingContext) event.getEventProperties().get(RoutingContext.class.getName());
        loginAttemptService.resetAuthAttempts(principalName, routingContext);
    }

    public void observeAuthenticationFailure(@ObservesAsync AuthenticationFailureEvent event) {
        RoutingContext routingContext = (RoutingContext) event.getEventProperties().get(RoutingContext.class.getName());
        String username = getUsername(routingContext);
        if (username != null) {
            loginAttemptService.addAuthFailure(username, routingContext);
        }
    }

}
