package dev.borishristov.heartbeat;

/**
 * Represents the result of a single website check.
 */
public record CheckResult(
        String url,
        Integer httpStatus,
        Long responseTimeMs,
        String error
) {
}