package com.naenae.student.auth.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudentSignupRateLimiter {

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger();
    private final Clock clock;
    private final int lookupLimit;
    private final int signupLimit;

    @Autowired
    public StudentSignupRateLimiter(
            @Value("${app.security.signup-rate-limit.lookup-per-minute:30}") int lookupLimit,
            @Value("${app.security.signup-rate-limit.signup-per-ten-minutes:10}") int signupLimit
    ) {
        this(Clock.systemUTC(), lookupLimit, signupLimit);
    }

    StudentSignupRateLimiter(Clock clock, int lookupLimit, int signupLimit) {
        this.clock = clock;
        this.lookupLimit = lookupLimit;
        this.signupLimit = signupLimit;
    }

    public void checkLookup(String clientAddress) {
        check("lookup:" + normalize(clientAddress), lookupLimit, Duration.ofMinutes(1));
    }

    public void checkSignup(String clientAddress) {
        check("signup:" + normalize(clientAddress), signupLimit, Duration.ofMinutes(10));
    }

    private void check(String key, int limit, Duration duration) {
        Instant now = clock.instant();
        AtomicBoolean allowed = new AtomicBoolean();
        windows.compute(key, (ignored, current) -> {
            if (current == null || !current.expiresAt().isAfter(now)) {
                allowed.set(true);
                return new Window(1, now.plus(duration));
            }
            if (current.count() < limit) {
                allowed.set(true);
                return new Window(current.count() + 1, current.expiresAt());
            }
            return current;
        });
        if ((requestCounter.incrementAndGet() & 255) == 0) {
            windows.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        }
        if (!allowed.get()) {
            throw new SignupRateLimitExceededException();
        }
    }

    private String normalize(String clientAddress) {
        return clientAddress == null || clientAddress.isBlank() ? "unknown" : clientAddress;
    }

    private record Window(int count, Instant expiresAt) {
    }
}
