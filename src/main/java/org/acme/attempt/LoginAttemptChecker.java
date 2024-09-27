package org.acme.attempt;

import io.quarkus.logging.Log;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.HttpSecurityUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.inject.Instance;

import java.util.function.Supplier;

@Priority(1)
@ApplicationScoped
public class LoginAttemptChecker implements IdentityProvider<UsernamePasswordAuthenticationRequest> {
    private final LoginAttemptService loginAttemptService;
    private final Instance<BlockingSecurity> blockingSecurity;

    public LoginAttemptChecker(LoginAttemptService loginAttemptService, Instance<BlockingSecurity> blockingSecurity) {
        this.loginAttemptService = loginAttemptService;
        this.blockingSecurity = blockingSecurity;
    }

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request,
                                              AuthenticationRequestContext context) {

        RoutingContext routingContext = HttpSecurityUtils.getRoutingContextAttribute(request);
        Uni<Boolean> accountLocked = loginAttemptService.isAccountLocked(request.getUsername(), routingContext);
        return accountLocked.map(bool -> {
            if (Boolean.TRUE.equals(bool)) {
                throw new ForbiddenException("Account " + request.getUsername() + " is locked");
            }
            return null;
        });
//        Uncomment to fix issues
//        BlockingSecurity function = blockingSecurity.get();
//        function.request = request;
//        return context.runBlocking(function);
    }

    @Override
    public int priority() {
        return SYSTEM_FIRST + 100;
    }

    @Dependent
    public static class BlockingSecurity implements Supplier<SecurityIdentity> {
        private final LoginAttemptService loginAttemptService;
        private UsernamePasswordAuthenticationRequest request;

        public BlockingSecurity(LoginAttemptService loginAttemptService) {
            this.loginAttemptService = loginAttemptService;
        }

        @Override
        @ActivateRequestContext
        public SecurityIdentity get() {
            RoutingContext routingContext = HttpSecurityUtils.getRoutingContextAttribute(request);
            Uni<Boolean> accountLocked = loginAttemptService.isAccountLocked(request.getUsername(), routingContext);
            accountLocked.map(bool -> {
                if (Boolean.TRUE.equals(bool)) {
                    throw new ForbiddenException("Account " + request.getUsername() + " is locked");
                }
                return null;
            }).await().indefinitely();
            return null;
        }
    }
}
