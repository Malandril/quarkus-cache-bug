package org.acme.attempt;


import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class LoginAttemptService {
    private final LoginConfiguration loginConfig;
    private final CaffeineCache authFailureCache;

    public LoginAttemptService(LoginConfiguration loginConfig,
                               @CacheName("auth-cache") Cache authFailureCache) {
        this.loginConfig = loginConfig;
        this.authFailureCache = authFailureCache.as(CaffeineCache.class);
    }

    private static String computeKey(String username, String host) {
        return username + host;
    }

    private static String computeKey(String username, RoutingContext routingContext) {
        if (routingContext == null) {
            return username;
        }
        return username + routingContext.request().authority().host();
    }

    private int getAuthFailures(String key) {
        return this.authFailureCache.get(key, s -> 0).await().indefinitely();
    }

    private void setAuthFailures(String key, int authFailures) {
        authFailureCache.put(key, CompletableFuture.completedFuture(authFailures));
    }

    public Uni<Boolean> isAccountLocked(String username, String host) {
        return isAccountLocked(computeKey(username, host));
    }

    public Uni<Boolean> isAccountLocked(String username, RoutingContext routingContext) {
        return isAccountLocked(computeKey(username, routingContext));
    }

    public void resetAuthAttempts(String username, RoutingContext routingContext) {
        resetAuthAttempts(computeKey(username, routingContext));
    }

    public void addAuthFailure(String username, RoutingContext routingContext) {
        addAuthFailure(computeKey(username, routingContext));
    }

    public void clearAll() {
        authFailureCache.invalidateAll().await().indefinitely();
    }

    private Uni<Boolean> isAccountLocked(String key) {
        return this.authFailureCache.get(key, s -> 0).map(n -> n >= loginConfig.maxFailures());
    }

    private void resetAuthAttempts(String key) {
        authFailureCache.invalidate(key).await().indefinitely();
    }

    private void addAuthFailure(String key) {
        int authFailures = getAuthFailures(key);
        Log.infof("Adding 1 to auth fail %s: %s", key, authFailures);
        setAuthFailures(key, authFailures + 1);
    }


}
