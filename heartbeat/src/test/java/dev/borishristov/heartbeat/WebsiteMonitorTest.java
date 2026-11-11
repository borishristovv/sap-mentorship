
package dev.borishristov.heartbeat;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WebsiteMonitorTest {

    @Test
    void shouldCheckAllConfiguredWebsites() {
        WebsiteChecker checker = mock(WebsiteChecker.class);

        List<String> urls = List.of("https://google.com", "https://github.com");

        when(checker.check("https://google.com"))
                .thenReturn(new CheckResult(
                        "https://google.com",
                        200,
                        150L,
                        WebsiteStatus.UP,
                        null
                ));

        when(checker.check("https://github.com"))
                .thenReturn(new CheckResult(
                        "https://github.com",
                        200,
                        120L,
                        WebsiteStatus.UP,
                        null
                ));

        WebsiteMonitor monitor = new WebsiteMonitor(checker, urls);

        monitor.checkWebsites();

        verify(checker).check("https://google.com");
        verify(checker).check("https://github.com");
        verifyNoMoreInteractions(checker);
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
