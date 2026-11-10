
package dev.borishristov.heartbeat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Checks website availability by sending HTTP requests
 * and measuring response times.
 */
public class WebsiteChecker {

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

            return new CheckResult(url, response.statusCode(), durationInMs, null);

        } catch (Exception e) {
            return new CheckResult(url, null, null, e.toString());
        }
    }
}
