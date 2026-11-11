
package dev.borishristov.heartbeat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * Checks website availability by sending HTTP requests
 * and measuring response times.
 */
@Component
public class WebsiteChecker {

    private static final long DEGRADED_THRESHOLD_MS = 1000;
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Performs an HTTP GET request and returns a structured result.
     * Network failures are represented as DOWN results.
     *
     * @param url the website URL to check
     * @return the HTTP response details or error information
     */
    public CheckResult check(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
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
     * Responses from 100 to 399 are considered healthy unless they exceed
     * the degraded response-time threshold.
     *
     * @param httpStatus the HTTP response status code
     * @param responseTimeMs the response time in milliseconds
     * @return UP, DOWN, or DEGRADED
     */
    public static WebsiteStatus classify(int httpStatus, long responseTimeMs) {
        if (httpStatus < 100 || httpStatus >= 400) {
            return WebsiteStatus.DOWN;
        }

        if (responseTimeMs >= DEGRADED_THRESHOLD_MS) {
            return WebsiteStatus.DEGRADED;
        }

        return WebsiteStatus.UP;
    }
}
