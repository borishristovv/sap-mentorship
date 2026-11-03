package dev.borishristov.heartbeat;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class WebsiteCheckerTest {

    @Test
    void shouldPrintHttp200ForWorkingWebsite() throws Exception {
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0
        );

        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        server.start();

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(output));

            WebsiteChecker checker = new WebsiteChecker();
            checker.check("http://127.0.0.1:" + server.getAddress().getPort());

            assertTrue(output.toString().contains("HTTP Status: 200"));

        } finally {
            System.setOut(originalOut);
            server.stop(0);
        }
    }

    @Test
    void shouldPrintHttpErrorCodes() throws Exception {
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

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(output));

            WebsiteChecker checker = new WebsiteChecker();
            String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();

            checker.check(baseUrl + "/not-found");
            checker.check(baseUrl + "/server-error");

            String result = output.toString();

            assertTrue(result.contains("HTTP Status: 404"));
            assertTrue(result.contains("HTTP Status: 500"));

        } finally {
            System.setOut(originalOut);
            server.stop(0);
        }
    }


    @Test
    void shouldHandleConnectionFailure() throws Exception {
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0
        );

        server.start();

        String url = "http://127.0.0.1:" + server.getAddress().getPort();

        // Stop the server before trying to connect.
        server.stop(0);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(output));

            WebsiteChecker checker = new WebsiteChecker();
            checker.check(url);

            String result = output.toString();

            assertTrue(result.contains("Status: DOWN"));
            assertTrue(result.contains("Error:"));

        } finally {
            System.setOut(originalOut);
        }
    }
}