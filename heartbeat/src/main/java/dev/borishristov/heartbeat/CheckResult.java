package dev.borishristov.heartbeat;

/**
 * Represents the outcome of a single website check.
 *
 * @param url the checked website URL
 * @param httpStatus the HTTP response code, or null if unavailable
 * @param responseTimeMs the elapsed response time, or null if unavailable
 * @param status the classified website health status
 * @param error the error description, or null if the request succeeded
 */
public record CheckResult(
        String url,
        Integer httpStatus,
        Long responseTimeMs,
        WebsiteStatus status,
        String error
) {
}