
package dev.borishristov.heartbeat;

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
     * Performs an HTTP GET request and returns the check result.
     *
     * @param url the URL of the website to check
     * @return the HTTP check result or error information
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

        } catch (Exception e) {
            return new CheckResult(url, null, null, WebsiteStatus.DOWN, e.toString());
        }
    }

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
