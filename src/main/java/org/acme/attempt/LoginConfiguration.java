package org.acme.attempt;

import io.smallrye.config.ConfigMapping;

import java.time.Duration;

@ConfigMapping(prefix = "global.auth")
public interface LoginConfiguration {
    int maxFailures();

    Duration lockDelay();

    int cacheSize();
}
