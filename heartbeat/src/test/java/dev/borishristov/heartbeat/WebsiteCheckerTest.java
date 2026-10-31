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
}