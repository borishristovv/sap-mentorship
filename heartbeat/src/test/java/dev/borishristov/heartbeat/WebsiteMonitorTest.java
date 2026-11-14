
package dev.borishristov.heartbeat;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                    out
            );

            monitor.checkWebsites();

            verify(checker).check(url);

            assertTrue(buffer.toString().contains("Status: UP"));
            assertTrue(buffer.toString().contains("HTTP Status: 200"));
        }
    }

    @Test
    void shouldUseConfiguredSchedulingInterval() throws NoSuchMethodException {
        Scheduled annotation = WebsiteMonitor.class
                .getDeclaredMethod("checkWebsites")
                .getAnnotation(Scheduled.class);

        assertEquals(
                "${heartbeat.monitor.interval-ms}",
                annotation.fixedDelayString()
        );
    }
}
