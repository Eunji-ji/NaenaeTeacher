package com.naenae.student.auth.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class StudentSignupRateLimiterTest {

    @Test
    void blocksLookupRequestsAfterConfiguredLimit() {
        StudentSignupRateLimiter limiter = new StudentSignupRateLimiter(
                Clock.fixed(Instant.parse("2026-07-21T00:00:00Z"), ZoneOffset.UTC), 2, 1);

        limiter.checkLookup("127.0.0.1");
        limiter.checkLookup("127.0.0.1");

        assertThatThrownBy(() -> limiter.checkLookup("127.0.0.1"))
                .isInstanceOf(SignupRateLimitExceededException.class);
    }

    @Test
    void separatesClientsAndSignupBucket() {
        StudentSignupRateLimiter limiter = new StudentSignupRateLimiter(
                Clock.fixed(Instant.parse("2026-07-21T00:00:00Z"), ZoneOffset.UTC), 1, 1);

        limiter.checkLookup("client-a");
        limiter.checkLookup("client-b");
        limiter.checkSignup("client-a");

        assertThatThrownBy(() -> limiter.checkSignup("client-a"))
                .isInstanceOf(SignupRateLimitExceededException.class);
    }
}
