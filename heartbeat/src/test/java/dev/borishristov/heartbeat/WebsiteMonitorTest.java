
package dev.borishristov.heartbeat;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class WebsiteMonitorTest {

    @Test
    void shouldCheckAllConfiguredWebsites() {
        WebsiteChecker checker = mock(WebsiteChecker.class);

        String url = "https://google.com";

        when(checker.check(url)).thenReturn(
                new CheckResult(url, 200, 150L, WebsiteStatus.UP, null)
        );

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (PrintStream out = new PrintStream(buffer)) {
            WebsiteMonitor monitor = new WebsiteMonitor(
                    checker,
                    List.of(url),
                    out,
                    Duration.ofSeconds(60),
                    Duration.ZERO
            );

            monitor.checkWebsites();

            verify(checker).check(url);

            assertTrue(buffer.toString().contains("Status: UP"));
            assertTrue(buffer.toString().contains("HTTP Status: 200"));
        }
    }


    @Test
    void shouldCheckWebsitesRepeatedlyUsingThread() {
        WebsiteChecker checker = mock(WebsiteChecker.class);
        String url = "https://example.com";

        when(checker.check(url)).thenReturn(
                new CheckResult(url, 200, 100L, WebsiteStatus.UP, null)
        );

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (PrintStream out = new PrintStream(buffer)) {
            WebsiteMonitor monitor = new WebsiteMonitor(
                    checker,
                    List.of(url),
                    out,
                    Duration.ofMillis(100),
                    Duration.ZERO
            );

            try {
                monitor.startMonitoring();

                verify(checker, timeout(2000).atLeast(3)).check(url);
            } finally {
                monitor.stopMonitoring();
            }
        }
    }

}
