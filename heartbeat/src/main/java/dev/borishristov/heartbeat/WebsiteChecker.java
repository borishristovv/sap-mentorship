
package dev.borishristov.heartbeat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 * Checks website availability by sending HTTP requests
 * and measuring response times.
 */
@Component
public class WebsiteChecker implements AutoCloseable {

    private static final Duration DEGRADED_THRESHOLD = Duration.ofSeconds(1);

    private final HttpClient client = HttpClient.newHttpClient();
    private final Duration timeout;


    /**
     * Creates a website checker with a configurable HTTP request timeout.
     *
     * @param timeout maximum time to wait for an HTTP response
     * @throws IllegalArgumentException if the timeout is not positive
     */
    public WebsiteChecker(
            @Value("${heartbeat.check.timeout:10s}") Duration timeout
    ) {
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("HTTP timeout must be positive");
        }

        this.timeout = timeout;
    }

    /**
     * Performs an HTTP GET request and measures its response time.
     * Network failures and timeouts are classified as DOWN.
     *
     * @param url the website URL to check
     * @return the HTTP response details or error information
     */
    public CheckResult check(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(timeout)
                    .GET()
                    .build();

            long start = System.nanoTime();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

            long durationInMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            int httpStatus = response.statusCode();
            WebsiteStatus status = classify(httpStatus, durationInMs);

            return new CheckResult(url, httpStatus, durationInMs, status, null);

        } catch (InterruptedException e) {
            // Restore the interrupted flag so callers can detect cancellation.
            Thread.currentThread().interrupt();
            return new CheckResult(
                    url, null, null, WebsiteStatus.DOWN, e.toString()
            );

        } catch (IOException | IllegalArgumentException e) {
            return new CheckResult(
                    url, null, null, WebsiteStatus.DOWN, e.toString()
            );
        }
    }


    /**
     * Classifies website health using the HTTP status code and response time.
     * Responses from 100 to 499 are considered UP or DEGRADED depending
     * on response time. Server errors (500+) are classified as DOWN.
     *
     * @param httpStatus the HTTP response status code
     * @param responseTimeMs the response time in milliseconds
     * @return UP, DOWN, or DEGRADED
     */
    public static WebsiteStatus classify(int httpStatus, long responseTimeMs) {
        if (httpStatus < 100 || httpStatus >= 500) {
            return WebsiteStatus.DOWN;
        }

        if (responseTimeMs >= DEGRADED_THRESHOLD.toMillis()) {
            return WebsiteStatus.DEGRADED;
        }

        return WebsiteStatus.UP;
    }

    @Override
    public void close() {
        client.close();
    }
}
