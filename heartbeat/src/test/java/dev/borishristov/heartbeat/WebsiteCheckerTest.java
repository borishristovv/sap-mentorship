package dev.borishristov.heartbeat;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class WebsiteCheckerTest {

    @Test
    void shouldReturnHttp200ForWorkingWebsite() throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 10
        );

        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        server.start();

        try (WebsiteChecker checker = new WebsiteChecker(Duration.ofSeconds(10))) {
            String url = "http://127.0.0.1:" + server.getAddress().getPort();

            CheckResult result = checker.check(url);

            assertEquals(url, result.url());
            assertEquals(200, result.httpStatus());
            assertNotNull(result.responseTimeMs());
            assertTrue(result.responseTimeMs() >= 0);
            assertNotEquals(WebsiteStatus.DOWN, result.status());
            assertNull(result.error());

        } finally {
            server.stop(0);
        }
    }


    @ParameterizedTest
    @ValueSource(ints = {400, 403, 404, 500, 503})
    void shouldReturnHttpErrorCodes(int httpStatus) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 10
        );

        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(httpStatus, -1);
            exchange.close();
        });

        server.start();

        try (WebsiteChecker checker = new WebsiteChecker(Duration.ofSeconds(10))) {
            String url = "http://127.0.0.1:" + server.getAddress().getPort();

            CheckResult result = checker.check(url);

            assertEquals(httpStatus, result.httpStatus());
            if (httpStatus < 500) {
                assertNotEquals(WebsiteStatus.DOWN, result.status());
            } else {
                assertEquals(WebsiteStatus.DOWN, result.status());
            }
            assertNotNull(result.responseTimeMs());
            assertNull(result.error());

        } finally {
            server.stop(0);
        }
    }



    @Test
    void shouldHandleConnectionFailure() throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 10
        );

        try (WebsiteChecker checker = new WebsiteChecker(Duration.ofSeconds(10))) {
            server.start();

            String url = "http://127.0.0.1:" + server.getAddress().getPort();

            // Stop the server to simulate a connection failure
            server.stop(0);

            CheckResult result = checker.check(url);

            assertEquals(url, result.url());
            assertNull(result.httpStatus());
            assertNull(result.responseTimeMs());
            assertEquals(WebsiteStatus.DOWN, result.status());
            assertNotNull(result.error());

        } finally {
            server.stop(0);
        }
    }


    @ParameterizedTest
    @CsvSource({
            "100, 100, UP",
            "200, 100, UP",
            "301, 500, UP",
            "200, 999, UP",
            "200, 1000, DEGRADED",
            "200, 1500, DEGRADED",
            "100, 1000, DEGRADED",
            "404, 1500, DEGRADED",
            "404, 100, UP",
            "500, 1500, DOWN",
            "0, 100, DOWN"
    })
    void shouldClassifyWebsiteStatuses(
            int httpStatus,
            long responseTimeMs,
            WebsiteStatus expectedStatus
    ) {
        assertEquals(
                expectedStatus,
                WebsiteChecker.classify(httpStatus, responseTimeMs)
        );
    }

}