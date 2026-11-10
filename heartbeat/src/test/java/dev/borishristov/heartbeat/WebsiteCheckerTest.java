package dev.borishristov.heartbeat;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class WebsiteCheckerTest {

    @Test
    void shouldReturnHttp200ForWorkingWebsite() throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0
        );

        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        server.start();

        try {
            WebsiteChecker checker = new WebsiteChecker();
            String url = "http://127.0.0.1:" + server.getAddress().getPort();

            CheckResult result = checker.check(url);

            assertEquals(url, result.url());
            assertEquals(200, result.httpStatus());
            assertNotNull(result.responseTimeMs());
            assertTrue(result.responseTimeMs() >= 0);
            assertNull(result.error());

        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldReturnHttpErrorCodes() throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0
        );

        server.createContext("/not-found", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });

        server.createContext("/server-error", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });

        server.start();

        try {
            WebsiteChecker checker = new WebsiteChecker();
            String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();

            CheckResult notFound = checker.check(baseUrl + "/not-found");
            CheckResult serverError = checker.check(baseUrl + "/server-error");

            assertEquals(404, notFound.httpStatus());
            assertEquals(500, serverError.httpStatus());
            assertNull(notFound.error());
            assertNull(serverError.error());

        } finally {
            server.stop(0);
        }
    }


    @Test
    void shouldHandleConnectionFailure() throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0
        );


        String url;
        try {
            server.start();
            url = "http://127.0.0.1:" + server.getAddress().getPort();
        } finally {
            server.stop(0);
        }

        WebsiteChecker checker = new WebsiteChecker();
        CheckResult result = checker.check(url);

        assertEquals(url, result.url());
        assertNull(result.httpStatus());
        assertNull(result.responseTimeMs());
        assertNotNull(result.error());
    }
}