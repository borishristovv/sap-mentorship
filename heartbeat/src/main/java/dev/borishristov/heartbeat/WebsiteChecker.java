
package dev.borishristov.heartbeat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Checks website availability by sending HTTP requests
 * and measuring response times.
 */
public class WebsiteChecker {

    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Sends an HTTP GET request to the specified URL and prints
     * the HTTP status code and response time.
     * Reports connection errors if the request fails.
     *
     * @param url the URL of the website to check
     */
    public void check(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            long start = System.nanoTime();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            System.out.println("Website: " + url);
            System.out.println("HTTP Status: " + response.statusCode());
            System.out.println("Response Time: " + elapsedMs + " ms");

        } catch (Exception e) {
            System.out.println("Website: " + url);
            System.out.println("Status: DOWN");
            System.out.println("Error: " + e.getMessage());
        }
    }
}
