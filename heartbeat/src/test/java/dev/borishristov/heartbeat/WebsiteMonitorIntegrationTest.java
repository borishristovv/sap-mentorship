
package dev.borishristov.heartbeat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "heartbeat.monitor.enabled=true",
        "heartbeat.monitor.urls=https://example.com",
        "heartbeat.monitor.interval-ms=100",
        "heartbeat.monitor.initial-delay-ms=500"
})
class WebsiteMonitorIntegrationTest {

    @MockitoBean
    private WebsiteChecker checker;

    @Test
    void shouldAutomaticallyCheckWebsiteMultipleTimes() {

        String url = "https://example.com";

        when(checker.check(url)).thenReturn(
                new CheckResult(
                        url,
                        200,
                        150L,
                        WebsiteStatus.UP,
                        null
                )
        );

        verify(checker, timeout(4000).atLeast(3))
                .check(url);
    }
}
