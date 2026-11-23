
package dev.borishristov.heartbeat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "heartbeat.monitor.enabled=true",
        "heartbeat.monitor.interval=100ms",
        "heartbeat.monitor.initial-delay=500ms"
})
class WebsiteMonitorIntegrationTest {

    @MockitoBean
    private WebsiteChecker checker;

    @MockitoBean
    private MonitorRepository repository;

    @Test
    void shouldAutomaticallyCheckWebsiteMultipleTimes() {

        String url = "https://example.com";

        when(repository.findAll()).thenReturn(
                List.of(new Monitor("https://example.com"))
        );

        when(checker.check(url)).thenReturn(
                new CheckResult(
                        url,
                        200,
                        150L,
                        WebsiteStatus.UP,
                        null
                )
        );

        verify(checker, timeout(4000).atLeast(3)).check(url);
    }
}
